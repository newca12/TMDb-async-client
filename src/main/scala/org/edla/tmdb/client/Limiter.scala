package org.edla.tmdb.client

import akka.actor._

import scala.concurrent.duration._

//http://doc.akka.io/docs/akka/snapshot/scala/stream/stream-cookbook.html#Globally_limiting_the_rate_of_a_set_of_streams
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

  private var waitQueue                           = scala.collection.immutable.Queue.empty[ActorRef]
  private var permitTokens                        = maxAvailableTokens
  private var replenishTimer: Option[Cancellable] = None

  override def receive: Receive = open

  val open: Receive = {
    case ReplenishTokens ⇒
      permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
    case WantToPass ⇒
      permitTokens -= 1
      //Attempt to better synchronize first trig with remote server limiter
      if (replenishTimer.isEmpty) {
        replenishTimer = Some(
          system.scheduler
            .schedule(initialDelay = Duration.Zero, interval = tokenRefreshPeriod, receiver = self, ReplenishTokens))
      }
      sender() ! MayPass
      if ((permitTokens == 0)) {
        context.become(closed)
      }
  }

  val closed: Receive = {
    case ReplenishTokens ⇒
      permitTokens = math.min(permitTokens + tokenRefreshAmount, maxAvailableTokens)
      releaseWaiting()
    case WantToPass ⇒
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
    if (replenishTimer.isDefined) replenishTimer.get.cancel()
    waitQueue foreach (_ ! Status.Failure(new IllegalStateException("limiter stopped")))
  }
}
