package akkaplayground.actor

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, javadsl, scaladsl}
import akka.actor.typed.scaladsl.Behaviors

/**
 * 2 players - each one of then is sending messages to another
 */
object SyncActor {

  sealed trait PingPong
  case class Ping(i:Int=0, replyTo: ActorRef[PingPong]) extends PingPong
  case class Pong(i:Int=0, replyTo: ActorRef[PingPong]) extends PingPong

  def player: Behavior[PingPong] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Ping(i, repl) =>
        ctx.log.info(s"ping ==> ${i}")
        repl ! Pong(i + 1, ctx.self)
      case Pong(i, repl) =>
        ctx.log.info(s"${i} <== pong")
        repl ! Ping(i + 1, ctx.self)
    }
    Behaviors.same
  }

  def guardian: Behavior[Any] = Behaviors.setup{ctx =>
    val p1 = ctx.spawn(player, "player1")
    val p2 = ctx.spawn(player, "player2")
    p1 ! Ping(0, p1)
    p2 ! Ping(100000, p2)
    Behaviors.same
  }
  def main(args: Array[String]): Unit = {
    val as = ActorSystem(guardian, "main")
    Thread.sleep(100)
    as.terminate()
  }
//  trait PingPong
//  case class Ping(iteration: Int = 0, replyTo: ActorRef[PingPong]) extends PingPong
//  case class Pong(iteration: Int = 0, replyTo: ActorRef[PingPong]) extends PingPong
//
//  val delayMillis = 100
//
//  def player: Behavior[PingPong] = Behaviors.receive { (ctx, msg) =>
//    msg match {
//      case Ping(i, repl) =>
//        ctx.log.info(s"===> Ping ($i)")
//        repl ! Pong(i + 1, ctx.self)
//      case Pong(i, repl) =>
//        ctx.log.info(s"<=== Pong ($i)")
//        repl ! Ping(i + 1, ctx.self)
//    }
//    Behaviors.same
//  }
//
//  def guardian: Behavior[Any] = Behaviors.setup { ctx => // All this will be done when actor is created, not when message is received
//    /*
//      I can ONLY create children within the top-main actor
//     */
//    val p1 = ctx.spawn(player, "player_1")
//    val p2 = ctx.spawn(player, "player_2")
//    p1 ! Ping(0, p2)
//    Behaviors.same
//  }
//
//  def main(args: Array[String]): Unit = {
//    val system = ActorSystem(guardian, "main")
//    Thread.sleep(500)
//    system.terminate()
//  }
}
