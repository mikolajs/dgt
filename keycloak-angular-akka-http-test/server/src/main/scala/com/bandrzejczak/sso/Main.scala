package com.bandrzejczak.sso

import akka.actor.ActorSystem
import akka.actor.FSM.LogEntry
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.bandrzejczak.sso.oauth2.{KeycloakTokenVerifier, OAuth2Authorization, OAuth2Token}
import org.keycloak.adapters.KeycloakDeploymentBuilder
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.io.StdIn

object Main extends App with Jsonp with JsonProtocol {
  implicit val system = ActorSystem("apiserver-system")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val oauth2 = new OAuth2Authorization(
    Logging(system, getClass),
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  import oauth2._

  val route1 = (authorized { token =>
    path("test") {
      get {
        jsonpWithParameter("callback") {
          complete(token)
        }
      }
    }
  })
  val route2 =
    path("webadmin") {
      get {
        complete("WEBADMIN")
      }
  }


  val bindingFuture = Http().bindAndHandle(route1 ~ route2, "localhost", 9000)

  println("bind on port 9000")

  StdIn.readLine()
  bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done


}

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit def OAuth2TokenFormat: RootJsonFormat[OAuth2Token] = jsonFormat2(OAuth2Token)
}
