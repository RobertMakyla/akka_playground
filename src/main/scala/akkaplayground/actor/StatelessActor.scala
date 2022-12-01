package akkaplayground.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object StatelessActor {

  trait StateChanger

  case class Increase(i: Int) extends StateChanger

  case class Decrease(i: Int) extends StateChanger

  def myBehavior(state: Int): Behavior[StateChanger] = Behaviors.receive{ (ctx, msg) => msg match {
    case Increase(i) =>
      val newState = state + i
      ctx.log.info(s"increase by ${i} gives ${newState} ")
      myBehavior(newState)
    case Decrease(i) =>
      val newState = state - i
      ctx.log.info(s"decrease by ${i} gives ${newState} ")
      myBehavior(newState)
  }}


  def main(args: Array[String]) = {

    val actorRef: ActorSystem[StateChanger] = ActorSystem(myBehavior(0), "main")

    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Decrease(300)

    Thread.sleep(1000)

    actorRef.terminate()
  }
}
