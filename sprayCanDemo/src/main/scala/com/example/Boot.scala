package com.example

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.{ TraceLogger, TraceLogging }
import spray.can.Http

import scala.concurrent.Future
import scala.concurrent.duration._

@SuppressWarnings(Array("org.wartremover.warts.Any")) // Actors infer type Any
object Boot extends App with TraceLogging {

  // we need an ActorSystem to host our application in
  implicit val system: ActorSystem = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout: Timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  val result: Future[Any]   = IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
  val myLogger: TraceLogger = logger
  myLogger.info(s"result: $result")
  myLogger.error("This is a test log!!!")
}
