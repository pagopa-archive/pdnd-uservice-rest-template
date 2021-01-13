package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.management.scaladsl.AkkaManagement
import akka.stream.scaladsl.{Sink, Source}
import akka.{actor => classic}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller

object Main extends App {

  ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: classic.ActorSystem = context.system.toClassic

    val petApi = PetApi(PetApiServiceImpl(), PetApiMarshallerImpl())

    val controller = Controller(petApi)

    AkkaManagement.get(classicSystem).start()

    Behaviors.empty
  }, "pdnd-uservice-template")
}
