package org.edla.tmdb.client

import acyclic.file
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success, Try }

import org.edla.tmdb.api.Protocol.{ noCrew, unReleased }

//For demonstration purpose only
//No error handling and Await all around

object Usage extends App {

  //val apiKey = sys.env("apiKey")
  val apiKey = "REPLACE_THIS_WITH_YOUR_OWN_API_KEY"

  val tmdbClient = TmdbClient(apiKey, "en")

  implicit val timeout = 10 seconds

  val token = Try(Await.result(tmdbClient.getToken, timeout).request_token)
  token match {
    case Success(v) ⇒
      tmdbClient.log.info(s"OK got a token ${token}")
    case Failure(e) ⇒
      tmdbClient.log.info(e.getMessage())
      tmdbClient.shutdown
      System.exit(1)
  }

  val movies = Await.result(tmdbClient.searchMovie("shark", 1), timeout)
  for (m ← movies.results) {
    val movie = Await.result(tmdbClient.getMovie(m.id), timeout)
    Await.result(tmdbClient.downloadPoster(movie, s"${System.getProperty("user.home")}/poster-${m.id}.jpg"), timeout)
    val credits = Await.result(tmdbClient.getCredits(m.id), timeout)
    val director = credits.crew.filter(crew ⇒ crew.job == "Director").headOption
    val releases = Await.result(tmdbClient.getReleases(m.id), timeout)
    val release = releases.countries.filter(country ⇒ country.iso_3166_1 == "US").headOption.getOrElse(unReleased).release_date
    tmdbClient.log.info(s"OK got a movie ${movie.title}. Director name is ${director.getOrElse(noCrew).name} and US released date is ${release}")
  }

  tmdbClient.shutdown

}
