package org.edla.tmdb.client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.io.IOException
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Await
import scala.Left
import scala.Right
import scala.concurrent.duration.DurationInt
import org.edla.tmdb.api._
import org.edla.tmdb.api.Protocol._
import scala.concurrent.duration.FiniteDuration
import akka.event.Logging
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import java.net.URLEncoder
import java.io.{ FileOutputStream, File }
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.stream.scaladsl.Flow

object TmdbClient {
  def apply(apiKey: String, language: String = "en", tmdbTimeOut: FiniteDuration = 10 seconds) = new TmdbClient(apiKey, language, tmdbTimeOut)
}

class TmdbClient(apiKey: String, language: String, tmdbTimeOut: FiniteDuration) extends TmdbApi {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  private implicit val timeout = Timeout(tmdbTimeOut)
  val log = Logging(system, getClass)

  log.info(s"TMDb timeout value is ${tmdbTimeOut}")

  val limiterProps = Limiter.props(maxAvailableTokens = 30, tokenRefreshPeriod = new FiniteDuration(10, SECONDS), tokenRefreshAmount = 30)
  val limiter = system.actorOf(limiterProps, name = "testLimiter")

  lazy val tmdbConnectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection("api.themoviedb.org", 80)

  val poolClientFlow = Http().cachedHostConnectionPool[String]("api.themoviedb.org")

  def limitGlobal[T](limiter: ActorRef): Flow[T, T, Unit] = {
    import akka.pattern.ask
    import akka.util.Timeout
    Flow[T].mapAsync(1)((element: T) ⇒ {
      val limiterTriggerFuture = limiter ? Limiter.WantToPass
      limiterTriggerFuture.map((_) ⇒ element)
    })
  }

  def errorHandling(): Flow[HttpResponse, HttpResponse, Unit] = {
    //Flow[HttpResponse].mapAsyncUnordered(4)(response => response)
    Flow[HttpResponse].map {
      response ⇒
        if (response.status.isSuccess) response else {
          val err = Unmarshal(response.entity).to[Error] map {
            e ⇒
              if (e.status_code == 7)
                throw new InvalidApiKeyException(message = e.status_message, code = e.status_code)
              else
                throw TmdbException(message = e.status_message, code = e.status_code)
          }
          //TODO is it possible to avoid Await ?
          Await.result(err, 1 seconds)
        }
    }
  }

  def tmdbRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(limitGlobal(limiter)).via(tmdbConnectionFlow).via(errorHandling) runWith (Sink.head)

  /*  private lazy val baseUrl = Await.result(getConfiguration(), tmdbTimeOut) match {
    case Right(conf)        ⇒ conf.images.base_url
    case Left(errorMessage) ⇒ log.error(errorMessage.toString)

  }*/

  private lazy val baseUrl = Await.result(getConfiguration(), tmdbTimeOut).images.base_url

  def getConfiguration(): Future[Configuration] = {
    tmdbRequest(RequestBuilding.Get(s"/3/configuration?api_key=${apiKey}")).flatMap {
      response ⇒ Unmarshal(response.entity).to[Configuration]
    }
  }

  def getToken(): Future[AuthenticateResult] = tmdbRequest(RequestBuilding.Get(s"/3/authentication/token/new?api_key=${apiKey}")).flatMap {
    response ⇒ Unmarshal(response.entity).to[AuthenticateResult]
  }

  //could not find implicit value for parameter um: 
  /*    def generic[T](request: String): Future[T] = tmdbRequest(RequestBuilding.Get(request)).flatMap {
      response ⇒ Unmarshal(response.entity).to[T]
    }*/

  def getMovie(id: Long): Future[Movie] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/${id}?api_key=${apiKey}&language=${language}")).flatMap {
      response ⇒ Unmarshal(response.entity).to[Movie]
    }
  }

  def getCredits(id: Long): Future[Credits] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/${id}/credits?api_key=${apiKey}&language=${language}")).flatMap {
      response ⇒ Unmarshal(response.entity).to[Credits]
    }
  }

  def getReleases(id: Long): Future[Releases] = {
    tmdbRequest(RequestBuilding.Get(s"/3/movie/${id}/releases?api_key=${apiKey}")).flatMap {
      response ⇒ Unmarshal(response.entity).to[Releases]
    }
  }

  def searchMovie(query: String, page: Int): Future[Results] = {
    tmdbRequest(RequestBuilding.Get(s"/3/search/movie?api_key=${apiKey}&language=${language}&page=${page}&query=${URLEncoder.encode(query, "UTF-8")}")).flatMap {
      response ⇒ Unmarshal(response.entity).to[Results]
    }
  }

  def shutdown(): Unit = {
    system.terminate
    Await.result(system.whenTerminated, Duration.Inf)
    Limiter.system.terminate()
    Await.result(Limiter.system.whenTerminated, Duration.Inf)
    ()
  }

  def getPoster(movie: Movie) = {
    val url = s"${baseUrl}w154${movie.poster_path.get}"
    log.info(s"Going to download for ${movie.id} ${url}")
    val result: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url)).mapTo[HttpResponse]
    result.flatMap {
      resp ⇒ Future { resp.entity.dataBytes }
    }
  }

  def downloadPoster(movie: Movie, path: String) = {
    val posterPath = movie.poster_path
    if (posterPath.isDefined) {
      val url = s"${baseUrl}w154${posterPath.get}"
      log.info(s"Going to download ${url}")
      val result: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url)).mapTo[HttpResponse]

      import java.nio.file.{ Paths, Files }
      result.flatMap {
        resp ⇒
          Future {
            val file = new File(path)
            val outputStream = new FileOutputStream(file)
            resp.entity.dataBytes.runForeach({ byteString ⇒
              outputStream.write(byteString.toByteBuffer.array())
            }).onComplete(_ ⇒ outputStream.close())
            //log.info(s"Got ${bytes.length} bytes")
            //Files.write(Paths.get(path), bytes)
            log.info("done")
            true
          }
      }
    } else Future {
      log.info("no poster")
      false
    }
  }

}