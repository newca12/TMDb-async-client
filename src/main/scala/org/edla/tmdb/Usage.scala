package org.edla.tmdb

import org.edla.tmdb._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
object Usage extends App {
  import scala.language.postfixOps

  val apiKey = "REPLACE THIS WITH YOUR OWN API KEY"

  val tmdbClient = TmdbClient(apiKey)

  val token = Await.result(tmdbClient.getToken, 5 seconds).request_token
  tmdbClient.log.info(s"OK got a token ${token}")

  val movie = Await.result(tmdbClient.getMovie(54181), 5 seconds)
  tmdbClient.log.info(s"OK got a movie ${movie.title}")

  Await.result(tmdbClient.downloadPoster(movie, "/tmp/poster.jpg"), 5 seconds)

  val movies = Await.result(tmdbClient.searchMovie("shark"), 5 seconds)
  for (m ← movies.results) tmdbClient.log.info(s"find ${m.title}")

  tmdbClient.shutdown

}