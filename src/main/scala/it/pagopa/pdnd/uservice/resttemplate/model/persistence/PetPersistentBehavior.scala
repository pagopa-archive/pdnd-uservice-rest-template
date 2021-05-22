package it.pagopa.pdnd.uservice.resttemplate.model.persistence

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import it.pagopa.pdnd.uservice.resttemplate.model.Pet

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.{DurationInt, DurationLong}
import scala.language.postfixOps

object PetPersistentBehavior {

  final case object PetNotFoundException extends Throwable

  def commandHandler(shard: ActorRef[ClusterSharding.ShardCommand], context: ActorContext[Command]): (State, Command) => Effect[Event, State] = { (state, command) =>
    val idleTimeout = context.system.settings.config.getDuration("pdnd-uservice-rest-template.idle-timeout")
    context.setReceiveTimeout(idleTimeout.get(ChronoUnit.SECONDS) seconds, Idle)
    command match {
      case AddPet(newPet, replyTo) =>
        val pet = for {
          petId <- newPet.id
          pet   <- state.pets.get(petId)
        } yield pet

        pet
          .map { _ =>
            replyTo ! StatusReply.Error[String](s"Pet ${newPet.id.getOrElse("UNKNOWN")} already exists")
            Effect.none[PetAdded, State]
          }
          .getOrElse {
            Effect
              .persist(PetAdded(newPet))
              .thenRun((_: State) => replyTo ! StatusReply.Success(newPet.id.getOrElse("UNKNOWN")))
          }

      case DeletePet(petId, replyTo) =>
        state.pets
          .get(petId) match {
          case Some(_) =>
            Effect
              .persist(PetDeleted(petId))
              .thenRun(_ => replyTo ! StatusReply.Success(petId))
          case None =>
            replyTo ! StatusReply.Error[String](PetNotFoundException)
            Effect.none[Event, State]
        }

      case List(replyTo) =>
        replyTo ! StatusReply.Success(state)
        Effect.none[Event, State]

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

      case Idle =>
        shard ! ClusterSharding.Passivate(context.self)
        context.log.error(s"Passivate shard: ${shard.path.name}")
        Effect.none[Event, State]
    }
  }

  val eventHandler: (State, Event) => State = (state, event) =>
    event match {
      case PetAdded(pet)     => state.add(pet)
      case PetDeleted(petId) => state.delete(petId)
    }

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("pdnd-uservice-rest-template-persistence-pet")

  def apply(shard: ActorRef[ClusterSharding.ShardCommand], persistenceId: PersistenceId): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.error(s"Starting Pet Shard ${persistenceId.id}")
      val numberOfEvents = context.system.settings.config.getInt("pdnd-uservice-rest-template.number-of-events-before-snapshot")
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.empty,
        commandHandler = commandHandler(shard, context),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = numberOfEvents, keepNSnapshots = 1))
        .withTagger(_ => Set(persistenceId.id))
        .onPersistFailure(SupervisorStrategy.restartWithBackoff(200 millis, 5 seconds, 0.1))
    }
  }
}
