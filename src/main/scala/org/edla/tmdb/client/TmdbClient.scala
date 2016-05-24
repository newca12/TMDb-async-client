package org.edla.tmdb.client

import java.io.{File, FileOutputStream}
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.sprayJsonUnmarshaller
import akka.http.scaladsl.model.Uri.apply
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import org.edla.tmdb.api.Protocol.{AuthenticateResult, Configuration, Credits, Error, Movie, Releases, Results}
import org.edla.tmdb.api.TmdbApi

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration, SECONDS}
import scala.concurrent.{Await, Future}

object TmdbClient {
  def apply(ApiKey: String,
            Language: String = "en",
            tmdbTimeOut: FiniteDuration = 10 seconds): TmdbClient =
    new TmdbClient(ApiKey, Language, tmdbTimeOut)
}

class TmdbClient(apiKey: String, language: String, tmdbTimeOut: FiniteDuration)
    extends TmdbApi {

  private val ApiKey = s"api_key=$apiKey"
  private val Language = s"language=$language"
  private val MaxAvailableTokens = 10
  // scalastyle:off magic.number
  private val TokenRefreshPeriod = new FiniteDuration(5, SECONDS)
  // scalastyle:on magic.number
  private val TokenRefreshAmount = 10
  private val Port = 80

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withInputBuffer(
          initialSize = 1,
          maxSize = 1
      )
  )
  private implicit val timeout = Timeout(tmdbTimeOut)
  val log = Logging(system, getClass)

  log.info(s"TMDb timeout value is $tmdbTimeOut")

  val limiterProps =
    Limiter.props(MaxAvailableTokens, TokenRefreshPeriod, TokenRefreshAmount)
  val limiter = system.actorOf(limiterProps, name = "testLimiter")

  //scalastyle:off no.whitespace.after.left.bracket
  lazy val tmdbConnectionFlow: Flow[
      HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection("api.themoviedb.org", Port)
  //scalastyle:on no.whitespace.after.left.bracket

  val poolClientFlow =
    Http().cachedHostConnectionPool[String]("api.themoviedb.org")

  def limitGlobal[T](limiter: ActorRef): Flow[T, T, NotUsed] = {
    import akka.pattern.ask
    Flow[T].mapAsync(1)((element: T) ⇒ {
      val limiterTriggerFuture = limiter ? Limiter.WantToPass
      limiterTriggerFuture.map((_) ⇒ element)
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
              throw new InvalidApiKeyException(message = e.status_message,
                                               code = e.status_code)
            } else {
              throw TmdbException(message = e.status_message,
                                  code = e.status_code)
            }
          }
        //TODO is it possible to avoid Await ?
        Await.result(err, 1 seconds)
      }
    }
  }

  def tmdbRequest(request: HttpRequest): Future[HttpResponse] =
    Source
      .single(request)
      .via(limitGlobal(limiter))
      .via(tmdbConnectionFlow)
      .via(errorHandling()) runWith Sink.head

  private lazy val baseUrl =
    Await.result(getConfiguration, tmdbTimeOut).images.base_url

  //could not find implicit value for parameter um:
  /*    def generic[T](request: String): Future[T] = tmdbRequest(RequestBuilding.Get(request)).flatMap {
      response ⇒ Unmarshal(response.entity).to[T]
    }*/

  def getConfiguration: Future[Configuration] = {
    tmdbRequest(RequestBuilding.Get(s"/3/configuration?$ApiKey")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[Configuration]
    }
  }

  def getToken: Future[AuthenticateResult] =
    tmdbRequest(RequestBuilding.Get(s"/3/authentication/token/new?$ApiKey")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[AuthenticateResult]
    }

  def getMovie(id: Long): Future[Movie] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/$id?$ApiKey&$Language")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[Movie]
    }
  }

  def getCredits(id: Long): Future[Credits] = {
    tmdbRequest(RequestBuilding.Get(
            s"/3/movie/$id/credits?$ApiKey&$Language")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[Credits]
    }
  }

  def getReleases(id: Long): Future[Releases] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/$id/releases?$ApiKey")).flatMap {
      response ⇒
        Unmarshal(response.entity).to[Releases]
    }
  }

  def searchMovie(query: String, page: Int): Future[Results] = {
    tmdbRequest(
        RequestBuilding.Get(
            s"/3/search/movie?$ApiKey&$Language&page=$page&query=${URLEncoder
      .encode(query, "UTF-8")}")).flatMap { response ⇒
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

  def downloadPoster(movie: Movie, path: String): Future[Boolean] = {
    val posterPath = movie.poster_path
    if (posterPath.isDefined) {
      val url = s"${baseUrl}w154${posterPath.get}"
      val result: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = url)).mapTo[HttpResponse]
      result.flatMap { resp ⇒
        val latch = new CountDownLatch(1)
        val file = new File(path)
        val outputStream = new FileOutputStream(file)
        val res = Future {
          resp.entity.dataBytes
            .runForeach({ byteString ⇒
              outputStream.write(byteString.toByteBuffer.array())
            })
            .onComplete { _ ⇒
              outputStream.close(); latch.countDown()
            }
        }
        latch.await()
        res.map(_ ⇒ true)
      }
    } else {
      Future {
        false
      }
    }
  }
}
