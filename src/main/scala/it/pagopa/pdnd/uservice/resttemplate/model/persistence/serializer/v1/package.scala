package it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer

import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1.events.{PetAddedV1, PetDeletedV1}
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1.pet.PetV1
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1.state.{PetsV1, StateV1}
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.{PetAdded, PetDeleted, State}

package object v1 {

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Nothing"
    )
  )
  implicit def stateV1PersistEventDeserializer: PersistEventDeserializer[StateV1, State] =
    state => {
      val pets = state.pets.map(petsV1 => (petsV1.key, Pet(Some(petsV1.value.id), petsV1.value.name))).toMap
      Right(State(pets))
    }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Nothing",
      "org.wartremover.warts.OptionPartial"
    )
  )
  implicit def stateV1PersistEventSerializer: PersistEventSerializer[State, StateV1] =
    state => {
      val pets = state.pets
      val petsV1 = pets.map(pet => PetsV1(pet._2.id.get, PetV1(pet._2.id.get, pet._2.name))).toSeq
      Right(StateV1(petsV1))
    }

  implicit def petAddedV1PersistEventDeserializer: PersistEventDeserializer[PetAddedV1, PetAdded] = event =>
    Right[Throwable, PetAdded](PetAdded(pet = Pet(id = Some(event.pet.id), name = event.pet.name)))

  implicit def petAddedV1PersistEventSerializer: PersistEventSerializer[PetAdded, PetAddedV1] = event =>
    event.pet.id
      .toRight(new RuntimeException("Deserialization from protobuf failed"))
      .map(petId => PetAddedV1.of(PetV1(id = petId, name = event.pet.name)))

  implicit def petDeletedV1PersistEventDeserializer: PersistEventDeserializer[PetDeletedV1, PetDeleted] =
    event => Right[Throwable, PetDeleted](PetDeleted(petId = event.petId))

  implicit def petDeletedV1PersistEventSerializer: PersistEventSerializer[PetDeleted, PetDeletedV1] =
    event => Right[Throwable, PetDeletedV1](PetDeletedV1.of(event.petId))
}
