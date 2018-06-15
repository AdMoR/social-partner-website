/* Generated File */
package models.note

import java.util.UUID
import models.graphql.{GraphQLContext, GraphQLSchemaHelper}
import models.graphql.CommonSchema._
import models.graphql.DateTimeSchema._
import models.result.data.DataFieldSchema
import models.result.filter.FilterSchema._
import models.result.orderBy.OrderBySchema._
import models.result.paging.PagingSchema.pagingOptionsType
import models.user.SystemUserSchema
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.macros.derive._
import sangria.schema._
import scala.concurrent.Future
import util.FutureUtils.graphQlContext

object NoteSchema extends GraphQLSchemaHelper("note") {
  implicit val notePrimaryKeyId: HasId[Note, UUID] = HasId[Note, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.services.noteServices.noteService.getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val noteByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val noteIdArg = Argument("id", uuidType)
  val noteIdSeqArg = Argument("ids", ListInputType(uuidType))

  val noteByAuthorRelation = Relation[Note, UUID]("byAuthor", x => Seq(x.author))
  val noteByAuthorFetcher = Fetcher.rel[GraphQLContext, Note, Note, UUID](
    getByPrimaryKeySeq, (c, rels) => c.services.noteServices.noteService.getByAuthorSeq(c.creds, rels(noteByAuthorRelation))(c.trace)
  )

  implicit lazy val noteType: ObjectType[GraphQLContext, Note] = deriveObjectType(
    AddFields(
      Field(
        name = "authorUser",
        fieldType = SystemUserSchema.systemUserType,
        resolve = ctx => SystemUserSchema.systemUserByPrimaryKeyFetcher.defer(ctx.value.author)
      ),
      Field(
        name = "relatedNotes",
        fieldType = ListType(NoteSchema.noteType),
        resolve = c => c.ctx.app.coreServices.notes.getFor(c.ctx.creds, "note", c.value.id)(c.ctx.trace)
      )
    )
  )

  implicit lazy val noteResultType: ObjectType[GraphQLContext, NoteResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "note", desc = None, t = OptionType(noteType), f = (c, td) => {
      c.ctx.services.noteServices.noteService.getByPrimaryKey(c.ctx.creds, c.arg(noteIdArg))(td)
    }, noteIdArg),
    unitField(name = "noteSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.services.noteServices.noteService.getByPrimaryKeySeq(c.ctx.creds, c.arg(noteIdSeqArg))(td)
    }, noteIdSeqArg),
    unitField(name = "noteSearch", desc = None, t = noteResultType, f = (c, td) => {
      runSearch(c.ctx.services.noteServices.noteService, c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg)
  )

  val noteMutationType = ObjectType(
    name = "NoteMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(noteType), f = (c, td) => {
        c.ctx.services.noteServices.noteService.create(c.ctx.creds, c.arg(DataFieldSchema.dataFieldsArg))(td)
      }, DataFieldSchema.dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(noteType), f = (c, td) => {
        c.ctx.services.noteServices.noteService.update(c.ctx.creds, c.arg(noteIdArg), c.arg(DataFieldSchema.dataFieldsArg))(td).map(_._1)
      }, noteIdArg, DataFieldSchema.dataFieldsArg),
      unitField(name = "remove", desc = None, t = noteType, f = (c, td) => {
        c.ctx.services.noteServices.noteService.remove(c.ctx.creds, c.arg(noteIdArg))(td)
      }, noteIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "note", desc = None, t = noteMutationType, f = (c, td) => Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[Note]) = {
    NoteResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
