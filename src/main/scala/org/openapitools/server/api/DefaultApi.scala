package org.openapitools.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import org.openapitools.server.AkkaHttpHelper._
import org.openapitools.server.model.Artist
import org.openapitools.server.model.InlineResponse200
import org.openapitools.server.model.InlineResponse400


class DefaultApi(
    defaultService: DefaultApiService,
    defaultMarshaller: DefaultApiMarshaller
) {

  
  import defaultMarshaller._

  lazy val route: Route =
    path("artists") { 
      get { 
        parameters("limit".as[Int].?, "offset".as[Int].?) { (limit, offset) => 
            defaultService.artistsGet(limit = limit, offset = offset)
        }
      }
    } ~
    path("artists") { 
      post {  
            entity(as[Artist]){ artist =>
              defaultService.artistsPost(artist = artist)
            }
      }
    } ~
    path("artists" / Segment) { (username) => 
      get {  
            defaultService.artistsUsernameGet(username = username)
      }
    }
}


trait DefaultApiService {

  def artistsGet200(responseArtistarray: Seq[Artist])(implicit toEntityMarshallerArtistarray: ToEntityMarshaller[Seq[Artist]]): Route =
    complete((200, responseArtistarray))
  def artistsGet400(responseInlineResponse400: InlineResponse400)(implicit toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]): Route =
    complete((400, responseInlineResponse400))
  /**
   * Code: 200, Message: Successfully returned a list of artists, DataType: Seq[Artist]
   * Code: 400, Message: Invalid request, DataType: InlineResponse400
   */
  def artistsGet(limit: Option[Int], offset: Option[Int])
      (implicit toEntityMarshallerArtistarray: ToEntityMarshaller[Seq[Artist]], toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]): Route

  def artistsPost200: Route =
    complete((200, "Successfully created a new artist"))
  def artistsPost400(responseInlineResponse400: InlineResponse400)(implicit toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]): Route =
    complete((400, responseInlineResponse400))
  /**
   * Code: 200, Message: Successfully created a new artist
   * Code: 400, Message: Invalid request, DataType: InlineResponse400
   */
  def artistsPost(artist: Artist)
      (implicit toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]): Route

  def artistsUsernameGet200(responseInlineResponse200: InlineResponse200)(implicit toEntityMarshallerInlineResponse200: ToEntityMarshaller[InlineResponse200]): Route =
    complete((200, responseInlineResponse200))
  def artistsUsernameGet400(responseInlineResponse400: InlineResponse400)(implicit toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]): Route =
    complete((400, responseInlineResponse400))
  /**
   * Code: 200, Message: Successfully returned an artist, DataType: InlineResponse200
   * Code: 400, Message: Invalid request, DataType: InlineResponse400
   */
  def artistsUsernameGet(username: String)
      (implicit toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400], toEntityMarshallerInlineResponse200: ToEntityMarshaller[InlineResponse200]): Route

}

trait DefaultApiMarshaller {
  implicit def fromEntityUnmarshallerArtist: FromEntityUnmarshaller[Artist]



  implicit def toEntityMarshallerArtistarray: ToEntityMarshaller[Seq[Artist]]

  implicit def toEntityMarshallerInlineResponse400: ToEntityMarshaller[InlineResponse400]

  implicit def toEntityMarshallerInlineResponse200: ToEntityMarshaller[InlineResponse200]

}

