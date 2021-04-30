package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.onSuccess
import akka.http.scaladsl.server.Route
import akka.pattern.StatusReply
import it.pagopa.pdnd.uservice.resttemplate.api.PetApiService
import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.{AddPet, Command, DeletePet, GetPet, State}
import it.pagopa.pdnd.uservice.resttemplate.common.system._
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.PetPersistentBehavior.PetNotFoundException

import scala.concurrent.Future
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class PetApiServiceImpl(commander: ActorRef[Command]) extends PetApiService {

  /** Code: 405, Message: Invalid input
    */
  override def addPet(pet: Pet): Route = {
    val result: Future[StatusReply[State]] = commander.ask(ref => AddPet(pet, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess => addPet200
      case statusReply if statusReply.isError   => addPet405
    }
  }

  /** Code: 400, Message: Invalid ID supplied
    * Code: 404, Message: Pet not found
    */
  override def deletePet(petId: String): Route = {
    val result: Future[StatusReply[State]] = commander.ask(ref => DeletePet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess => deletePet200
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

    val result: Future[StatusReply[Pet]] = commander.ask(ref => GetPet(petId, ref))
    onSuccess(result) {
      case statusReply if statusReply.isSuccess => getPetById200(statusReply.getValue)
      case statusReply if statusReply.isError =>
        statusReply.getError match {
          case PetNotFoundException => getPetById404
          case _                    => getPetById400
        }
    }
  }
}
