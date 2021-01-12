package it.pagopa.pdnd.uservice.template.server.impl

import akka.actor.ActorSystem
import it.pagopa.pdnd.uservice.template.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl, ProbeApiServiceImpl}
import it.pagopa.pdnd.uservice.template.api.{PetApi, ProbeApi}
import it.pagopa.pdnd.uservice.template.server.Controller

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  implicit private val system: ActorSystem = ActorSystem("server")

  val petApi = new PetApi(new PetApiServiceImpl(), new PetApiMarshallerImpl())

  val probeApi = new ProbeApi(new ProbeApiServiceImpl())

  val controller = new Controller(petApi, probeApi)

  locally {
    val _ = Await.result(controller.bindingFuture, Duration.Inf)
  }

}
