package eu.dgt.sso.oauth2

import org.keycloak.RSATokenVerifier
import org.keycloak.adapters.KeycloakDeployment

import java.util.concurrent.ForkJoinPool
//scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}
import org.keycloak.representations.AccessToken

class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment) extends TokenVerifier {
  implicit val executionContext = ExecutionContext.fromExecutor(new ForkJoinPool(2))
  
  

  def verifyToken(token: String): Future[AccessToken] = {
    Future {
      RSATokenVerifier.verifyToken(
        token,
        keycloakDeployment.getPublicKeyLocator.getPublicKey("realm-public-key",keycloakDeployment),
        keycloakDeployment.getRealmInfoUrl
      )
    }
  }
}
