package org.edla.tmdb

import java.io.File
import java.nio.file.{Files, Paths}

import org.edla.tmdb.api.Protocol.Movie

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

//import org.edla.tmdb.api.Protocol.Results
import org.edla.tmdb.client.{InvalidApiKeyException, TmdbClient}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{GivenWhenThen, Matchers, PropSpec}

//import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

class TmdbAsyncClientTest extends PropSpec with Matchers with ScalaFutures with GivenWhenThen {

  val apiKey: String = sys.env("apiKey")

  val tmdbClient = TmdbClient(apiKey)

  implicit val timeout: FiniteDuration = 10 seconds

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(1, Seconds))

  property("Authentication with valid API key should be successfull") {
    whenReady(tmdbClient.getToken) { authenticateResult =>
      authenticateResult.success should be(true)
    }
  }

  property("Authentication with invalid API key should throw InvalidApiKeyException") {
    TmdbClient("00000000000000000000000000000000").getToken.failed.futureValue shouldBe an[InvalidApiKeyException]
  }

  property("Search movie by name should return results") {
    whenReady(tmdbClient.searchMovie("shark", 1)) { results =>
      results.total_results should be > 5
    }
  }

  property("Get title from movie.id should be correct") {
    val path = Paths.get(s"${System.getProperty("java.io.tmpdir")}${File.separator}poster.jpg")
    whenReady(tmdbClient.getMovie(680)) { movie =>
      movie.title should be("Pulp Fiction")
      Then("the poster should be downloaded successfully")
      val poster = tmdbClient.downloadPoster(movie, path)
      if (poster.isDefined) {
        whenReady(poster.get) {
          _.wasSuccessful should equal(true)
        }
      }
      And("the poster should be OK")
      Files.size(path) should (be(17695) or be(17709))
    }
  }

  property("Do not flood remote server when download posters") {
    var count = 0
    for (pageNum <- List(1, 2)) {
      tmdbClient.searchMovie("batman", pageNum).map { movies =>
        for (result <- movies.results) {
          val movieF = tmdbClient.getMovie(result.id)
          movieF.onComplete {
            case Success(movie) =>
              count = count + 1
              val path = Paths.get(s"${System.getProperty("java.io.tmpdir")}${File.separator}${movie.id}.jpg")
              //println(movie.id + ":" + path)
              val poster = tmdbClient.downloadPoster(movie, path)
              if (poster.isDefined) {
                whenReady(poster.get) {
                  _.wasSuccessful should equal(true)
                }
              }
            case Failure(_) =>
          }
        }
      }
    }
    //Thread.sleep(30000)
    count shouldBe 40
  }

  property("Get director from movie.id should be correct") {
    whenReady(tmdbClient.getCredits(680)) { credits =>
      credits.crew.find(crew => crew.job == "Director").get.name should be("Quentin Tarantino")
    }
  }

  property("Get localized release date from movie.id should be correct") {
    whenReady(tmdbClient.getReleases(680)) { releases =>
      releases.countries.find(country => country.iso_3166_1 == "US").get.release_date should be("1994-09-23")
    }
  }

  property("Respect server rating (test 1)") {
    var count = 85
    for (_ <- 1 to 85) {
      val test: Future[Movie] = tmdbClient.getMovie(680)
      test onComplete {
        case Success(_) =>
          count = count - 1
        case Failure(_) =>
      }
    }
    Thread.sleep(30000)
    count shouldBe 0
  }

  property("Respect server rating (test 2)") {

    whenReady(tmdbClient.searchMovie("life", 1)) { movies =>
      for (m <- movies.results) {
        val movie = tmdbClient.getMovie(m.id)
        movie.onComplete {
          case Failure(e) => fail(e)
          case Success(_) =>
            val credits = tmdbClient.getCredits(m.id)
            credits.onComplete {
              case Failure(e) => fail(e)
              case Success(_) =>
            }
            val releases = tmdbClient.getReleases(m.id)
            releases.onComplete {
              case Failure(e) => fail(e)
              case Success(_) =>
            }
        }
      }
    }
    whenReady(tmdbClient.searchMovie("Take me", 1)) { movies =>
      for (m <- movies.results) {
        val movie = tmdbClient.getMovie(m.id)
        movie.onComplete {
          case Failure(e) => fail(e)
          case Success(_) =>
            val credits = tmdbClient.getCredits(m.id)
            credits.onComplete {
              case Failure(e) => fail(e)
              case Success(_) =>
            }
            val releases = tmdbClient.getReleases(m.id)
            releases.onComplete {
              case Failure(e) => fail(e)
              case Success(_) =>
            }
        }
      }
    }
    //Thread.sleep(2 * timeout.toMillis)
  }

  property("TMDb should shutdown gracefully") {
    tmdbClient.shutdown()
  }

}
