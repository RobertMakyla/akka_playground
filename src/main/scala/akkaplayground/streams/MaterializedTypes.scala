package akkaplayground.streams

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.Future

object MaterializedTypes {

  implicit val system = ActorSystem()
  import system.dispatcher // instead of import scala.concurrent.ExecutionContext.Implicits.global

  val source: Source[Int, NotUsed] = Source(1 to 3) //Materialized type: NotUsed
  val sinkPrinter: Sink[Any, Future[Done]] = Sink.foreach(println) //Materialized type: Future[Done]
  val sinkAdder: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)((acc, next) => acc + next) //Materialized type: Future[Int]

  //SCAN: for each element it applies fold from zero element to the current one
  // no.1 = 1       = 1
  // no.2 = 1*1     = 1
  // no.3 = 1*1*2   = 2
  // no.4 = 1*1*2*3 = 6
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)//no.2 = 1*2  no.3 = 1*2*3 ...
  val reduced: Source[Int, NotUsed] = source.reduce(_ + _) // Similar to fold but uses first element as zero element

  def main( args: Array[String]):Unit = {

    // At the end I want to have the Materialized value : Future[Done] to know when it completes
    // toMat()    - to return materialized value
    // Keep.right - because I want to get most right materialized value (not the NotUsed)
    val futDone: Future[Done] = source.toMat(sinkPrinter)(Keep.right).run()
    futDone.onComplete(_ => println("done printing ..."))

    Thread.sleep(1000)

    // shorter way
    val futDone1: Future[Done] = factorials.runWith(Sink.foreach(println)) // shortcut for source.toMat(sink)(Keep.right).run()
    futDone1.onComplete(_ => println("done printing (factorials)..."))

    Thread.sleep(1000)

    // even shorter way
    val futDone2: Future[Done] = factorials.runForeach(println) // shortcut for running source on a Sink.foreach()
    futDone2.onComplete(_ => println("done printing (factorials)..."))

    Thread.sleep(1000)

    // reduced
    val futDone3: Future[Done] = reduced.runForeach(println) // shortcut for running source on a Sink.foreach()
    futDone3.onComplete(_ => println("done printing (reduced)..."))

    Thread.sleep(1000)

    // summing up
    val futInt: Future[Int] = source.toMat(sinkAdder)(Keep.right).run()
    futInt.onComplete(sum => println(s"done adding = $sum"))

    //todo https://doc.akka.io/docs/akka/current/stream/stream-quickstart.html#flattening-sequences-in-streams
  }
}

