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

class EventController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
) extends AbstractController(components) with I18nSupport {

  var events = Map[Long, Event](0.toLong -> Event(0.toLong, "My first event", "Cool stuff", "Amicale scolaire",
    List("Entraide"), "Entraide"), 1.toLong -> Event(1, "My first event", "Cool stuff", "Amicale scolaire", List("Entraide"), "Entraide"))
  var eventCounter = 2.toLong

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
    Future.successful(Ok(views.html.category(events.values.toList, Option(request.identity))))
  }

  def listing(userId: String) = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.category(List(events { userId.toLong }), Option(request.identity))))
  }

  def event(userId: String) = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.event_page(events { userId.toLong }, Option(request.identity))))
  }

  def form() = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    eventCounter += 1
    Future.successful(Ok(views.html.form(eventForm, user = Option(request.identity))))
  }

  def submit = Action { implicit request =>
    val event = eventForm.bindFromRequest.get
    events = events + (eventCounter -> event)
    Redirect(s"event/${eventCounter.toString}")
  }

}