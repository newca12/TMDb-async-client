package org.edla.tmdb.api

import java.nio.file.Path

import akka.stream.IOResult
import org.edla.tmdb.api.Protocol.{AuthenticateResult, Configuration, Credits, Movie, Results}

import scala.concurrent.Future

trait TmdbApi {
  def getMovie(id: Long): Future[Movie]
  def getCredits(id: Long): Future[Credits]
  def getConfiguration: Future[Configuration]
  def getToken: Future[AuthenticateResult]
  def searchMovie(query: String, page: Int): Future[Results]
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]]
  def shutdown(): Unit
}
