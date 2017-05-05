package org.edla.tmdb

import scala.concurrent.duration._

package object client {
  val RequestRateLimitMax   = 40
  val RequestRateLimitDelay = 10 seconds
  val Port                  = 80
}
