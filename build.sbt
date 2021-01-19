import scala.sys.process.Process

ThisBuild / scalaVersion := "3.0.0-M3"
ThisBuild / organization := "it.pagopa"
ThisBuild / organizationName := "Pagopa S.p.A."
ThisBuild / libraryDependencies ++= Dependencies.Jars.`server`.map(_.withDottyCompat(scalaVersion.value))

lazy val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

generateCode := {
  import sys.process._

  val packagePrefix = name.value.
    replaceFirst("pdnd-","pdnd.").
    replaceFirst("uservice-","uservice.").
    replaceAll("-","")

  val output = Process(
    s"""openapi-generator generate -t template/scala-akka-http-server
       |                           -i src/main/resources/interface-specification.yml
       |                           -g scala-akka-http-server
       |                           -p projectName=${name.value}
       |                           -p invokerPackage=it.pagopa.${packagePrefix}.server
       |                           -p modelPackage=it.pagopa.${packagePrefix}.model
       |                           -p apiPackage=it.pagopa.${packagePrefix}.api
       |                           -p dateLibrary=java8
       |                           -o generated""".stripMargin
  ).!!
}

(compile in Compile) := ((compile in Compile) dependsOn generateCode).value

cleanFiles += baseDirectory.value / "generated" / "src"

lazy val generated = project.in(file("generated")).settings(scalacOptions := Seq())

lazy val root = (project in file(".")).
  settings(
    name := "pdnd-uservice-rest-template",
    parallelExecution in Test := false,
    dockerRepository in Docker := Some(System.getenv("DOCKER_REPO")),
    version in Docker := s"${(version in ThisBuild).value}".toLowerCase,
    packageName in Docker := s"services/${name.value}",
    daemonUser in Docker  := "daemon",
    dockerExposedPorts in Docker := Seq(8088),
    dockerBaseImage in Docker := "openjdk:8-jre-alpine",
    dockerUpdateLatest in Docker := true
  ).
  dependsOn(generated).
  aggregate(generated).
  enablePlugins(AshScriptPlugin, DockerPlugin)

