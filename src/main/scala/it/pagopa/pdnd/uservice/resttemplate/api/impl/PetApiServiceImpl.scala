package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ClusterShardingSettings
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

import scala.concurrent.{ExecutionContextExecutor, Future}

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ImplicitParameter"
  )
)
class PetApiServiceImpl(system: ActorSystem[_]) extends PetApiService {

  private val sharding: ClusterSharding = ClusterSharding(system)

  private val entity = Entity(typeKey = PetPersistentBehavior.TypeKey) { entityContext =>
    PetPersistentBehavior(entityContext.shard, entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
  }

  private val settings: ClusterShardingSettings = entity.settings match {
    case None    => ClusterShardingSettings(system)
    case Some(s) => s
  }

  @inline private def getShard(id: String): String = (id.hashCode % settings.numberOfShards).toString

  locally {
    val _ = sharding.init(entity)
  }
  /** Code: 405, Message: Invalid input
    */
  override def addPet(pet: Pet): Route = {
    pet.id.fold(addPet405){
      id =>
        val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(id))
        val result: Future[StatusReply[State]] = commander.ask(ref => AddPet(pet, ref))
        onSuccess(result) {
          case statusReply if statusReply.isSuccess =>
            addPet200
          case statusReply if statusReply.isError   => addPet405
        }
    }
  }

  /** Code: 400, Message: Invalid ID supplied
    * Code: 404, Message: Pet not found
    */
  override def deletePet(petId: String): Route = {
    val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(petId))
    val result: Future[StatusReply[State]] = commander.ask(ref => DeletePet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess =>
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
    val commander = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(petId))
    val result: Future[StatusReply[Pet]] = commander.ask(ref => GetPet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess =>
        getPetById200(statusReply.getValue)
      case statusReply if statusReply.isError =>
        statusReply.getError match {
          case PetNotFoundException => getPetById404
          case _                    => getPetById400
        }
    }
  }

  /**
   * Code: 200, Message: List of pets, DataType: Seq[Pet]
   */
  override def listPets()(implicit toEntityMarshallerPetarray: ToEntityMarshaller[Seq[Pet]]): Route = {
    implicit val ec: ExecutionContextExecutor = system.executionContext
    val commanders = (0 until settings.numberOfShards).map(shard => sharding.entityRefFor(PetPersistentBehavior.TypeKey, shard.toString))
    val futures: Seq[Future[Seq[Pet]]] = commanders.map(_.ask(ref => List(ref)).map(_.getValue.pets.values.toSeq))
    val result: Future[Seq[Pet]] = Future.sequence(futures).map(_.flatten)
    onSuccess(result) {
      case pets: Seq[Pet] =>
        listPets200(pets)
    }
  }
}
