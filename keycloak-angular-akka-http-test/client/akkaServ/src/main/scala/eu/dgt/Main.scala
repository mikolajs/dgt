import akka.actor.{ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer}
import com.typesafe.config.ConfigFactory
import java.nio.file.{Files, Paths}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.scaladsl.Source
import scala.io.StdIn
import akka.http.scaladsl.model.headers.RawHeader

object routes {

  val workingDirectory = System.getProperty("user.dir") + "/plain"


  private def getExtensions(fileName: String) : String = {

    val index = fileName.lastIndexOf('.')
    if(index != 0) {
      fileName.drop(index+1)
    }else
      ""
  }

  private def getDefaultPage ={
    val fullPath = List(Paths.get(workingDirectory + "/index.html"))
    val res = fullPath.filter(x => Files.exists(x))
    if(!res.isEmpty)
      res.head
    else
      Paths.get("")
  }

  def generate = {
    logRequestResult("nallo-micro-http-server") {
      get {
        entity(as[HttpRequest]) { requestData =>
          complete {       
            val fullPath = requestData.uri.path.toString match {
              case "/"=> getDefaultPage
              case "" => getDefaultPage
              case _ => Paths.get(workingDirectory +  requestData.uri.path.toString)
            }

            val ext = getExtensions(fullPath.getFileName.toString)
            val mediaTyp = ext match {
              case "html" => MediaTypes.`text/html`
              case "js" => MediaTypes.`application/javascript`
              case _ => MediaTypes.`text/plain`
            }
            val c : ContentType = mediaTyp.withCharset(HttpCharsets.`UTF-8`)
            val byteArray = akka.util.ByteString(Files.readAllBytes(fullPath))
            
            HttpResponse(OK, entity = HttpEntity.Default(c, byteArray.length, Source.single(byteArray) ))
            .withHeaders(RawHeader("Access-Control-Allow-Origin", "*"))
          }
        }
      }
    }
  }
}


object Main extends App {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val bindingFuture = Http().bindAndHandle(routes.generate, "localhost", 8000)
  StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  
}
