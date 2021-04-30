package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.directives.SecurityDirectives
import akka.management.scaladsl.AkkaManagement
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.common.system.{Authenticator, classicActorSystem}
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.{Command, PetPersistentBehavior}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller
import kamon.Kamon

import scala.jdk.CollectionConverters._

object Main extends App {

  Kamon.init()

  val petCommander: ActorSystem[Command] = ActorSystem(PetPersistentBehavior(), "pdnd-uservice-rest-template")

  private val petApi = new PetApi(
    new PetApiServiceImpl(petCommander),
    new PetApiMarshallerImpl(),
    SecurityDirectives.authenticateBasic("SecurityRealm", Authenticator)
  )

  locally {
    val _ = AkkaManagement.get(classicActorSystem).start()

    val controller = new Controller(
      petApi,
      validationExceptionToRoute = Some(e => {
        val results = e.results()
        results.crumbs().asScala.foreach { crumb =>
          println(crumb.crumb())
        }
        results.items().asScala.foreach { item =>
          println(item.dataCrumbs())
          println(item.dataJsonPointer())
          println(item.schemaCrumbs())
          println(item.message())
          println(item.severity())
        }
        val message = e.results().items().asScala.map(_.message()).mkString("\n")
        complete((400, message))
      })
    )

    val _ = Http().newServerAt("0.0.0.0", 8088).bind(controller.routes)
  }
}
