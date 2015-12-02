package org.edla.tmdb

import scala.concurrent.duration.DurationInt
import org.edla.tmdb.client.TmdbClient
import org.scalatest.{ Finders, Matchers, PropSpec }
import org.scalatest.time.{Seconds, Span}
import org.scalatest.concurrent.ScalaFutures.{ convertScalaFuture, patienceConfig, whenReady }
import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent.ScalaFutures

class FullTestKitExampleSpec extends PropSpec with Matchers with ScalaFutures {

  val apiKey = sys.env("apiKey")

  val tmdbClient = TmdbClient(apiKey, "en")

  implicit val timeout = 10 seconds
  
 implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(1, Seconds))

  property("Authentication should be successfull") {
    whenReady(tmdbClient.getToken) { s ⇒
      s.success should be(true)
    }
  }

}