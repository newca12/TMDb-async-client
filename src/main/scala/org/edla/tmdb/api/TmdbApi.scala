package org.edla.tmdb.api

import scala.concurrent.Future
import Protocol._

trait TmdbApi {
  def getMovie(id: Long): Future[Movie]
  def getCredits(id: Long): Future[Credits]
  def getConfiguration(): Future[Configuration]
  def getToken(): Future[AuthenticateResult]
  def searchMovie(query: String, page: Long): Future[Results]
  def downloadPoster(movie: Movie, path: String): Future[Unit]
  def getPoster(movie: Movie): Future[Array[Byte]]
  def shutdown(): Unit
}