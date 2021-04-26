package it.pagopa.pdnd.uservice.resttemplate.model.persistence

import it.pagopa.pdnd.uservice.resttemplate.model.Pet

sealed trait Event extends Persistable

final case class PetAdded(pet: Pet)        extends Event
final case class PetDeleted(petId: String) extends Event
