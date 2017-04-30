package org.edla.tmdb

import java.io.File
import java.nio.file.{Files, Paths}

import org.edla.tmdb.client.{InvalidApiKeyException, TmdbClient}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{GivenWhenThen, Matchers, PropSpec}

import scala.concurrent.duration.DurationInt

class TmdbAsyncClientTest extends PropSpec with Matchers with ScalaFutures with GivenWhenThen {

  val apiKey = sys.env("apiKey")

  val tmdbClient = TmdbClient(apiKey, "en")

  implicit val timeout = 10 seconds

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(1, Seconds))

  property("Authentication with valid API key should be successfull") {
    whenReady(tmdbClient.getToken) { authenticateResult ⇒
      authenticateResult.success should be(true)
    }
  }

  property("Authentication with invalid API key should throw InvalidApiKeyException") {
    assertThrows[InvalidApiKeyException](
      whenReady(TmdbClient("00000000000000000000000000000000", "en").getToken.failed) { e ⇒
        })
  }

  property("Search movie by name should return results") {
    whenReady(tmdbClient.searchMovie("shark", 1)) { results ⇒
      results.total_results should be > 5
    }
  }

  property("Get title from movie.id should be correct") {
    val path = Paths.get(s"${System.getProperty("java.io.tmpdir")}${File.separator}poster.jpg")
    whenReady(tmdbClient.getMovie(680)) { movie ⇒
      movie.title should be("Pulp Fiction")
      Then("the poster should be downloaded successfully")
      val poster = tmdbClient.downloadPoster(movie, path)
      if (poster.isDefined)
        whenReady(poster.get) {
          _.wasSuccessful should equal(true)
        }
      And("the poster should be OK")
      Files.size(path) should be(14982)
    }
  }

  property("Get director from movie.id should be correct") {
    whenReady(tmdbClient.getCredits(680)) { credits ⇒
      credits.crew.filter(crew ⇒ crew.job == "Director").headOption.get.name should be("Quentin Tarantino")
    }
  }

  property("Get localized release date from movie.id should be correct") {
    whenReady(tmdbClient.getReleases(680)) { releases ⇒
      releases.countries.filter(country ⇒ country.iso_3166_1 == "US").headOption.get.release_date should be(
        "1994-10-14")
    }
  }

  property("TMDb should shutdown gracefully") {
    tmdbClient.shutdown()
  }

}
