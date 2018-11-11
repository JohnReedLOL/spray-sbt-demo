package spray.examples

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.io.IO
import spray.can.Http

object Main extends App with MySslConfiguration {

  implicit val system: ActorSystem = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler: ActorRef = system.actorOf(Props[DemoService], name = "handler")

  IO(Http) ! Http.Bind(listener = handler, interface = "localhost", port = 8080)
}
