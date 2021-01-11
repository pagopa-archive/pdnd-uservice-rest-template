package it.pagopa.pdnd.uservice.template.server.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import it.pagopa.pdnd.uservice.template.api.impl.PetApiServiceImpl
import it.pagopa.pdnd.uservice.template.api.{PetApi, PetApiMarshaller}
import it.pagopa.pdnd.uservice.template.model.Pet
import it.pagopa.pdnd.uservice.template.server.Controller

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  implicit private val system: ActorSystem = ActorSystem("server")

  val api = new PetApi(new PetApiServiceImpl(), new PetApiMarshaller {
    override implicit def fromEntityUnmarshallerPet: FromEntityUnmarshaller[Pet] = ???

    override implicit def toEntityMarshallerPet: ToEntityMarshaller[Pet] = ???
  })

  val controller = new Controller(api)

  locally {
    val _ = Await.result(controller.bindingFuture, Duration.Inf)
  }

}
