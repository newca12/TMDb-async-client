package org.edla.tmdb

import scala.concurrent.Future

trait TmdbApi {
  def getMovie(id: Int): Future[Movie]
  def searchMovie(query: String): Future[Results]
  def downloadPoster(movie: Movie, path: String): Future[List[Any]]
  def shutdown(): Unit
}