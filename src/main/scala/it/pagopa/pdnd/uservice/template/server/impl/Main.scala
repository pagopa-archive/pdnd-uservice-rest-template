package it.pagopa.pdnd.uservice.template.server.impl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.{actor => classic}
import it.pagopa.pdnd.uservice.template.api.PetApi
import it.pagopa.pdnd.uservice.template.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.template.server.Controller

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: classic.ActorSystem = context.system.toClassic

    val petApi = PetApi(PetApiServiceImpl(), PetApiMarshallerImpl())

    val controller = Controller(petApi)

    locally {
      Await.result(controller.bindingFuture, Duration.Inf)
    }
    Behaviors.empty
  }, "pdnd-uservice-template")
}
