package eu.dgt.sso.oauth2

import org.keycloak.RSATokenVerifier
import org.keycloak.adapters.KeycloakDeployment

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment) extends TokenVerifier {
  implicit val executionContext = ExecutionContext.fromExecutor(new ForkJoinPool(2))

  def verifyToken(token: String): Future[String] = {
    Future {
      RSATokenVerifier.verifyToken(
        token,
        keycloakDeployment.getPublicKeyLocator.getPublicKey("realm-public-key",keycloakDeployment),
        keycloakDeployment.getRealmInfoUrl
      ).getPreferredUsername
    }
  }
}
