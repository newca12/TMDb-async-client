package org.edla.tmdb.client

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.Success
import scala.util.Failure

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

  val movie = Await.result(tmdbClient.getMovie(54181), 5 seconds)
  tmdbClient.log.info(s"OK got a movie ${movie.title}")

  Await.result(tmdbClient.downloadPoster(movie, "/tmp/poster.jpg"), 5 seconds)

  val movies = Await.result(tmdbClient.searchMovie("shark"), 5 seconds)
  for (m ← movies.results) tmdbClient.log.info(s"find ${m.title}")

  tmdbClient.shutdown

}