import scala.sys.process.Process

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / organization := "it.pagopa"
ThisBuild / organizationName := "Pagopa S.p.A."
ThisBuild / libraryDependencies := Dependencies.Jars.`server`.map(m =>
  if (scalaVersion.value.startsWith("3.0"))
    m.withDottyCompat(scalaVersion.value)
  else
    m
)
ThisBuild / dependencyOverrides ++= Dependencies.Jars.overrides
ThisBuild / version := {
  Process("./version.sh").lineStream_!.head.replaceFirst("v", "")
}

val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

val packagePrefix = settingKey[String]("The package prefix derived from the uservice name")

packagePrefix := {
    name.value.replaceFirst("pdnd-", "pdnd.").replaceFirst("uservice-", "uservice.").replaceAll("-", "")
}

generateCode := {
  import sys.process._

  Process(s"""openapi-generator-cli generate -t template/scala-akka-http-server
             |                               -i src/main/resources/interface-specification.yml
             |                               -g scala-akka-http-server
             |                               -p projectName=${name.value}
             |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.server
             |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
             |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
             |                               -p dateLibrary=java8
             |                               -o generated""".stripMargin).!!

  Process(s"""openapi-generator-cli generate -t template/scala-akka-http-client
             |                               -i src/main/resources/interface-specification.yml
             |                               -g scala-akka
             |                               -p projectName=${name.value}
             |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.client.invoker
             |                               -p modelPackage=it.pagopa.${packagePrefix.value}.client.model
             |                               -p apiPackage=it.pagopa.${packagePrefix.value}.client.api
             |                               -p dateLibrary=java8
             |                               -o client""".stripMargin).!!

}

Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value)

cleanFiles += baseDirectory.value / "generated" / "src"

cleanFiles += baseDirectory.value / "generated" / "target"

cleanFiles += baseDirectory.value / "client" / "src"

cleanFiles += baseDirectory.value / "client" / "target"

lazy val generated = project
  .in(file("generated"))
  .settings(
    scalacOptions := Seq()
  ).enablePlugins(BuildInfoPlugin)

lazy val client = project
  .in(file("client"))
  .settings(
    name := "pdnd-uservice-rest-template-client",
    scalacOptions := Seq(),
    libraryDependencies := Dependencies.Jars.client.map(m =>
      if (scalaVersion.value.startsWith("3.0"))
        m.withDottyCompat(scalaVersion.value)
      else
        m
    ),
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    updateOptions := updateOptions.value.withGigahorse(false),
    publishTo := {
      val nexus = s"https://${System.getenv("MAVEN_REPO")}/nexus/repository/"

      if (isSnapshot.value)
        Some("snapshots" at nexus + "maven-snapshots/")
      else
        Some("releases" at nexus + "maven-releases/")
    }
  )

lazy val root = (project in file("."))
  .settings(
    name := "pdnd-uservice-rest-template",
    Test / parallelExecution := false,
    dockerBuildOptions ++= Seq("--network=host"),
    dockerRepository := Some(System.getenv("DOCKER_REPO")),
    dockerBaseImage := "adoptopenjdk:11-jdk-hotspot",
    dockerUpdateLatest := true,
    daemonUser := "daemon",
    Docker / version := s"${
      val buildVersion = (ThisBuild / version).value
      if (buildVersion == "latest")
        buildVersion
      else
        s"v$buildVersion"
    }".toLowerCase,
    Docker / packageName := s"services/${name.value}",
    Docker / dockerExposedPorts := Seq(8080),
    wartremoverErrors ++= Warts.all,
    wartremoverExcluded += sourceManaged.value
  )
  .aggregate(client)
  .dependsOn(generated)
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging, JavaAgent)

javaAgents += "io.kamon" % "kanela-agent" % "1.0.7"
