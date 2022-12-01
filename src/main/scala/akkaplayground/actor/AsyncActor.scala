package akkaplayground.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

/**
 * 2 players - each one of then is sending messages to himself
 */
object AsyncActor {

  case class Msg(iteration: Int, actorName: String)

  def player: Behavior[Msg] = Behaviors.receive { (ctx, msg: Msg) =>
    ctx.log.info(s"hello, ${msg.actorName}}")
    ctx.self.tell(msg.copy(iteration = msg.iteration + 1))
    Behaviors.same
  }


  val guardianActor : Behavior[Unit] = Behaviors.setup { ctx =>
    val playerA = ctx.spawn(player, "playerA")
    val playerB = ctx.spawn(player, "playerB")

    playerA ! Msg(0, "actor_A")
    playerB ! Msg(0, "actor_B")
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    val as = ActorSystem(guardianActor, "main")
    Thread.sleep(20)
    as.terminate()
  }
}
