package org.edla.tmdb.client

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.edla.tmdb.api.Protocol._

//For demonstration purpose only
//No error handling and Await all around

object Usage extends App {
  import scala.language.postfixOps

  val apiKey = "REPLACE_THIS_WITH_YOUR_OWN_API_KEY"

  val tmdbClient = TmdbClient(apiKey)

  val token = Try(Await.result(tmdbClient.getToken, 5 seconds).request_token)
  token match {
    case Success(v) ⇒
      tmdbClient.log.info(s"OK got a token ${token}")
    case Failure(e) ⇒
      tmdbClient.log.info(e.getMessage())
      tmdbClient.shutdown
      System.exit(1)
  }

  val movies = Await.result(tmdbClient.searchMovie("shark", 1), 5 seconds)
  for (m ← movies.results) {
    val movie = Await.result(tmdbClient.getMovie(m.id), 5 seconds)
    Await.result(tmdbClient.downloadPoster(movie, s"${System.getProperty("user.home")}/poster-${m.id}.jpg"), 9 seconds)
    val credits = Await.result(tmdbClient.getCredits(m.id), 5 seconds)
    val director = credits.crew.filter(crew ⇒ crew.job == "Director").headOption
    tmdbClient.log.info(s"OK got a movie ${movie.title} and Director name is ${director.getOrElse(noCrew).name}")
  }

  tmdbClient.shutdown

}