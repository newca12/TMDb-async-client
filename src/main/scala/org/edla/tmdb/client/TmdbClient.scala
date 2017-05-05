package org.edla.tmdb.client

import java.net.URLEncoder
import java.nio.file.Path

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.sprayJsonUnmarshaller
import akka.http.scaladsl.model.Uri.apply
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.Timeout
import org.edla.tmdb.api.Protocol.{AuthenticateResult, Configuration, Credits, Error, Movie, Releases, Results}
import org.edla.tmdb.api.TmdbApi

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}

object TmdbClient {
  def apply(ApiKey: String, Language: String = "en", tmdbTimeOut: FiniteDuration = RequestRateLimitDelay): TmdbClient =
    new TmdbClient(ApiKey, Language, tmdbTimeOut)
}

class TmdbClient(apiKey: String, language: String, tmdbTimeOut: FiniteDuration) extends TmdbApi {

  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private implicit val timeout   = Timeout(tmdbTimeOut)
  private val ApiKey             = s"api_key=$apiKey"
  private val Language           = s"language=$language"
  private val MaxAvailableTokens = RequestRateLimitMax - 1
  private val TokenRefreshPeriod = RequestRateLimitDelay
  private val TokenRefreshAmount = RequestRateLimitMax - 1

  val tmdbConnectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection("api.themoviedb.org", Port)
  private val baseUrl =
    Await.result(getConfiguration, tmdbTimeOut).images.base_url
  val log = Logging(system, getClass)

  lazy val limiterProps =
    Limiter.props(MaxAvailableTokens, TokenRefreshPeriod, TokenRefreshAmount)
  lazy val limiter = system.actorOf(limiterProps, name = "testLimiter")

  log.info(s"TMDb timeout value is $tmdbTimeOut")

  def getConfiguration: Future[Configuration] = {
    tmdbRequest(RequestBuilding.Get(s"/3/configuration?$ApiKey")).flatMap { response ⇒
      Unmarshal(response.entity).to[Configuration]
    }
  }

  def getToken: Future[AuthenticateResult] =
    tmdbRequest(RequestBuilding.Get(s"/3/authentication/token/new?$ApiKey")).flatMap { response ⇒
      Unmarshal(response.entity).to[AuthenticateResult]
    }

  def getMovie(id: Long): Future[Movie] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/$id?$ApiKey&$Language")).flatMap { response ⇒
      Unmarshal(response.entity).to[Movie]
    }
  }

  //could not find implicit value for parameter um:
  //http://kto.so/2016/04/10/hakk-the-planet-implementing-akka-http-marshallers/
  /*
  def generic[T](request: String): Future[T] = tmdbRequest(RequestBuilding.Get(request)).flatMap { response ⇒
    Unmarshal(response.entity).to[T]
  }
   */

  def getCredits(id: Long): Future[Credits] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/$id/credits?$ApiKey&$Language")).flatMap { response ⇒
      Unmarshal(response.entity).to[Credits]
    }
  }

  def tmdbRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(tmdbConnectionFlow).via(limitGlobal(limiter)).via(errorHandling()) runWith Sink.head

  def limitGlobal(limiter: ActorRef): Flow[HttpResponse, HttpResponse, NotUsed] = {
    import akka.pattern.ask
    Flow[HttpResponse].mapAsync(parallelism = 1)((response: HttpResponse) ⇒ {
      val rateLimitRemaining   = response.getHeader("X-RateLimit-Remaining").get.value().toInt
      val limiterTriggerFuture = limiter ? Limiter.WantToPass(rateLimitRemaining)
      limiterTriggerFuture.map((_) ⇒ response)
    })
  }

  def errorHandling(): Flow[HttpResponse, HttpResponse, NotUsed] = {
    //Flow[HttpResponse].mapAsyncUnordered(4)(response => response)
    Flow[HttpResponse].map { response ⇒
      if (response.status.isSuccess) {
        response
      } else {
        val err =
          Unmarshal(response.entity).to[Error] map { e ⇒
            if (e.status_code == 7) {
              throw new InvalidApiKeyException(message = e.status_message, code = e.status_code)
            } else {
              throw TmdbException(message = e.status_message, code = e.status_code)
            }
          }
        //TODO is it possible to avoid Await ?
        Await.result(err, 1 seconds)
      }
    }
  }

  def getReleases(id: Long): Future[Releases] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/$id/releases?$ApiKey")).flatMap { response ⇒
      Unmarshal(response.entity).to[Releases]
    }
  }

  def searchMovie(query: String, page: Int): Future[Results] = {
    tmdbRequest(
      RequestBuilding
        .Get(s"/3/search/movie?$ApiKey&$Language&page=$page&query=${URLEncoder.encode(query, "UTF-8")}")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[Results]
    }
  }

  def shutdown(): Unit = {
    Http().shutdownAllConnectionPools().onComplete { _ ⇒
      system.terminate
      Await.result(system.whenTerminated, Duration.Inf)
      Limiter.system.terminate()
      Await.result(Limiter.system.whenTerminated, Duration.Inf)
      ()
    }
  }

  //http://stackoverflow.com/questions/34912143/how-to-download-a-http-resource-to-a-file-with-akka-streams-and-http
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]] = {
    val posterPath = movie.poster_path
    if (posterPath.isDefined) {
      val url = s"${baseUrl}w154${posterPath.get}"
      val result: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = url)).mapTo[HttpResponse]
      Some(result.flatMap { resp ⇒
        val source = resp.entity.dataBytes
        source.runWith(FileIO.toPath(path))
      })
    } else {
      None
    }
  }
}
