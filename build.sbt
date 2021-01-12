import scala.sys.process.Process

Global / semanticdbEnabled := true
Global / semanticdbVersion := "4.4.0"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "it.pagopa"
ThisBuild / organizationName := "Pagopa S.p.A."
ThisBuild / wartremoverErrors ++= Warts.unsafe
ThisBuild / libraryDependencies ++= Dependencies.Jars.`server`
ThisBuild / parallelExecution in Test := false

Docker / packageName := s"services/${name.value}"
Docker / daemonUser := "daemon"
Docker / dockerRepository := Some(System.getenv("DOCKER_REPO"))
Docker / version := s"${(version in ThisBuild).value}-${Process("git log -n 1 --pretty=format:%h").lineStream.head}"
Docker / dockerExposedPorts := Seq(8080)
Docker / dockerBaseImage := "openjdk:8-jre-alpine"
Docker / dockerUpdateLatest := true

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Xlint:_,-type-parameter-shadow",
  "-Xfatal-warnings",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:patvars,-implicits",
  "-Ywarn-value-discard"
)

lazy val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

generateCode := {
  import sys.process._
  val output = Process(
    s"""openapi-generator generate -t template/scala-akka-http-server
      |                           -i src/main/resources/interface-specification.yml
      |                           -g scala-akka-http-server
      |                           -p projectName=${name.value}
      |                           -p invokerPackage=it.pagopa.pdnd.uservice.template.server
      |                           -p modelPackage=it.pagopa.pdnd.uservice.template.model
      |                           -p apiPackage=it.pagopa.pdnd.uservice.template.api
      |                           -p dateLibrary=java8
      |                           -o generated""".stripMargin
  ).!!
  println(output)
}

//(compile in Compile) := ((compile in Compile) dependsOn generateCode).value

lazy val generated = project.in(file("generated"))
  .settings(
    wartremoverExcluded += baseDirectory.value
  )

lazy val root = (project in file(".")).
  settings(
    name := "pdnd-uservice-template",
    wartremoverErrors ++= Warts.unsafe,
    libraryDependencies ++= Dependencies.Jars.`server`,
    parallelExecution in Test := false,
    packageName in Docker := s"services/${name.value}",
    daemonUser in Docker  := "daemon",
    dockerRepository in Docker := Some(System.getenv("DOCKER_REPO")),
    version in Docker := s"${(version in ThisBuild).value}-${Process("git log -n 1 --pretty=format:%h").lineStream.head}",
    dockerExposedPorts in Docker := Seq(8080),
    dockerBaseImage in Docker := "openjdk:8-jre-alpine",
    dockerUpdateLatest in Docker := true).
  dependsOn(generated).
  aggregate(generated).
  enablePlugins(AshScriptPlugin, DockerPlugin)
