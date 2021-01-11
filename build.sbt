import com.typesafe.sbt.packager.docker.DockerChmodType

import scala.sys.process.Process

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "it.pagopa"
ThisBuild / organizationName := "Pagopa S.p.A."

ThisBuild / wartremoverErrors ++= Warts.unsafe
ThisBuild / scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:higherKinds", // Allow higher-kinded types
  "-language:postfixOps", // Allow postfix operator notation, such as `1 to 10 toList'
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
  "-Ywarn-macros:before", // via som
  "-Yrangepos" // for longer squiggles
)
Global / semanticdbEnabled := true
Global / semanticdbVersion := "4.4.0"
ThisBuild / resolvers += Resolver.sonatypeRepo("public")
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")
ThisBuild / credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

ThisBuild / libraryDependencies ++= Dependencies.Jars.`server`

def tree(root: File, skipHidden: Boolean = false): Stream[File] =
  if (!root.exists || (skipHidden && root.isHidden)) Stream.empty
  else
    root #:: (root.listFiles match {
      case null  => Stream.empty
      case files => files.toStream.flatMap(tree(_, skipHidden))
    })

// scoverage settings
coverageEnabled := false // Disable for publishing
//coverage for only implemented parts
coverageExcludedPackages := "<empty>;.*api.*;.*model;.*router;.*controllers.*;.*modules"
coverageMinimum := 80
coverageFailOnMinimum := true

// update settings
updateOptions := updateOptions.value.withGigahorse(false)
dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")

//tests
parallelExecution in Test := false
fork in Test := true

lazy val generated = project.in(file("generated"))
  .enablePlugins(OpenApiGeneratorPlugin)
  .settings(
    openApiInputSpec := "src/main/resources/interface-specification.yml",
    openApiConfigFile := "config.yaml",
    openApiValidateSpec := SettingDisabled,
    openApiGenerateModelTests := SettingEnabled,
    wartremoverExcluded += baseDirectory.value
    //    scalaVersion := "2.13.4"
  )

lazy val root = (project in file(".")).dependsOn(generated).aggregate(generated)

//Docker settings
dockerRepository := Some("gateway.pdnd.dev")
dockerBaseImage := "openjdk:8u212-jre-slim-buster"
version in Docker := s"${(version in ThisBuild).value}-${Process("git log -n 1 --pretty=format:%h").lineStream.head}"
dockerChmodType := DockerChmodType.UserGroupWriteExecute
