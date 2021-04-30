package it.pagopa.pdnd.uservice.resttemplate.model.persistence

import it.pagopa.pdnd.uservice.resttemplate.model.Pet

final case class State(pets: Map[String, Pet]) extends Persistable {

  def add(pet: Pet): State = pet.id.fold(this)(petId => copy(pets = pets + (petId -> pet)))

  def delete(petId: String): State = copy(pets = pets - petId)

}

object State {
  val empty: State = State(pets = Map.empty[String, Pet])
}
