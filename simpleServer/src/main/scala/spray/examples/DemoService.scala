package spray.examples

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.TraversableOps")) // Actors infer type Any
class DemoService extends Actor with ActorLogging {
  implicit val timeout: Timeout = 1.second // for the actor 'asks'
  import context.dispatcher // ExecutionContext for the futures and scheduler

  def receive: PartialFunction[Any, Unit] = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! index

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
      val peer: ActorRef    = sender // since the Props creator is executed asyncly we need to save the sender ref
      val result1: ActorRef = context actorOf Props(new Streamer(client = peer, count = 15))

    case HttpRequest(GET, Uri.Path("/server-stats"), _, _, _) =>
      val client: ActorRef = sender
      context.actorSelection("/user/IO-HTTP/listener-0") ? Http.GetStats onSuccess {
        case x: Stats => client ! statsPresentation(x)
      }

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sender ! HttpResponse(
        entity = "About to throw an exception in the request handling actor, " +
        "which triggers an actor restart")
      sys.error("BOOM!")

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path startsWith "/timeout" =>
      log.info("Dropping request, triggering a timeout")

    case HttpRequest(GET, Uri.Path("/stop"), _, _, _) =>
      sender ! HttpResponse(entity = "Shutting down in 1 second ...")
      sender ! Http.Close
      val result2: Cancellable = context.system.scheduler.scheduleOnce(1.second) { context.system.shutdown() }

    case r @ HttpRequest(POST, Uri.Path("/file-upload"), headers, entity: HttpEntity.NonEmpty, protocol) =>
      // emulate chunked behavior for POST requests to this path
      val parts: Stream[HttpMessagePart] = r.asPartStream()
      val client: ActorRef               = sender
      @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf")) // You can use an exhaustive match instead of asInstanceOf
      val handler: ActorRef =
        context.actorOf(Props(new FileUploadHandler(client, parts.head.asInstanceOf[ChunkedRequestStart])))
      import scala.language.postfixOps
      parts.tail.foreach(handler !) // "handler !" is a postfix operator

    case s @ ChunkedRequestStart(HttpRequest(POST, Uri.Path("/file-upload"), _, _, _)) =>
      val client: ActorRef  = sender
      val handler: ActorRef = context.actorOf(Props(new FileUploadHandler(client, s)))
      sender ! RegisterChunkHandler(handler)

    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")

    // This is what we get when we timeout at: http://127.0.0.1:8080/timeout/timeout
    case Timedout(HttpRequest(_, Uri.Path("/timeout/timeout"), _, _, _)) =>
      log.info("Dropping Timeout message")

    // This is what we get when we timeout at: http://127.0.0.1:8080/timeout
    case Timedout(HttpRequest(method, uri, _, _, _)) =>
      sender ! HttpResponse(
        status = 500,
        entity = s"The $method request to '$uri' has timed out..."
      )
  }

  ////////////// helpers //////////////

  lazy val index = HttpResponse(
    entity = HttpEntity(
      `text/html`,
      <html>
        <body>
          <h1>Say hello to <i>spray-can</i>!</h1>
          <p>Defined resources:</p>
          <ul>
            <li><a href="/ping">/ping</a></li>
            <li><a href="/stream">/stream</a></li>
            <li><a href="/server-stats">/server-stats</a></li>
            <li><a href="/crash">/crash</a></li>
            <li><a href="/timeout">/timeout</a></li>
            <li><a href="/timeout/timeout">/timeout/timeout</a></li>
            <li><a href="/stop">/stop</a></li>
          </ul>
          <p>Test file upload</p>
          <form action ="/file-upload" enctype="multipart/form-data" method="post">
            <input type="file" name="datafile" multiple=""></input>
            <br/>
            <input type="submit">Submit</input>
          </form>
        </body>
      </html>.toString()
    )
  )

  def statsPresentation(s: Stats): HttpResponse = HttpResponse(
    entity = HttpEntity(
      `text/html`,
      <html>
        <body>
          <h1>HttpServer Stats</h1>
          <table>
            <tr><td>uptime:</td><td>{s.uptime.formatHMS}</td></tr>
            <tr><td>totalRequests:</td><td>{s.totalRequests}</td></tr>
            <tr><td>openRequests:</td><td>{s.openRequests}</td></tr>
            <tr><td>maxOpenRequests:</td><td>{s.maxOpenRequests}</td></tr>
            <tr><td>totalConnections:</td><td>{s.totalConnections}</td></tr>
            <tr><td>openConnections:</td><td>{s.openConnections}</td></tr>
            <tr><td>maxOpenConnections:</td><td>{s.maxOpenConnections}</td></tr>
            <tr><td>requestTimeouts:</td><td>{s.requestTimeouts}</td></tr>
          </table>
        </body>
      </html>.toString()
    )
  )

  class Streamer(client: ActorRef, count: Int) extends Actor with ActorLogging {
    log.debug("Starting streaming response ...")

    // we use the successful sending of a chunk as trigger for scheduling the next chunk
    client ! ChunkedResponseStart(HttpResponse(entity = " " * 2048)).withAck(Ok(count))

    def receive: PartialFunction[Any, Unit] = {
      case Ok(0) =>
        log.info("Finalizing response stream ...")
        client ! MessageChunk("\nStopped...")
        client ! ChunkedMessageEnd
        context.stop(self)

      case Ok(remaining) =>
        log.info("Sending response chunk ...")
        val result3: Cancellable = context.system.scheduler.scheduleOnce(100 millis span) {
          client ! MessageChunk(DateTime.now.toIsoDateTimeString + ", \n").withAck(Ok(remaining - 1))
        }

      case x: Http.ConnectionClosed =>
        log.info("Canceling response stream due to {} ...", x)
        context.stop(self)
    }

    // simple case class whose instances we use as send confirmation message for streaming chunks
    case class Ok(remaining: Int)
  }
}
