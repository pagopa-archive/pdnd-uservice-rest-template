import Versions._
import sbt._

object Dependencies {

  private[this] object akka {
    lazy val namespace              = "com.typesafe.akka"
    lazy val actorTyped             = namespace %% "akka-actor-typed" % akkaVersion
    lazy val clusterTyped           = namespace %% "akka-cluster-typed" % akkaVersion
    lazy val clusterSharding        = namespace %% "akka-cluster-sharding-typed" % akkaVersion
    lazy val discovery              = namespace %% "akka-discovery" % akkaVersion
    lazy val persistence            = namespace %% "akka-persistence-typed" % akkaVersion
    lazy val persistenceQuery       = namespace %% "akka-persistence-query" % akkaVersion
    lazy val projection             = "com.lightbend.akka" %% "akka-projection-eventsourced" % projectionVersion
    lazy val projectionCassandra    = "com.lightbend.akka" %% "akka-projection-cassandra" % projectionVersion
    lazy val clusterTools           = namespace %% "akka-cluster-tools" % akkaVersion
    lazy val persistenceCassandra   = namespace %% "akka-persistence-cassandra" % cassandraPersistenceVersion
    lazy val s3Journal              = "com.github.j5ik2o" %% "akka-persistence-s3-journal" % s3Persistence
    lazy val s3Snapshot             = "com.github.j5ik2o" %% "akka-persistence-s3-snapshot" % s3Persistence
    lazy val stream                 = namespace %% "akka-stream-typed" % akkaVersion
    lazy val http                   = namespace %% "akka-http"   % akkaHttpVersion
    lazy val httpJson               = namespace %% "akka-http-spray-json" % akkaHttpVersion
    lazy val httpJson4s             = "de.heikoseeberger" %% "akka-http-json4s" % httpJson4sVersion
    lazy val discoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion
    lazy val clusterBootstrap       = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
    lazy val clusterHttp            = "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion
    lazy val slf4j                  = namespace %% "akka-slf4j"  % akkaVersion
  }

  private[this] object awssdk {
    lazy val namespace = "software.amazon.awssdk"
    lazy val s3        = namespace % "s3" % awsSdkVersion
  }

  lazy val Protobuf  = "protobuf"

  private[this] object scalaprotobuf {
    lazy val namespace = "com.thesamet.scalapb"
    lazy val core      = namespace %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion
  }

  private[this] object json4s {
    lazy val namespace = "org.json4s"
    lazy val jackson   = namespace %% "json4s-jackson" % json4sVersion
    lazy val ext       = namespace %% "json4s-ext" % json4sVersion
  }

  private[this] object jackson {
    lazy val namespace       = "com.fasterxml.jackson.core"
    lazy val core            = namespace % "jackson-core" % jacksonVersion
    lazy val annotations     = namespace % "jackson-annotations" % jacksonVersion
    lazy val databind        = namespace % "jackson-databind" % jacksonVersion
  }

  private[this] object logback {
    lazy val namespace = "ch.qos.logback"
    lazy val classic   = namespace % "logback-classic" % logbackVersion
  }

  private[this] object kamon {
    lazy val namespace  = "io.kamon"
    lazy val bundle     = namespace %% "kamon-bundle"     % kamonVersion
    lazy val prometheus = namespace %% "kamon-prometheus" % kamonVersion
  }

  private[this] object openapi4j {
    lazy val namespace          = "org.openapi4j"
    lazy val operationValidator = namespace % "openapi-operation-validator" % openapi4jVersion
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
    lazy val overrides: Seq[ModuleID] = Seq(
      jackson.annotations      % Compile,
      jackson.core             % Compile,
      jackson.databind         % Compile
    )
    lazy val `server`: Seq[ModuleID] = Seq(
      // For making Java 12 happy
      "javax.annotation" % "javax.annotation-api" % "1.3.2" % "compile",
      //
      akka.actorTyped              % Compile,
      akka.clusterTyped            % Compile,
      akka.clusterSharding         % Compile,
      akka.clusterHttp             % Compile,
      akka.discovery               % Compile,
      akka.discoveryKubernetesApi  % Compile,
      akka.clusterBootstrap        % Compile,
      akka.clusterTools            % Compile,
      akka.persistence             % Compile,
      akka.persistenceQuery        % Compile,
      akka.projection              % Compile,
      akka.projectionCassandra     % Compile,
      akka.persistenceCassandra    % Compile,
      akka.s3Journal               % Compile,
      akka.s3Snapshot              % Compile,
      akka.stream                  % Compile,
      akka.http                    % Compile,
      akka.httpJson                % Compile,
      awssdk.s3                    % Compile,
      logback.classic              % Compile,
      akka.slf4j                   % Compile,
      openapi4j.operationValidator % Compile,
      kamon.bundle                 % Compile,
      kamon.prometheus             % Compile,
      scalaprotobuf.core           % Protobuf,
      scalatest.core               % Test
    )
    lazy val client: Seq[ModuleID] = Seq(
      akka.stream                  % Compile,
      akka.http                    % Compile,
      akka.httpJson4s              % Compile,
      json4s.jackson               % Compile,
      json4s.ext                   % Compile
    )
  }
}
