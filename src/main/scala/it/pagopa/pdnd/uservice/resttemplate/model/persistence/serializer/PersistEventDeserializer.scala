package it.pagopa.pdnd.uservice.resttemplate.model.persistence.serializer

trait PersistEventDeserializer[A, B] {
  def from(a: A): Either[Throwable, B]
}

object PersistEventDeserializer {
  def from[A, B](a: A)(implicit e: PersistEventDeserializer[A, B]): Either[Throwable, B] = e.from(a)
}
