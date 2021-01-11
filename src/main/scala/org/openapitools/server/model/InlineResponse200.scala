package org.openapitools.server.model


/**
 * @param artistName  for example: ''null''
 * @param artistGenre  for example: ''null''
 * @param albumsRecorded  for example: ''null''
*/
final case class InlineResponse200 (
  artistName: Option[String],
  artistGenre: Option[String],
  albumsRecorded: Option[Int]
)

