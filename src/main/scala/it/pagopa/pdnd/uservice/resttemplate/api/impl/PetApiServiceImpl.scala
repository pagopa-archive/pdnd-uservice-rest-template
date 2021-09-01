package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardingEnvelope}
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onSuccess
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.pattern.StatusReply
import it.pagopa.pdnd.uservice.resttemplate.api.PetApiService
import it.pagopa.pdnd.uservice.resttemplate.common.system._
import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.PetPersistentBehavior.PetNotFoundException
import it.pagopa.pdnd.uservice.resttemplate.model.persistence._

import java.io.File
import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.Any",
    "org.wartremover.warts.Nothing",
    "org.wartremover.warts.Recursion"
  )
)
class PetApiServiceImpl(
  system: ActorSystem[_],
  sharding: ClusterSharding,
  entity: Entity[Command, ShardingEnvelope[Command]]
) extends PetApiService {

  private val settings: ClusterShardingSettings = entity.settings match {
    case None    => ClusterShardingSettings(system)
    case Some(s) => s
  }

  @inline private def getShard(id: String): String = math.abs(id.hashCode % settings.numberOfShards).toString

  /** Code: 200, Message: Pet create
    * Code: 405, Message: Invalid input
    */
  override def addPet(pet: Pet)(implicit contexts: Seq[(String, String)]): Route = {
    println(contexts)
    pet.id.fold(addPet405) { id =>
      val commander                           = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(id))
      val result: Future[StatusReply[String]] = commander.ask(ref => AddPet(pet, ref))
      onSuccess(result) {
        case statusReply if statusReply.isSuccess =>
          addPet200
        case statusReply if statusReply.isError => addPet405
      }
    }
  }

  /** Code: 200, Message: successful operation
    * Code: 400, Message: Invalid ID supplied
    * Code: 404, Message: Pet not found
    */
  override def deletePet(petId: String)(implicit contexts: Seq[(String, String)]): Route = {
    val commander                           = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(petId))
    val result: Future[StatusReply[String]] = commander.ask(ref => DeletePet(petId, ref))
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
  override def getPetById(
    petId: String
  )(implicit contexts: Seq[(String, String)], toEntityMarshallerPet: ToEntityMarshaller[Pet]): Route = {
    val commander                        = sharding.entityRefFor(PetPersistentBehavior.TypeKey, getShard(petId))
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

  private def slices(commander: EntityRef[Command], sliceSize: Int): LazyList[Pet] = {
    @tailrec
    def readSlice(commander: EntityRef[Command], from: Int, to: Int, lazyList: LazyList[Pet]): LazyList[Pet] = {
      lazy val slice: Seq[Pet] = Await.result(commander.ask(ref => List(from, to, ref)), Duration.Inf).getValue
      if (slice.isEmpty)
        lazyList
      else
        readSlice(commander, to, to + sliceSize, slice.to(LazyList) #::: lazyList)
    }
    readSlice(commander, 0, sliceSize, LazyList.empty)
  }

  /** Code: 200, Message: List of pets, DataType: Seq[Pet]
    */
  override def listPets()(implicit
    contexts: Seq[(String, String)],
    toEntityMarshallerPetarray: ToEntityMarshaller[Seq[Pet]]
  ): Route = {
    val sliceSize = 1000
    val commanders: Seq[EntityRef[Command]] = (0 until settings.numberOfShards).map(shard =>
      sharding.entityRefFor(PetPersistentBehavior.TypeKey, shard.toString)
    )
    val pets: Seq[Pet] = commanders.to(LazyList).flatMap(ref => slices(ref, sliceSize))
    listPets200(pets)
  }

  /** Code: 200, Message: successful operation
    */
  override def addDocument(description: String, doc: (FileInfo, File))(implicit
    contexts: Seq[(String, String)]
  ): Route = {
    addDocument200
  }
}
