package models.audit

import java.util.UUID

import graphql.{GraphQLContext, GraphQLSchemaHelper}
import graphql.CommonSchema._
import graphql.DateTimeSchema._
import models.audit.AuditRecordSchema._
import models.note.NoteSchema
import models.result.data.DataFieldSchema
import models.result.filter.FilterSchema._
import models.result.orderBy.OrderBySchema._
import models.result.paging.PagingSchema.pagingOptionsType
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.macros.derive._
import sangria.schema._
import util.FutureUtils.graphQlContext

import scala.concurrent.Future

object AuditSchema extends GraphQLSchemaHelper("audit") {
  implicit val auditPrimaryKeyId: HasId[Audit, UUID] = HasId[Audit, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = c.app.coreServices.audits.getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val auditByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val auditIdArg = Argument("id", uuidType, description = "Returns the Audit matching the provided Id.")

  val auditByUserIdRelation = Relation[Audit, UUID]("byUserId", x => Seq(x.id))
  val auditByUserIdFetcher = Fetcher.rel[GraphQLContext, Audit, Audit, UUID](
    getByPrimaryKeySeq, (c, rels) => c.app.coreServices.audits.getByUserIdSeq(c.creds, rels(auditByUserIdRelation))(c.trace)
  )

  implicit lazy val auditType: ObjectType[GraphQLContext, Audit] = deriveObjectType(
    AddFields(
      Field(
        name = "auditRecordAuditIdFkey",
        fieldType = ListType(AuditRecordSchema.auditRecordType),
        resolve = c => AuditRecordSchema.auditRecordByAuditIdFetcher.deferRelSeq(
          AuditRecordSchema.auditRecordByAuditIdRelation, c.value.id
        )
      ),
      Field(
        name = "relatedNotes",
        fieldType = ListType(NoteSchema.noteType),
        resolve = c => c.ctx.app.coreServices.notes.getFor(c.ctx.creds, "audit", c.value.id)(c.ctx.trace)
      )
    )
  )

  implicit lazy val auditResultType: ObjectType[GraphQLContext, AuditResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "audit", desc = None, t = OptionType(auditType), f = (c, td) => {
      c.ctx.app.coreServices.audits.getByPrimaryKey(c.ctx.creds, c.arg(auditIdArg))(td)
    }, auditIdArg),
    unitField(name = "audits", desc = None, t = auditResultType, f = (c, td) => {
      runSearch(c.ctx.app.coreServices.audits, c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg)
  )

  val auditMutationType = ObjectType(
    name = "audit",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(auditType), f = (c, td) => {
        c.ctx.app.coreServices.audits.create(c.ctx.creds, c.arg(DataFieldSchema.dataFieldsArg))(td)
      }, DataFieldSchema.dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(auditType), f = (c, td) => {
        c.ctx.app.coreServices.audits.update(c.ctx.creds, c.arg(auditIdArg), c.arg(DataFieldSchema.dataFieldsArg))(td).map(_._1)
      }, auditIdArg, DataFieldSchema.dataFieldsArg),
      unitField(name = "remove", desc = None, t = auditType, f = (c, td) => {
        c.ctx.app.coreServices.audits.remove(c.ctx.creds, c.arg(auditIdArg))(td)
      }, auditIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "audit", desc = None, t = auditMutationType, f = (c, td) => Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[Audit]) = {
    AuditResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
