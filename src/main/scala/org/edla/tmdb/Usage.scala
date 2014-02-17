package org.edla.tmdb

import org.edla.tmdb._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.async.Async.async
import scala.async.Async.await
import scala.concurrent.ExecutionContext

object Usage extends App {
  import scala.language.postfixOps
  import ExecutionContext.Implicits.global

  val apiKey = "REPLACE_THIS_WITH_YOUR_OWN_API_KEY"

  val tmdbClient = TmdbClient(apiKey)

  //val token = Await.result(tmdbClient.getToken, 5 seconds).request_token
  //tmdbClient.log.info(s"OK got a token ${token}")

  async {
    val f = tmdbClient.getToken
    await(tmdbClient.getToken) match {
      case AuthenticateResult(expires_at: String, request_token: String, success: Boolean) ⇒
        tmdbClient.log.info("API key is valid")
      case Error(status_code: Long, status_message: String) ⇒
        tmdbClient.log.info(s"[status_code: ${status_code}] ${status_message}")
      case _ ⇒ tmdbClient.log.info("Should not happen")
    }
  }

  //  
  //  val movie = Await.result(tmdbClient.getMovie(54181), 5 seconds)
  //  tmdbClient.log.info(s"OK got a movie ${movie.title}")
  //
  //  Await.result(tmdbClient.downloadPoster(movie, "/tmp/poster.jpg"), 5 seconds)
  //
  //  val movies = Await.result(tmdbClient.searchMovie("shark"), 5 seconds)
  //  for (m ← movies.results) tmdbClient.log.info(s"find ${m.title}")

  Thread.sleep(3000)

  tmdbClient.shutdown

}