package it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer

import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1.events.{PetAddedV1, PetDeletedV1}
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1.pet.PetV1
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.{PetAdded, PetDeleted}

package object v1 {

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
