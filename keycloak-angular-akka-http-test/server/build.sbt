name := "keycloak-akka-http"

version := "1.0"

scalaVersion := "2.12.6"

val akkaHttp= "10.1.3"
val akkaStream = "2.5.12"
val keyCloakV = "4.0.0.Final"

val akka = Seq (
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe.akka" %% "akka-stream" % akkaStream,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp
)

val logging = Seq (
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
 "com.typesafe.akka" %% "akka-slf4j" % akkaStream
)

val keycloak = Seq (
   "org.keycloak" % "keycloak-adapter-core" % keyCloakV,
  // we include all necessary transitive dependencies,
  // because they're marked provided in keycloak pom.xml
  "org.keycloak" % "keycloak-core" % keyCloakV,
  "org.jboss.logging" % "jboss-logging" % "3.3.1.Final",
 "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)

libraryDependencies ++= akka ++ logging ++ keycloak
