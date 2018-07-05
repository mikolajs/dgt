package eu.dgt.sso

import akka.actor.ActorSystem
import akka.actor.FSM.LogEntry
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.ActorMaterializer
import eu.dgt.sso.oauth2.{ KeycloakTokenVerifier, OAuth2Authorization, OAuth2Token }
import org.keycloak.adapters.KeycloakDeploymentBuilder
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

import scala.io.StdIn
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.server.AuthorizationFailedRejection
import java.nio.file.Files
import java.nio.file.Paths
import akka.stream.scaladsl.Source
import akka.http.scaladsl.server.PathMatchers
import org.apache.http.HttpHeaders

object Main extends App with Jsonp with JsonProtocol {
  implicit val system = ActorSystem("apiserver-system")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val oauth2 = new OAuth2Authorization(
    Logging(system, getClass),
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json"))))

  import oauth2._
  val allOrginHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "http://localhost:8000"),
    RawHeader("Access-Control-Allow-Credentials", true.toString),
    RawHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS"),
    RawHeader("Access-Control-Allow-Headers", "Authorization, Content-Type"),
    RawHeader("Access-Control-Max-Age", "86400"))

  val myHandler = RejectionHandler.newBuilder()
    .handle {
      case AuthorizationFailedRejection =>
        respondWithHeaders(allOrginHeaders: _*) {
          complete((Forbidden, "Authorization Failed "))
        }
    }
    .handleAll[MethodRejection] { methodRejections =>
      val names = methodRejections.map(_.supported.name)
      respondWithHeaders(allOrginHeaders: _*) {
        complete(s"Can't do that! Supported: ${names mkString " or "}!")
      }
    }
    .result()

  val route1 = handleRejections(myHandler) {
    authorized { token =>
      path("test") {
        get {
          respondWithHeaders(allOrginHeaders: _*) {
            complete("ACCEPTED")
          }
        }

      }
    }
  }
  val route2 =
    path("webadmin") {
      get {
        respondWithHeaders(allOrginHeaders: _*) {
          complete("WEBADMIN")
        }
      }
    }
  val route3 = options {
    respondWithHeaders(allOrginHeaders: _*) {
      complete("")
    }
  }

  val bindingFuture = Http().bindAndHandle(route1, "localhost", 9000)

  println("bind on port 9000")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

object StaticRoutes {

  val workingDirectory = "/home/ms/Programy/dgt/keycloak-angular-akka-http-test/client/akkaServ/plain"
  //System.getProperty("user.dir") + "/plain"

  private def getExtensions(fileName: String): String = {

    val index = fileName.lastIndexOf('.')
    if (index != 0) {
      fileName.drop(index + 1)
    } else
      ""
  }

  private def getDefaultPage = {
    val fullPath = List(Paths.get(workingDirectory + "/index.html"))
    val res = fullPath.filter(x => Files.exists(x))
    if (!res.isEmpty)
      res.head
    else
      Paths.get("")
  }

  def generate = {
    logRequestResult("nallo-micro-http-server") {
      get {
        pathPrefix("plain") {
          path(PathEnd) {
            complete {
              HttpResponse(NotFound)
                .withHeaders(RawHeader("Access-Control-Allow-Origin", "*"))
            }
          } ~
            path(Segment) { file: String =>

              complete {
                val fullPath = file match {
                  case "" => getDefaultPage
                  case _  => Paths.get(workingDirectory + "/" + file)
                }

                val ext = getExtensions(fullPath.getFileName.toString)
                val mediaTyp = ext match {
                  case "html" => MediaTypes.`text/html`
                  case "js"   => MediaTypes.`application/javascript`
                  case _      => MediaTypes.`text/plain`
                }
                val c: ContentType = mediaTyp.withCharset(HttpCharsets.`UTF-8`)
                val byteArray = akka.util.ByteString(Files.readAllBytes(fullPath))

                HttpResponse(OK, entity = HttpEntity.Default(c, byteArray.length, Source.single(byteArray)))
                  .withHeaders(RawHeader("Access-Control-Allow-Origin", "*"))
              }

            }
        }
      }
    }
  }

}

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit def OAuth2TokenFormat: RootJsonFormat[OAuth2Token] = jsonFormat2(OAuth2Token)
}
