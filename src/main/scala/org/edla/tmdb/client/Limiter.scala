package org.edla.tmdb.client

//import acyclic.file
import akka.actor._

import scala.concurrent.duration._

//http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0-M1/scala/stream-cookbook.html#Globally_limiting_the_rate_of_a_set_of_streams
object Limiter {
  implicit val system = ActorSystem()

  case object WantToPass
  case object MayPass
  case object ReplenishTokens

  def props(
      maxAvailableTokens: Int,
      tokenRefreshPeriod: FiniteDuration,
      tokenRefreshAmount: Int
  ): Props =
    Props(new Limiter(maxAvailableTokens, tokenRefreshPeriod, tokenRefreshAmount))
}

class Limiter(
    val maxAvailableTokens: Int,
    val tokenRefreshPeriod: FiniteDuration,
    val tokenRefreshAmount: Int
) extends Actor {
  import Limiter._
  import akka.actor.Status
  import context.dispatcher

  private var waitQueue    = scala.collection.immutable.Queue.empty[ActorRef]
  private var permitTokens = maxAvailableTokens
  private val replenishTimer = system.scheduler.schedule(
    initialDelay = tokenRefreshPeriod,
    interval = tokenRefreshPeriod,
    receiver = self,
    ReplenishTokens
  )

  override def receive: Receive = open

  val open: Receive = {
    case ReplenishTokens =>
      permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
    case WantToPass =>
      permitTokens -= 1
      sender() ! MayPass
      if (permitTokens == 0) context.become(closed)
  }

  val closed: Receive = {
    case ReplenishTokens =>
      permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
      releaseWaiting()
    case WantToPass =>
      waitQueue = waitQueue.enqueue(sender())
  }

  private def releaseWaiting(): Unit = {
    val (toBeReleased, remainingQueue) = waitQueue.splitAt(permitTokens)
    waitQueue = remainingQueue
    permitTokens -= toBeReleased.size
    toBeReleased foreach (_ ! MayPass)
    if (permitTokens > 0) context.become(open)
  }

  override def postStop(): Unit = {
    replenishTimer.cancel()
    waitQueue foreach (_ ! Status.Failure(new IllegalStateException("limiter stopped")))
  }
}
