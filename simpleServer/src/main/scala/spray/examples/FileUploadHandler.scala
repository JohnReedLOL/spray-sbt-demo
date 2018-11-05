package spray.examples

import akka.actor._

import scala.concurrent.duration._
import java.io.{File, FileInputStream, FileOutputStream, InputStream}

import org.jvnet.mimepull.{MIMEMessage, MIMEPart}
import spray.http._
import MediaTypes._
import HttpHeaders._
import parser.HttpParser
import HttpHeaders.RawHeader
import spray.io.CommandWrapper

import scala.annotation.tailrec
import scala.collection.mutable

@SuppressWarnings(Array("org.wartremover.warts.Any")) // Actors infer type Any
class FileUploadHandler(client: ActorRef, start: ChunkedRequestStart) extends Actor with ActorLogging {
  import start.request._
  client ! CommandWrapper(SetRequestTimeout(Duration.Inf)) // cancel timeout

  val tmpFile: File = File.createTempFile("chunked-receiver", ".tmp", new File("/tmp"))
  tmpFile.deleteOnExit()
  val output = new FileOutputStream(tmpFile)
  val Some(HttpHeaders.`Content-Type`(ContentType(multipart: MultipartMediaType, _))) = header[HttpHeaders.`Content-Type`]
  val boundary = multipart.parameters("boundary")

  log.info(s"Got start of chunked request $method $uri with multipart boundary '$boundary' writing to $tmpFile")
  @SuppressWarnings(Array("org.wartremover.warts.Var")) // Generally avoid shared mutable state
  var bytesWritten = 0L

  def receive: PartialFunction[Any, Unit] = {
    case c: MessageChunk =>
      log.debug(s"Got ${c.data.length} bytes of chunked request $method $uri")

      output.write(c.data.toByteArray)
      bytesWritten += c.data.length

    case e: ChunkedMessageEnd =>
      log.info(s"Got end of chunked request $method $uri")
      output.close()

      client ! HttpResponse(status = 200, entity = renderResult())
      client ! CommandWrapper(SetRequestTimeout(2.seconds)) // reset timeout to original value
      val deleteResult: Boolean = tmpFile.delete()
      context.stop(self)
  }

  import collection.JavaConverters._
  def renderResult(): HttpEntity = {
    val message = new MIMEMessage(new FileInputStream(tmpFile), boundary)
    // caution: the next line will read the complete file regardless of its size
    // In the end the mime pull parser is not a decent way of parsing multipart attachments
    // properly
    val parts: mutable.Buffer[MIMEPart] = message.getAttachments.asScala

    HttpEntity(`text/html`,
      <html>
        <body>
          <p>Got {bytesWritten} bytes</p>
          <h3>Parts</h3>
          {
            parts.map { part =>
              val name: String = fileNameForPart(part).getOrElse("<unknown>")
              <div>{name}: {part.getContentType} of size {sizeOf(part.readOnce())}</div>
            }
          }
        </body>
      </html>.toString()
    )
  }
  def fileNameForPart(part: MIMEPart): Option[String] =
    for {
      dispHeader <- part.getHeader("Content-Disposition").asScala.headOption
      // Variable disp is never used
      Right(disp: `Content-Disposition`) = HttpParser.parseHeader(RawHeader("Content-Disposition", dispHeader))
      name <- disp.parameters.get("filename")
    } yield name

  def sizeOf(is: InputStream): Long = {
    val buffer = new Array[Byte](_length = 65000)

    @tailrec def inner(cur: Long): Long = {
      val read: Int = is.read(buffer)
      if (read > 0) {
        inner(cur + read)
      }
      else {
        cur
      }
    }

    inner(0)
  }
}
