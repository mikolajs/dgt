package eu.dgt

import org.keycloak.authorization.client.AuthzClient
import com.softwaremill.sttp._

object Main extends App {
  val authzClient = AuthzClient.create();
  println("=================== Start =============")
  val config = authzClient.getConfiguration

  println(s""" Server: ${config.getAuthServerUrl} 
       |  Realm ${config.getRealm} 
       |  Credentials ${config.getCredentials}
       | Resource ${config.getResource}
       |  ${config.getTokenStore}
        \n""")
  val token = authzClient.obtainAccessToken()
  println(s"""TOKEN:  
    | expires: ${token.getExpiresIn}
    | token: ${token.getToken}
    | refresh token: ${token.getRefreshToken}""")

  val query = "http language:scala"
  val oldToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0RzUyU3hPWXFacHdXSkdybTUtTT"+
  "QySWdoSFd1ZTY2S0hNQ1V3aDdKeW1vIn0.eyJqdGkiOiIwYzhiZWNhMi03MzE4LTQ5MjktYTFmMi1jYmU3ZmM1MmE4NWMiL"+
  "CJleHAiOjE1MzA3ODU3MTgsIm5iZiI6MCwiaWF0IjoxNTMwNzg1NDE4LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV"+
  "0aC9yZWFsbXMvYXBpc2VydmVyIiwiYXVkIjoiYXBpY2xpZW50Iiwic3ViIjoiZDgzYjM3ZjEtYTg2OS00MGJiLTlkMzktMjMwOD"+
  "dlOWZmMmJiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYXBpY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiODk"+
  "yNGNhZmEtYjBiNC00MGU2LWI3MzctYjM5NDFmY2VhNmM3IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJsb2NhbGhvc3Q6OD"+
  "AwMCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3Vy"+
  "Y2VfYWNjZXNzIjp7ImFwaWNsaWVudCI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW"+
  "5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIi"+
  "wiY2xpZW50SG9zdCI6IjE3Mi4xNy4wLjEiLCJjbGllbnRJZCI6ImFwaWNsaWVudCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVy"+
  "cmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWFwaWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxNzIuMTcuMC4xIiwiZW1haWwiOiJzZ"+
  "XJ2aWNlLWFjY291bnQtYXBpY2xpZW50QHBsYWNlaG9sZGVyLm9yZyJ9.Q2vnJUoJAP8eUuwCinlGRH0Fv0gWms6kxMHdnqkQwcThJuYkz5ih4"+
  "FfXAm-iuTzbahwOitywPSVLFqXCHLp4jPx1-wmXub7I_ZFyJOCtbdrXblPgPP39xUpvgG-4V-x0JWvqx_Nrq3AC7wL2kCOVdfuWZbCkUOs7be"+
  "hi4KFRr35i1pnC6Nl5t_Smmu10-5vwhcCm9HKJCYyCYFrpwQ-aU4GIoX_YbIokhF30P-PFBElaBnb2xq4hIk_UqV8Qu6hyx3lFGiVcTYmz2seC"+
  "KaY9Z9U_R7vIgj7_Vij7KS0RU1PHMwjpZqXctDdMZYKFFRCbchYvD3mqPlBSWIkOzGN1MQ"
  val request = sttp.cookie("X-Authorization-Token", oldToken)
  .get(uri"http://localhost:9000/test?callback=JSON_CALLBACK")
  //.header("Authorization", "Bearer " + token.getToken, true)

  implicit val backend = HttpURLConnectionBackend()
  val response = request.send()
  response.body match {
    case Right(s) => println("Right: " + s)
    case Left(s) => println("LEFT: " + s)
  }
  
  val request2 = sttp.cookie("X-Authorization-Token", token.getToken)
  .get(uri"http://localhost:9000/test?callback=JSON_CALLBACK")
  //.header("Authorization", "Bearer " + token.getToken, true)
  val response2 = request2.send()
  response2.body match {
    case Right(s) => println("Right: " + s)
    case Left(s) => println("LEFT: " + s)
  }

}