package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.SecurityDirectives
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.management.scaladsl.AkkaManagement
import akka.stream.scaladsl.{Sink, Source}
import akka.{actor => classic}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.common.system.{Authenticator, classicActorSystem}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller
import kamon.Kamon

object Main extends App {

  Kamon.init()

  val petApi = PetApi(PetApiServiceImpl(), PetApiMarshallerImpl(), SecurityDirectives.authenticateBasic("SecurityRealm", Authenticator))

  AkkaManagement.get(classicActorSystem).start()
  
  val controller = Controller(petApi)
  
}