package models.graphql

import java.time.LocalDateTime

import models.result.filter.{Filter, FilterSchema}
import models.result.orderBy.{OrderBy, OrderBySchema}
import models.result.paging.PagingOptions
import sangria.schema.{Args, Context}
import services.ModelServiceHelper
import util.tracing.TraceData
import util.FutureUtils.graphQlContext

import scala.concurrent.Future

abstract class SchemaHelper(val name: String) {
  protected def traceF[A](ctx: GraphQLContext, k: String)(f: TraceData => Future[A]) = ctx.app.tracing.trace(name + ".schema." + k)(f)(ctx.trace)
  protected def traceB[A](ctx: GraphQLContext, k: String)(f: TraceData => A) = ctx.app.tracing.traceBlocking(name + ".schema." + k)(f)(ctx.trace)

  protected case class SearchArgs(start: LocalDateTime, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])
  protected case class SearchResult[T](count: Int, results: Seq[T], args: SearchArgs) {
    val paging = PagingOptions.from(count, args.limit, args.offset)
    val dur = (System.currentTimeMillis - util.DateUtils.toMillis(args.start)).toInt
  }

  def argsFor(args: Args) = SearchArgs(
    start = util.DateUtils.now,
    filters = args.arg(FilterSchema.reportFiltersArg).getOrElse(Nil),
    orderBys = args.arg(OrderBySchema.orderBysArg).getOrElse(Nil),
    limit = args.arg(CommonSchema.limitArg),
    offset = args.arg(CommonSchema.offsetArg)
  )

  def runSearch[T](svc: ModelServiceHelper[T], c: Context[GraphQLContext, Unit], td: TraceData) = {
    val args = argsFor(c.args)
    val f = c.arg(CommonSchema.queryArg) match {
      case Some(q) => svc.searchWithCount(c.ctx.creds, q, args.filters, args.orderBys, args.limit, args.offset)(td)
      case _ => svc.getAllWithCount(c.ctx.creds, args.filters, args.orderBys, args.limit, args.offset)(td)
    }
    c.ctx.trace.annotate("Composing search result.")
    f.map(x => SearchResult(x._1, x._2, args))
  }
}
