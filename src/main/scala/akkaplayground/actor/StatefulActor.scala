package akkaplayground.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

object StatefulActor {

  trait StateChanger

  case class Increase(i: Int) extends StateChanger

  case class Decrease(i: Int) extends StateChanger

  val myActor: Behavior[StateChanger] = Behaviors.setup { context =>
    var state: Int = 0
    Behaviors.receiveMessage {
      case Increase(i) =>
        context.log.info(s"Increasing state ${state}")
        state += i
        Behaviors.same
      case Decrease(i) =>
        context.log.info(s"Decreased state ${state}")
        state -= i
        Behaviors.same
    }
  }


  def main(args: Array[String]) = {

    val actorRef: ActorSystem[StateChanger] = ActorSystem(myActor, "main")

    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Increase(100)
    actorRef ! Decrease(300)

    Thread.sleep(1000)

    actorRef.terminate()
  }
}
