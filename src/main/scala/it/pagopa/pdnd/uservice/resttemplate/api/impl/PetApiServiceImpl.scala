package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onSuccess
import akka.http.scaladsl.server.Route
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import it.pagopa.pdnd.uservice.resttemplate.api.PetApiService
import it.pagopa.pdnd.uservice.resttemplate.common.system._
import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.PetPersistentBehavior.PetNotFoundException
import it.pagopa.pdnd.uservice.resttemplate.model.persistence._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.OptionPartial",
    "org.wartremover.warts.NonUnitStatements"
  )
)
class PetApiServiceImpl(system: ActorSystem[_]) extends PetApiService {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  private val sharding = ClusterSharding(system)

  sharding.init(Entity(typeKey = PetPersistentBehavior.TypeKey) { entityContext =>
    PetPersistentBehavior(entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
  })

  /** Code: 405, Message: Invalid input
    */
  override def addPet(pet: Pet): Route = {
    val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, pet.id.get)
    val result: Future[StatusReply[State]] = commander.ask(ref => AddPet(pet, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess =>
        log.error("ped added")
        addPet200
      case statusReply if statusReply.isError   => addPet405
    }
  }

  /** Code: 400, Message: Invalid ID supplied
    * Code: 404, Message: Pet not found
    */
  override def deletePet(petId: String): Route = {
    val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, petId)
    val result: Future[StatusReply[State]] = commander.ask(ref => DeletePet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess =>
        log.error("pet deleted")
        deletePet200
      case statusReply if statusReply.isError =>
        statusReply.getError match {
          case PetNotFoundException => deletePet404
          case _                    => deletePet400
        }
    }
  }

  /** Code: 200, Message: successful operation, DataType: Pet
    * Code: 400, Message: Invalid ID supplied
    * Code: 404, Message: Pet not found
    */
  override def getPetById(petId: String)(implicit toEntityMarshaller: ToEntityMarshaller[Pet]): Route = {
    val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, petId)
    val result: Future[StatusReply[Pet]] = commander.ask(ref => GetPet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess =>
        log.error("pet returned")
        getPetById200(statusReply.getValue)
      case statusReply if statusReply.isError =>
        statusReply.getError match {
          case PetNotFoundException => getPetById404
          case _                    => getPetById400
        }
    }
  }
}
