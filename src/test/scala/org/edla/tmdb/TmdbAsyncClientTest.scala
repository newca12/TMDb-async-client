package org.edla.tmdb

import scala.concurrent.duration.DurationInt
import org.edla.tmdb.client.TmdbClient
import org.scalatest.{ Finders, Matchers, PropSpec }
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.concurrent.ScalaFutures.{ convertScalaFuture, patienceConfig, whenReady }
import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent.ScalaFutures
import java.nio.file.Paths
import java.nio.file.Files
import org.edla.tmdb.client.InvalidApiKeyException

class FullTestKitExampleSpec extends PropSpec with Matchers with ScalaFutures with GivenWhenThen {

  val apiKey = sys.env("apiKey")

  val tmdbClient = TmdbClient(apiKey, "en")

  implicit val timeout = 10 seconds

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(1, Seconds))

  property("Authentication with valid API key should be successfull") {
    whenReady(tmdbClient.getToken) { authenticateResult ⇒
      authenticateResult.success should be(true)
    }
  }

  property("Authentication with invalid API key should be successfull") {
    whenReady(TmdbClient("00000000000000000000000000000000", "en").getToken.failed) { e ⇒
      e shouldBe a[InvalidApiKeyException]
    }
  }

  property("Search movie by name should return results") {
    whenReady(tmdbClient.searchMovie("shark", 1)) { results ⇒
      results.total_results should be > 5
    }
  }

  property("Get title from movie.id should be correct") {
    val fileName = "/tmp/poster.jpg"
    whenReady(tmdbClient.getMovie(680)) { movie ⇒
      movie.title should be("Pulp Fiction")
      Then("the corresponding poster should be downloaded successfully")
      whenReady(tmdbClient.downloadPoster(movie, fileName)) { result ⇒
        result should be(true)
      }
      And("poster should be OK")
      Files.size(Paths.get(fileName)) should be(14982)
    }
  }

  property("Get director from movie.id should be correct") {
    whenReady(tmdbClient.getCredits(680)) { credits ⇒
      credits.crew.filter(crew ⇒ crew.job == "Director").headOption.get.name should be("Quentin Tarantino")
    }
  }

  property("Get localized release date from movie.id should be correct") {
    whenReady(tmdbClient.getReleases(680)) { releases ⇒
      releases.countries.filter(country ⇒ country.iso_3166_1 == "US").headOption.get.release_date should be("1994-10-14")
    }
  }

  property("TMDb should shutdown gracefukky") {
    tmdbClient.shutdown
  }

}
