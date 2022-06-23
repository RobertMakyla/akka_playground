package akkaplayground.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object StatelessActor {

  trait StateChanger

  case class Increase(i: Int) extends StateChanger

  case class Decrease(i: Int) extends StateChanger

  def myActor(state: Int = 0): Behavior[StateChanger] = Behaviors.receive {
    (context, msg) => msg match {
      case Increase(i) =>
        context.log.info(s"Increasing state ${state}")
        myActor(state + i)
      case Decrease(i) =>
        context.log.info(s"Decreased state ${state}")
        myActor(state - i)
    }
  }


  def main(args: Array[String]) = {

    val actorRef: ActorSystem[StateChanger] = ActorSystem(myActor(), "main")

    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Decrease(300)

    Thread.sleep(1000)

    actorRef.terminate()
  }
}
