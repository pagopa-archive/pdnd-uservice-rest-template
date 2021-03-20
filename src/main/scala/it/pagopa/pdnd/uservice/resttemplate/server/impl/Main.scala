package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.directives.SecurityDirectives
import akka.management.scaladsl.AkkaManagement
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.common.system.{Authenticator, classicActorSystem}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller
import kamon.Kamon
import org.openapi4j.operation.validator.validation.RequestValidator
import org.openapi4j.parser.OpenApi3Parser
import scala.jdk.CollectionConverters._
import java.io.{File, FileOutputStream}

object Main extends App {

  Kamon.init()

  val resource = Thread.currentThread().getContextClassLoader.getResourceAsStream("interface-specification.yml")
  val file = new File("/tmp/interface-specification.yml")
  locally {
    val _ = resource.transferTo(new FileOutputStream(file))
  }
  val api = new OpenApi3Parser().parse(file, true)
  val validator = new RequestValidator(api)

  val petApi = new PetApi(
    new PetApiServiceImpl(),
    new PetApiMarshallerImpl(),
    SecurityDirectives.authenticateBasic("SecurityRealm", Authenticator),
    validator = Some(validator),
    validationExceptionToRoute = Some(e => {
      val message = e.results().items().asScala.map(_.message()).mkString("\n")
      complete((400, message))
    })
  )

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