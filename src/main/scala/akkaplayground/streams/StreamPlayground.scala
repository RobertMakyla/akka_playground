package akkaplayground.streams

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object StreamPlayground {

  implicit val system = ActorSystem(Behaviors.empty, "main")

  /**
   * Simple example
   */
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 10)
  val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  val simpleGrapf = source.via(flow).to(sink)

  /**
   * FUSION
   *
   * when we use 'via' and 'to', by default Akka-Streams uses the same actor (Fusion) because the  majority of processign cases are fast so we don't want to create unnecessary network traffic
   * And an Actor processes messages sequentially so no back pressure here:
   *
   * Actual Result here:
   * [flow] 1
   * [sink] 1
   * [flow] 2
   * [sink] 2
   * [flow] 3
   * [sink] 3
   */
  val debuggingFlow = Flow[Int].map { x => println(s"[flow] $x"); x }
  val slowSink = Sink.foreach[Int] { x => Thread.sleep(1000); println(s"[sink] $x") }
  val noBackPressureGrapf = source.via(debuggingFlow).to(slowSink)

  /**
   * BACKPRESSURE
   *
   * If any of the components are slow, we don't want to use the same actor by default to actually have the backpressure. So we add: async:
   *
   * source.via(flow).async.to(slowSink)   means that source and flow are on one actor  and  slowSink on another one.
   *
   * Actual Result here:
   * [flow] 1
   * [flow] 2
   *  ...
   * [flow] 16  (16 elements) slowSink forces debuggingFlow to buffer 16 elements (16 = default buffer size), now when flow's buffer gets full, it signals backpressure to source to stop sending the data
   *
   * [sink] 1 ( slow sink consuming)
   * ...      ( slow sink consuming)
   * [sink] 7 ( slow sink consuming) flow is noticing that half of it's buffer is empty so it signals source to resume sending the data (half of the buffer = 8 elements)
   *
   * [flow] 16 (start sending half of flow's buffer)
   * ...
   * [flow] 24 (stop sending half of flow's buffer - flow's buffer is full)
   *
   *
   * To summ-up: first the source sends 16 elements to fill the last buffer before a separate actor, then slowSink starts slow consumption,
   *             and once half of flow's buffer is consumed by the sink, the flow tells the source to send a fresh portion of 8 elements. And so on...
   */

  val backPressureGrapf = source.via(debuggingFlow).async.to(slowSink)

  /**
   * Buffer Default configuration = BACKPRESSURE, buffer size 16
   */
  val backPressureDefaultConfig = source.via(debuggingFlow.buffer(16, OverflowStrategy.backpressure)).async.to(slowSink)

  /**
   * Buffer custom configuration
   */

  // If I cannot slow down the source , I might want to start loosing the data

  // drop head   - If the buffer is full when a new element arrives, drops the oldest element from the buffer to make space for the new element.
  // drop tail   - If the buffer is full when a new element arrives, drops the youngest element from the buffer to make space for the new element.
  // drop buffer - If the buffer is full when a new element arrives, drops all the buffered elements to make space for the new element.
  // drop new    - If the buffer is full when a new element arrives, drops the new element.
  val dropHeadGraph = source.via(debuggingFlow.buffer(10, OverflowStrategy.dropHead)).async.to(slowSink)


  def main(args: Array[String]): Unit = {
//    simpleGrapf.run()
//    noBackPressureGrapf.run()
//    backPressureGrapf.run()
    dropHeadGraph.run()
  }

}
