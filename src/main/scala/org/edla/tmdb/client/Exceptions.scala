package org.edla.tmdb.client

//http://stackoverflow.com/questions/10925268/define-your-own-exceptions-with-overloaded-constructors-in-scala
class TmdbException(message: String = null, cause: Throwable = null, val code: Long) extends RuntimeException(message, cause)

class InvalidApiKeyException(message: String = null, cause: Throwable = null, code: Long) extends TmdbException(message, cause, code)

object TmdbException {
  def apply(message: String = null, cause: Throwable = null, code: Long) = new TmdbException(message, cause, code)
}