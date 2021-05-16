package it.pagopa.pdnd.uservice.resttemplate.api.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import it.pagopa.pdnd.uservice.resttemplate.api.PetApiMarshaller
import it.pagopa.pdnd.uservice.resttemplate.model.Pet
import spray.json._

class PetApiMarshallerImpl extends PetApiMarshaller with SprayJsonSupport with DefaultJsonProtocol {

  override implicit def fromEntityUnmarshallerPet: FromEntityUnmarshaller[Pet] =
    sprayJsonUnmarshaller[Pet](jsonFormat2(Pet))

  override implicit def toEntityMarshallerPet: ToEntityMarshaller[Pet] = sprayJsonMarshaller[Pet](jsonFormat2(Pet))

  implicit val petFormat: RootJsonFormat[Pet] = jsonFormat2(Pet)

  override implicit def toEntityMarshallerPetarray: ToEntityMarshaller[Seq[Pet]] = sprayJsonMarshaller
}
