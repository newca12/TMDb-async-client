package org.edla.tmdb.api

import java.nio.file.Path

import akka.stream.IOResult
import org.edla.tmdb.api.Protocol.{AuthenticateResult, Configuration, Credits, Movie, Releases, Results}

import scala.concurrent.Future

trait TmdbApi {
  def getMovie(id: Int): Future[Movie]
  def getCredits(id: Int): Future[Credits]
  def getReleases(id: Int): Future[Releases]
  def getConfiguration: Future[Configuration]
  def getToken: Future[AuthenticateResult]
  def searchMovie(query: String, page: Int): Future[Results]
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]]
  def shutdown(): Unit

}
