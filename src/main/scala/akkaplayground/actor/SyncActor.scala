package akkaplayground.actor

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
 * 2 players - each one of then is sending messages to another
 */
object SyncActor {

  trait PingPong
  case class Ping(iteration: Int = 0, replyTo: ActorRef[PingPong]) extends PingPong
  case class Pong(iteration: Int = 0, replyTo: ActorRef[PingPong]) extends PingPong

  val delayMillis = 100

  def player: Behavior[PingPong] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Ping(i, replyTo) =>
        ctx.log.info(s" -- ping --> ${i}")
        Thread.sleep(delayMillis)
        replyTo ! Pong(i + 1, ctx.self)
        Behaviors.same
      case Pong(i, replyTo) =>
        ctx.log.info(s" <-- pong -- ${i}")
        Thread.sleep(delayMillis)
        replyTo ! Ping(i + 1, ctx.self)
        Behaviors.same
    }
  }

  val main: Behavior[Any] = Behaviors.setup { ctx => // All this will be done when actor is created, not when message is received
    val player1 = ctx.spawn(player, "player-1")
    val player2 = ctx.spawn(player, "player-2")
    player1 ! Ping(0, player2)
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {

    val mainRef = ActorSystem(main, "main")
    Thread.sleep(1000)
    mainRef.terminate()
  }
}
