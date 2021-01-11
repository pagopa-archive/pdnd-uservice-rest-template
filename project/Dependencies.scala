import sbt._
import Versions._

object Dependencies {

  private[this] object akka {
    lazy val namespace = "com.typesafe.akka"
    lazy val stream    = namespace %% "akka-stream" % akkaVersion
    lazy val http      = namespace %% "akka-http"   % akkaHttpVersion
    lazy val slf4j     = namespace %% "akka-slf4j"  % akkaVersion
  }

  private[this] object webjars {
    lazy val namespace = "org.webjars"
    lazy val swaggerUi = namespace % "swagger-ui" % swaggerUiVersion
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
      akka.stream       % Compile,
      akka.http         % Compile,
      akka.slf4j        % Compile,
      webjars.swaggerUi % Compile,
      scalatest.core    % Test,
      mockito.core      % Test
    )
  }
}
