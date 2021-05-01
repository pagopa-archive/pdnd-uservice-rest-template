package it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer

import akka.serialization.SerializerWithStringManifest
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.State
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer.v1._

import java.io.NotSerializableException

class StateSerializer extends SerializerWithStringManifest {

  final val version1: String = "1"

  final val currentVersion: String = version1

  override def identifier: Int = 20000

  override def manifest(o: AnyRef): String = s"${o.getClass.getName}|$currentVersion"

  final val StateManifest: String = classOf[State].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: State => serialize(s, StateManifest, currentVersion)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest.split('|').toList match {
      case StateManifest :: `version1` :: Nil =>
        deserialize(v1.state.StateV1, bytes, manifest, currentVersion)
      case _ =>
        throw new NotSerializableException(
          s"Unable to handle manifest: [[$manifest]], currentVersion: [[$currentVersion]] "
        )
}

}