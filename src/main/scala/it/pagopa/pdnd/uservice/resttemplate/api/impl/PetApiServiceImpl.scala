package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import it.pagopa.pdnd.uservice.resttemplate.api.PetApiService
import it.pagopa.pdnd.uservice.resttemplate.model.Pet

class PetApiServiceImpl extends PetApiService {
  /**
   * Code: 405, Message: Invalid input
   */
  override def addPet(pet: Pet): Route = ???

  /**
   * Code: 400, Message: Invalid ID supplied
   * Code: 404, Message: Pet not found
   */
  override def deletePet(petId: Long): Route = ???

  /**
   * Code: 200, Message: successful operation, DataType: Pet
   * Code: 400, Message: Invalid ID supplied
   * Code: 404, Message: Pet not found
   */
  override def getPetById(petId: Long)(implicit toEntityMarshaller: ToEntityMarshaller[Pet]): Route = getPetById200(Pet(Some(1), "Romeo"))
}
