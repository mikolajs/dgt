package eu.dgt.sso.oauth2

import scala.concurrent.Future
import org.keycloak.representations.AccessToken

trait TokenVerifier {
  def verifyToken(token: String): Future[AccessToken]
}

