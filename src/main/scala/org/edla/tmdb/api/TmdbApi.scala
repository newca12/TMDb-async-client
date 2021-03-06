package org.edla.tmdb.api

//import acyclic.file
import java.nio.file.Path

import scala.concurrent.Future
import Protocol.{AuthenticateResult, Configuration, Credits, Movie, Results}
import akka.stream.IOResult

trait TmdbApi {
  def getMovie(id: Long): Future[Movie]
  def getCredits(id: Long): Future[Credits]
  def getConfiguration(): Future[Configuration]
  def getToken(): Future[AuthenticateResult]
  def searchMovie(query: String, page: Int): Future[Results]
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]]
  def shutdown(): Unit
}
