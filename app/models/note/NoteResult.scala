/* Generated File */
package models.note

import java.time.LocalDateTime

import io.circe.{Decoder, Encoder}
import models.result.BaseResult
import models.result.filter.Filter
import models.result.orderBy.OrderBy
import models.result.paging.PagingOptions
import io.circe.generic.semiauto._
import io.circe.java8.time._

case class NoteResult(
    override val filters: Seq[Filter] = Nil,
    override val orderBys: Seq[OrderBy] = Nil,
    override val totalCount: Int = 0,
    override val paging: PagingOptions = PagingOptions(),
    override val results: Seq[Note] = Nil,
    override val durationMs: Int = 0,
    override val occurred: LocalDateTime = util.DateUtils.now
) extends BaseResult[Note]

object NoteResult {
  implicit val jsonEncoder: Encoder[NoteResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[NoteResult] = deriveDecoder

  def fromRecords(
    q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int],
    startMs: Long, totalCount: Int, results: Seq[Note]
  ) = {
    val paging = PagingOptions.from(totalCount, limit, offset)
    val durationMs = (System.currentTimeMillis - startMs).toInt
    NoteResult(paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs)
  }
}
