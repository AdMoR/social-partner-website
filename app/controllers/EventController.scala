package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.mvc.Action
import models.events.Event
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, MessagesActionBuilder }
import play.api.mvc.Results._
import utils.auth.DefaultEnv
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import io.getquill._

class EventController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
) extends AbstractController(components) with I18nSupport {

  lazy val ctx = new PostgresJdbcContext(CamelCase, "ctx")
  import ctx._

  // Initial query to populate the db
  val q = quote {
    query[Event]
  }
  var eventCounter = ctx.run(q.size).toInt
  if (eventCounter == 0) {
    val a = quote(query[Event].insert(lift(Event(0.toLong, "My first event", "Cool stuff", "Amicale scolaire", List("Entraide"), "Entraide"))))
    println(a)
    ctx.run(a)
  }

  val eventForm = Form(
    mapping(
      "id" -> ignored(eventCounter.toLong),
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "groupName" -> text,
      "tags" -> list(text),
      "eventType" -> text
    )(Event.apply)(Event.unapply)
  )

  def listings() = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val q = quote {
      query[Event].take(10)
    }
    val events = ctx.run(q)
    Future.successful(Ok(views.html.category(events.toList, Option(request.identity))))
  }

  def event(userId: String) = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val q = quote {
      query[Event].filter(e => e.id == lift(userId).toLong).take(1)
    }
    val events = ctx.run(q)
    println(events, userId)
    Future.successful(Ok(views.html.category(events, Option(request.identity))))
  }

  def form() = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.form(eventForm, user = Option(request.identity))))
  }

  def submit = silhouette.SecuredAction.async { implicit request =>
    eventCounter += 1
    val event = eventForm.bindFromRequest.get
    val a = quote(query[Event].insert(lift(event)))
    println(a)
    ctx.run(a)
    Future.successful(Redirect(s"event/${eventCounter.toString}"))
  }

}