package org.edla.tmdb

import scala.concurrent.duration._

package object client {
  val RequestRateLimitMax   = 38
  val RequestRateLimitDelay = 11 seconds
  val Port                  = 80
}
