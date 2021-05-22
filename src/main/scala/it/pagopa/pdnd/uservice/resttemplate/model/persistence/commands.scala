package it.pagopa.pdnd.uservice.resttemplate.model.persistence

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import it.pagopa.pdnd.uservice.resttemplate.model.Pet

sealed trait Command

final case class AddPet(pet: Pet, replyTo: ActorRef[StatusReply[String]])         extends Command
final case class DeletePet(petId: String, replyTo: ActorRef[StatusReply[String]]) extends Command
final case class GetPet(petId: String, replyTo: ActorRef[StatusReply[Pet]])      extends Command
final case class List(replyTo: ActorRef[StatusReply[State]])                     extends Command
case object      Idle                                                            extends Command
