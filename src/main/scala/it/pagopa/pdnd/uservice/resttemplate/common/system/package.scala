package it.pagopa.pdnd.uservice.resttemplate.common
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.server.Directives.Authenticator
import akka.http.scaladsl.server.directives.Credentials
import akka.{actor => classic}

import scala.concurrent.ExecutionContext

package object system {

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty[Nothing], "pdnd-uservice-rest-template-system")

  implicit val executionContext: ExecutionContext = actorSystem.executionContext

  implicit val classicActorSystem: classic.ActorSystem = actorSystem.toClassic

  object Authenticator extends Authenticator[Unit] {
    override def apply(credentials: Credentials): Option[Unit] = Some(())
  }
}
