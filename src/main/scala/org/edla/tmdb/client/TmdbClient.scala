package org.edla.tmdb.client

import java.net.URLEncoder
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.sprayJsonUnmarshaller
import akka.http.scaladsl.model.Uri.apply
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream._
import akka.stream.contrib.DelayFlow
import akka.stream.contrib.DelayFlow.DelayStrategy
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source, SourceQueueWithComplete}
import org.edla.tmdb.api.Protocol.{AuthenticateResult, Configuration, Credits, Error, Movie, Releases, Results}
import org.edla.tmdb.api.TmdbApi

import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success, Try}

object TmdbClient {
  def apply(ApiKey: String, Language: String = "en", tmdbTimeOut: FiniteDuration = RequestRateLimitDelay): TmdbClient =
    new TmdbClient(ApiKey, Language, tmdbTimeOut)
}

class TmdbClient(apiKey: String, language: String, tmdbTimeOut: FiniteDuration) extends TmdbApi {

  implicit val system: ActorSystem                = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withInputBuffer(initialSize = 1, maxSize = 1)
      .withMaxFixedBufferSize(1)
      .withOutputBurstLimit(1)
  )

  private val ApiKey   = s"api_key=$apiKey"
  private val Language = s"language=$language"

  //https://stackoverflow.com/a/49068165

  val noDelay: FiniteDuration = FiniteDuration(0L, TimeUnit.SECONDS)

  val strategySupplier: () => DelayStrategy[HttpResponse] = () =>
    (response: HttpResponse) => {
      val rate = RateLimit(response.headers)
      if (rate.remaining > 1) {
        noDelay
      } else {
        (rate.reset - Instant.now.getEpochSecond + 1).seconds
      }
    }
  val delayFlow: Flow[HttpResponse, HttpResponse, NotUsed] = DelayFlow(strategySupplier)

  val tmdbConnectionFlow: Flow[
    (HttpRequest, Promise[HttpResponse]),
    (Try[HttpResponse], Promise[HttpResponse]),
    Http.HostConnectionPool
  ] =
    Http().cachedHostConnectionPool("api.themoviedb.org", Port)

  private lazy val baseUrl =
    Await.result(getConfiguration, tmdbTimeOut).images.base_url
  val log = Logging(system, getClass)

  val bufferSize = 100

  //if the buffer fills up then this strategy drops the oldest elements
  //upon the arrival of a new element.
  val overflowStrategy: OverflowStrategy = akka.stream.OverflowStrategy.dropHead

  lazy val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = Source
    .queue(bufferSize, overflowStrategy)
    .via(tmdbConnectionFlow)
    .via(errorHandling())
    .via(delayFlow)
    .to(Sink.ignore)
    .run()

  log.info(s"TMDb timeout value is $tmdbTimeOut")

  //could not find implicit value for parameter um:
  //https://web.archive.org/web/20180816230355/http://kto.so/2016/04/10/hakk-the-planet-implementing-akka-http-marshallers/
  /*
  def manageRequest[@specialized T](request: String): Future[T] = {
    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(request), promise))
    promise.future.flatMap { response ⇒
      Unmarshal(response.entity).to[T]
  }
   */

  def getConfiguration: Future[Configuration] = {

    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(s"/3/configuration?$ApiKey"), promise))
    promise.future.flatMap { response =>
      Unmarshal(response.entity).to[Configuration]
    }
  }

  def getToken: Future[AuthenticateResult] = {
    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(s"/3/authentication/token/new?$ApiKey"), promise))
    val f: Future[HttpResponse] = promise.future
    f recover { case cause => throw cause }
    f.flatMap { response =>
      Unmarshal(response.entity).to[AuthenticateResult]
    }
  }

  def getMovie(id: Int): Future[Movie] = {
    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(s"/3/movie/$id?$ApiKey&$Language"), promise))
    promise.future.flatMap { response =>
      Unmarshal(response.entity).to[Movie]
    }
  }

  def getCredits(id: Int): Future[Credits] = {
    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(s"/3/movie/$id/credits?$ApiKey&$Language"), promise))
    promise.future.flatMap { response =>
      Unmarshal(response.entity).to[Credits]
    }
  }

  def getReleases(id: Int): Future[Releases] = {
    val promise = Promise[HttpResponse]()
    queue.offer((RequestBuilding.Get(s"/3/movie/$id/releases?$ApiKey"), promise))
    promise.future.flatMap { response =>
      Unmarshal(response.entity).to[Releases]
    }
  }

  def searchMovie(query: String, page: Int): Future[Results] = {
    val promise = Promise[HttpResponse]()
    queue.offer(
      (
        RequestBuilding.Get(s"/3/search/movie?$ApiKey&$Language&page=$page&query=${URLEncoder.encode(query, "UTF-8")}"),
        promise
      )
    )
    promise.future.flatMap { response =>
      Unmarshal(response.entity).to[Results]
    }
  }

  def errorHandling(): Flow[(Try[HttpResponse], Promise[HttpResponse]), HttpResponse, NotUsed] =
    Flow[(Try[HttpResponse], Promise[HttpResponse])].map {
      case (Success(response), p) =>
        if (response.status.isSuccess) {
          p.success(response)
          response
        } else {
          Unmarshal(response.entity).to[Error] map { e =>
            if (e.status_code == 7) {
              p.failure(new InvalidApiKeyException(message = e.status_message, code = e.status_code))
            } else {
              p.failure(TmdbException(message = e.status_message, code = e.status_code))
            }
          }
          response
        }
      case (Failure(t), _) =>
        throw t
    }

  def shutdown(): Unit = {
    Http().shutdownAllConnectionPools().onComplete { _ =>
      system.terminate
      Await.result(system.whenTerminated, Duration.Inf)
      ()
    }
  }

  //http://stackoverflow.com/questions/34912143/how-to-download-a-http-resource-to-a-file-with-akka-streams-and-http
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]] = {
    val posterPath = movie.poster_path
    val settings   = ConnectionPoolSettings(system).withMaxOpenRequests(64)
    if (posterPath.isDefined) {
      val url = s"${baseUrl}w154${posterPath.get}"
      val result: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = url), settings = settings).mapTo[HttpResponse]
      Some(result.flatMap { resp =>
        val source = resp.entity.dataBytes
        source.runWith(FileIO.toPath(path))
      })
    } else {
      None
    }
  }

}
