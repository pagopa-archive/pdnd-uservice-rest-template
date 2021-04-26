package it.pagopa.pdnd.uservice.resttemplate.model.persistence

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import it.pagopa.pdnd.uservice.resttemplate.model.Pet

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object PetPersistentBehavior {

  final case object PetNotFoundException extends Throwable

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case AddPet(newPet, replyTo) =>
        val pet = for {
          petId <- newPet.id
          pet   <- state.pets.get(petId)
        } yield pet

        pet
          .map { _ =>
            replyTo ! StatusReply.Error[State](s"Pet ${newPet.id.getOrElse("UNKNOWN")} already exists")
            Effect.none[PetAdded, State]
          }
          .getOrElse {
            Effect
              .persist(PetAdded(newPet))
              .thenRun(s => replyTo ! StatusReply.Success(s))
          }

      case DeletePet(petId, replyTo) =>
        state.pets
          .get(petId) match {
          case Some(_) =>
            Effect
              .persist(PetDeleted(petId))
              .thenRun(state => replyTo ! StatusReply.Success(state))
          case None =>
            replyTo ! StatusReply.Error[State](PetNotFoundException)
            Effect.none[Event, State]
        }

      case GetPet(petId, replyTo) =>
        state.pets
          .get(petId) match {
          case Some(pet) =>
            replyTo ! StatusReply.Success(pet)
            Effect.none[Event, State]
          case None =>
            replyTo ! StatusReply.Error[Pet](PetNotFoundException)
            Effect.none[Event, State]
        }
    }
  }

  val eventHandler: (State, Event) => State = (state, event) =>
    event match {
      case PetAdded(pet)     => state.add(pet)
      case PetDeleted(petId) => state.delete(petId)
    }

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("pdnd-uservice-rest-template"),
      emptyState = State.empty,
      commandHandler = commandHandler,
      eventHandler = eventHandler
    ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 1))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200 millis, 5 seconds, 0.1))
}
