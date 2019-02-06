package org.edla.tmdb.client

import akka.http.scaladsl.model.HttpHeader

case class RateLimit(limit: Int, remaining: Int, reset: Long)

//https://github.com/DanielaSfregola/twitter4s/pull/116/files
object RateLimit {

  def apply(headers: Seq[HttpHeader]): RateLimit = {
    val errorMsg =
      s"""Rate Information expected but not found.
         |
         |Please report it at https://github.com/newca12/TMDb-async-client/issues/new
         |Headers names were: ${headers.map(_.lowercaseName).mkString(", ")}""".stripMargin

    def extractHeaderValue[T](name: String)(f: String => T): T =
      headers
        .find(_.lowercaseName == name)
        .map(h => f(h.value))
        .getOrElse(throw TmdbException(errorMsg))

    val limit     = extractHeaderValue("x-ratelimit-limit")(_.toInt)
    val remaining = extractHeaderValue("x-ratelimit-remaining")(_.toInt)
    val reset     = extractHeaderValue("x-ratelimit-reset")(_.toLong)
    apply(limit, remaining, reset)
  }

}
