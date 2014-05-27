package org.edla.tmdb.client

import scala.async.Async.async
import scala.async.Async.await
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Success
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.event.Logging
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import spray.can.Http
import spray.client.pipelining.Get
import spray.client.pipelining.WithTransformerConcatenation
import spray.client.pipelining.sendReceive
import spray.client.pipelining.unmarshal
import spray.http.HttpMethods.GET
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.Uri
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.util.pimpFuture
import org.edla.tmdb.api._
import org.edla.tmdb.api.Protocol._
import scala.concurrent.duration.FiniteDuration
import java.net.URLEncoder
import com.pragmasoft.reactive.throttling.threshold.Frequency._
import com.pragmasoft.reactive.throttling.http.client.HttpClientThrottling
import HttpClientThrottling._
import com.pragmasoft.reactive.throttling.threshold._
import spray.http.HttpHeaders.Host

object TmdbClient {
  def apply(apiKey: String, tmdbTimeOut: FiniteDuration = 5 seconds) = new TmdbClient(apiKey, tmdbTimeOut)
}

class TmdbClient(apiKey: String, tmdbTimeOut: FiniteDuration) extends TmdbApi {

  import scala.language.postfixOps
  import system.dispatcher // execution context for futures
  import scala.concurrent.duration._

  private implicit val system = ActorSystem()
  private implicit val timeout = Timeout(tmdbTimeOut)
  val log = Logging(system, getClass)

  log.info(s"TMDb timeout value is ${tmdbTimeOut}")

  def addHost = { request: HttpRequest ⇒
    request.withEffectiveUri(false, Host("api.themoviedb.org", 80))
  }

  val basicPipeline = addHost ~> sendReceive(throttleFrequencyAndParallelRequests(3 perSecond, 20))

  private lazy val baseUrl = Await.result(getConfiguration(), tmdbTimeOut).images.base_url

  def getConfiguration() = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Configuration]
    pipeline(Get(s"/3/configuration?api_key=${apiKey}"))
  }

  def getToken() = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[AuthenticateResult]
    pipeline(Get(s"/3/authentication/token/new?api_key=${apiKey}"))
  }

  def getMovie(id: Long) = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Movie]
    pipeline(Get(s"/3/movie/${id}?api_key=${apiKey}"))
  }

  def getCredits(id: Long) = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Credits]
    pipeline(Get(s"/3/movie/${id}/credits?api_key=${apiKey}"))
  }

  def searchMovie(query: String, page: Long) = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Results]
    pipeline(Get(s"/3/search/movie?api_key=${apiKey}&page=${page}&query=${URLEncoder.encode(query, "UTF-8")}"))
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(10.second).await
    system.shutdown()
  }

  def getPoster(movie: Movie) = {
    import spray.http.HttpMethods._
    val url = s"${baseUrl}w154${movie.poster_path.get}"
    log.info(s"Going to download for ${movie.id} ${url}")
    val result = (IO(Http) ? HttpRequest(GET, Uri(url))).mapTo[HttpResponse]
    result.flatMap {
      resp ⇒ Future { resp.entity.data.toByteArray }
    }
  }

  def downloadPoster(movie: Movie, path: String) = {
    import spray.http.HttpMethods._
    val url = s"${baseUrl}w154${movie.poster_path.get}"
    log.info(s"Going to download ${url}")
    val result = (IO(Http) ? HttpRequest(GET, Uri(url))).mapTo[HttpResponse]

    import java.nio.file.{ Paths, Files }
    result.flatMap {
      resp ⇒
        Future {
          val bytes = resp.entity.data.toByteArray
          log.info(s"Got ${bytes.length} bytes")
          Files.write(Paths.get(path), bytes)
          log.info("done")
        }
    }
  }

  private val mapErrors = (response: HttpResponse) ⇒ {
    import spray.json._
    if (response.status.isSuccess) response else {
      JsonParser(response.entity.asString).convertTo[Error] match {
        case e ⇒
          if (e.status_code == 7)
            throw new InvalidApiKeyException(message = e.status_message, code = e.status_code)
          else
            throw TmdbException(message = e.status_message, code = e.status_code)
      }
    }
  }
}