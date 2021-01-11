import scala.sys.process.Process

Global / semanticdbEnabled := true
Global / semanticdbVersion := "4.4.0"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "it.pagopa"
ThisBuild / organizationName := "Pagopa S.p.A."
ThisBuild / wartremoverErrors ++= Warts.unsafe
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
ThisBuild / resolvers += Resolver.sonatypeRepo("public")
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")
ThisBuild / credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
ThisBuild / libraryDependencies ++= Dependencies.Jars.`server`
ThisBuild / parallelExecution in Test := false
ThisBuild / fork in Test := true

Docker / packageName := "services/pdnd-uservice-template"
Docker / daemonUser := "daemon"
Docker / dockerRepository := Some(System.getenv("DOCKER_REPO"))
Docker / version := s"${(version in ThisBuild).value}-${Process("git log -n 1 --pretty=format:%h").lineStream.head}"
Docker / dockerExposedPorts := Seq(8080)
Docker / dockerBaseImage := "openjdk:8-jre-alpine"
Docker / dockerUpdateLatest := true

updateOptions := updateOptions.value.withGigahorse(false)
dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")

lazy val generated = project.in(file("generated"))
  .enablePlugins(OpenApiGeneratorPlugin)
  .settings(
    openApiInputSpec := "src/main/resources/interface-specification.yml",
    openApiConfigFile := "config.yaml",
    openApiValidateSpec := SettingDisabled,
    openApiGenerateModelTests := SettingEnabled,
    wartremoverExcluded += baseDirectory.value
  )

lazy val root = (project in file(".")).
  dependsOn(generated).
  aggregate(generated).
  enablePlugins(AshScriptPlugin, DockerPlugin)
