package akkaplayground.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

/**
 * 2 players - each one of then is sending messages to himself
 */
object AsyncActor {

  case class Msg(iteration: Int, actorName: String)

  def player: Behavior[Msg] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Msg(i, actorName) =>
        ctx.log.info(s"${i} at ${actorName}")
        ctx.self ! Msg(i + 1, actorName)
        Behaviors.same
    }
  }

  val main: Behavior[Any] = Behaviors.setup { ctx => // All this will be done when actor is created, not when message is received
    /*
      I can ONLY create children within the top-main actor
     */
    val player1 = ctx.spawn(player, "Mike")
    val player2 = ctx.spawn(player, "John")
    player1 ! Msg(0, "Mike")
    player2 ! Msg(0, "John")
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {

    val mainRef = ActorSystem(main, "main")
    Thread.sleep(10)
    mainRef.terminate()
  }
}
