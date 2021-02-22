package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.SecurityDirectives
import akka.management.scaladsl.AkkaManagement
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.common.system.{Authenticator, classicActorSystem}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller
import kamon.Kamon

object Main extends App {

  Kamon.init()

  val petApi = new PetApi(new PetApiServiceImpl(), new PetApiMarshallerImpl(), SecurityDirectives.authenticateBasic("SecurityRealm", Authenticator))

  locally {
    val _ = AkkaManagement.get(classicActorSystem).start()
  }

  val controller = new Controller(petApi)

  val bindingFuture = Http().
    newServerAt(
      "0.0.0.0",
      8088,
    ).
    bind(controller.routes)
}