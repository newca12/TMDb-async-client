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

object TmdbClient {
  def apply(apiKey: String) = new TMDbClient(apiKey)
}

class TMDbClient(apiKey: String) extends TmdbApi {

  import scala.language.postfixOps
  import system.dispatcher // execution context for futures
  import scala.concurrent.duration._
  import org.edla.tmdb.api.Protocol._

  private implicit val system = ActorSystem()
  private implicit val timeout = Timeout(10.seconds)
  val log = Logging(system, getClass)

  private lazy val basicPipeline: HttpRequest ⇒ Future[spray.http.HttpResponse] = Await.result(async {
    await(IO(Http) ? Http.HostConnectorSetup("api.themoviedb.org", port = 80)) match {
      case Http.HostConnectorInfo(connector, _) ⇒
        sendReceive(connector)
    }
  }, 1.seconds)
  // alternative syntax
  /*  
  val pipeline0 = for (
    Http.HostConnectorInfo(connector, _) ← IO(Http) ? Http.HostConnectorSetup("api.themoviedb.org", port = 80)
  ) yield sendReceive(connector)
  lazy val basicPipeline = Await.result(pipeline0, 1.seconds)
  */

  private lazy val baseUrl = Await.result(getConfiguration(), 1 seconds).images.base_url

  def getConfiguration() = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Configuration]
    pipeline(Get(s"/3/configuration?api_key=${apiKey}"))
  }

  def getToken() = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[AuthenticateResult]
    pipeline(Get(s"/3/authentication/token/new?api_key=${apiKey}"))
  }

  def getMovie(id: Int) = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Movie]
    pipeline(Get(s"/3/movie/${id}?api_key=${apiKey}"))
  }

  def searchMovie(query: String) = {
    val pipeline = basicPipeline ~> mapErrors ~> unmarshal[Results]
    pipeline(Get(s"/3/search/movie?api_key=${apiKey}&query=${query}"))
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(10.second).await
    system.shutdown()
  }

  def downloadPoster(movie: Movie, path: String) = {
    import spray.http.HttpMethods._
    val url = s"${baseUrl}w154${movie.poster_path}"
    log.info(s"Going to download ${url}")
    val result = (IO(Http) ? HttpRequest(GET, Uri(url))).mapTo[HttpResponse]

    import java.nio.file.{ Paths, Files }
    import scala.concurrent.future

    val f = Future {
      result.foreach { response: HttpResponse ⇒
        val bytes = response.entity.data.toByteArray
        log.info(s"Got ${bytes.length} bytes")
        Files.write(Paths.get(path), bytes)
        log.info("done")
      }
    }
    Future.sequence(List(result, f))
  }

  private val mapErrors = (response: HttpResponse) ⇒ {
    import spray.json._
    if (response.status.isSuccess) response else {
      response.entity.asString.asJson.convertTo[Error] match {
        case e ⇒
          if (e.status_code == 7)
            throw new InvalidApiKeyException(message = e.status_message, code = e.status_code)
          else
            throw TmdbException(message = e.status_message, code = e.status_code)
      }
    }
  }
}