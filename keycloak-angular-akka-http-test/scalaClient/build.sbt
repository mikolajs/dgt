name := "keycloak-scalaClient"

version := "1.0"

scalaVersion := "2.12.6"

val keyCloakV = "4.0.0.Final"


val keycloak = Seq (
   "org.keycloak" % "keycloak-authz-client" % keyCloakV,
  // we include all necessary transitive dependencies,
  // because they're marked provided in keycloak pom.xml
  "org.keycloak" % "keycloak-core" % keyCloakV,
)

libraryDependencies ++= keycloak
