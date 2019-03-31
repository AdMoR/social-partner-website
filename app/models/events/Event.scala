package models.events

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.models.tag.Tag
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object Event {
  implicit val jsonEncoder: Encoder[Event] = deriveEncoder
  implicit val jsonDecoder: Decoder[Event] = deriveDecoder
}

final case class Event(
    id: UUID = UUID.randomUUID,
    organizerId: UUID = UUID.randomUUID(),
    title: String = "????",
    description: String = ""
) extends DataFieldModel {

  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("organizerId", Some(organizerId.toString)),
    DataField("title", Some(title)),
    DataField("description", Some(description))
  )

}
