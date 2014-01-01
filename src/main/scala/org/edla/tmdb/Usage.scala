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

  val movies = Await.result(tmdbClient.searchMovie("shark"), 5 seconds)
  for (m ← movies.results) tmdbClient.log.info(s"find ${m.title}")

  tmdbClient.downloadPoster(movie, "/tmp/poster.jpg")

  //downloadPoster is async so we need to wait a little before shutdown
  //TODO : http://letitcrash.com/post/30165507578/shutdown-patterns-in-akka-2
  Thread.sleep(5000)

  tmdbClient.shutdown

}