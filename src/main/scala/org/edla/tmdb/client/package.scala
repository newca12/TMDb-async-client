package org.edla.tmdb

import scala.concurrent.duration._

package object client {
  val RequestRateLimitDelay: FiniteDuration = 11 seconds
  val Port: Int                             = 80
}
