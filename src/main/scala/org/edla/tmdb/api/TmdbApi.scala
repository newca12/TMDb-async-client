package org.edla.tmdb.api

import scala.concurrent.Future

trait TmdbApi {
  def getMovie(id: Long): Future[Movie]
  def getConfiguration(): Future[Configuration]
  def getToken(): Future[AuthenticateResult]
  def searchMovie(query: String): Future[Results]
  def downloadPoster(movie: Movie, path: String): Future[Unit]
  def getPoster(movie: Movie): Future[Array[Byte]]
  def shutdown(): Unit
}