import Versions._
import sbt._

object Dependencies {

  private[this] object akka {
    lazy val namespace  = "com.typesafe.akka"
    lazy val actorTyped = namespace %% "akka-actor-typed" % akkaVersion
    lazy val stream     = namespace %% "akka-stream" % akkaVersion
    lazy val http       = namespace %% "akka-http"   % akkaHttpVersion
    lazy val httpJson   = namespace %% "akka-http-spray-json" % akkaHttpVersion
    lazy val management = "com.lightbend.akka.management" %% "akka-management" % "1.0.9" 
    lazy val slf4j      = namespace %% "akka-slf4j"  % akkaVersion
  }

  private[this] object logback {
    lazy val namespace = "ch.qos.logback"
    lazy val classic   = namespace % "logback-classic" % logbackVersion
  }

  private[this] object scalatest {
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace %% "scalatest" % scalatestVersion
  }

  private[this] object mockito {
    lazy val namespace = "org.mockito"
    lazy val core      = namespace % "mockito-core" % mockitoVersion
  }

  object Jars {
    lazy val `server`: Seq[ModuleID] = Seq(
      // For making Java 12 happy
      "javax.annotation" % "javax.annotation-api" % "1.3.2" % "compile",
      //
      akka.actorTyped   % Compile,
      akka.stream       % Compile,
      akka.http         % Compile,
      akka.httpJson     % Compile,
      akka.management   % Compile,
      logback.classic   % Compile,
      akka.slf4j        % Compile,
      scalatest.core    % Test,
      mockito.core      % Test
    )
  }
}
