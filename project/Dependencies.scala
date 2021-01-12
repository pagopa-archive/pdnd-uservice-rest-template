import sbt._
import Versions._
import sbt.Keys.scalaVersion

object Dependencies {

  private[this] object akka {
    lazy val namespace  = "com.typesafe.akka"
    lazy val actorTyped = namespace % "akka-actor-typed_2.13" % akkaVersion
    lazy val stream     = namespace % "akka-stream_2.13" % akkaVersion
    lazy val http       = namespace % "akka-http_2.13"   % akkaHttpVersion
    lazy val httpJson   = namespace % "akka-http-spray-json_2.13" % akkaHttpVersion
    lazy val slf4j      = namespace % "akka-slf4j_2.13"  % akkaVersion
  }

  private[this] object logback {
    lazy val namespace = "ch.qos.logback"
    lazy val classic   = namespace % "logback-classic" % logbackVersion
  }

  private[this] object scalatest {
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace % "scalatest_2.13" % scalatestVersion
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
      logback.classic   % Compile,
      akka.slf4j        % Compile,
      scalatest.core    % Test,
      mockito.core      % Test
    )
  }
}
