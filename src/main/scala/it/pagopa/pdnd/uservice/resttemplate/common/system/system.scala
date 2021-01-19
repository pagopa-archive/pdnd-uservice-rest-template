package it.pagopa.pdnd.uservice.resttemplate.common.system

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.{TypedActorRefOps, TypedActorSystemOps}
import akka.http.scaladsl.server.Directives.Authenticator
import akka.http.scaladsl.server.directives.Credentials
import akka.{actor => classic}

import scala.concurrent.ExecutionContext

given actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "pdnd-uservice-user-management-system")

given executionContext: ExecutionContext = actorSystem.executionContext

given classicActorSystem: classic.ActorSystem = actorSystem.toClassic

object Authenticator extends Authenticator[Unit] {
  override def apply(credentials: Credentials): Option[Unit] = Some(())
}


