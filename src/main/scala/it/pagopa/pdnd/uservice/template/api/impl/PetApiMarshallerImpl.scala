package it.pagopa.pdnd.uservice.template.api.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import it.pagopa.pdnd.uservice.template.api.PetApiMarshaller
import it.pagopa.pdnd.uservice.template.model.Pet
import spray.json._

class PetApiMarshallerImpl extends PetApiMarshaller with SprayJsonSupport with DefaultJsonProtocol {

  override implicit def fromEntityUnmarshallerPet: FromEntityUnmarshaller[Pet] = sprayJsonUnmarshaller(jsonFormat2(Pet))

  override implicit def toEntityMarshallerPet: ToEntityMarshaller[Pet] =  sprayJsonMarshaller(jsonFormat2(Pet))
}
