package it.pagopa.pdnd.uservice.template.api.impl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import it.pagopa.pdnd.uservice.template.api.ProbeApiService
import spray.json.DefaultJsonProtocol

class ProbeApiServiceImpl extends ProbeApiService with SprayJsonSupport with DefaultJsonProtocol {

  /**
   * Code: 200, Message: aaa, DataType: String
   */
  override def probe(): Route = probe200("OK")(sprayJsonMarshaller(StringJsonFormat.write(_)))
}
