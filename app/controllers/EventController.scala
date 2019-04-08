package controllers

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.database.ApplicationDatabase
import play.filters.csrf._
import play.api.data._
import play.api.data.Forms._
import models.objects.{Event}
import play.api.mvc.MessagesActionBuilder
import play.api.mvc.Results._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class EventController @javax.inject.Inject() (override val app: Application, messagesAction: MessagesActionBuilder)
  extends AuthController("home") with play.api.i18n.I18nSupport {
  ApplicationDatabase.migrateSafe()

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

  def listings() = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.category(events.values.toList)))
  }

  def listing(userId: String) = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.category(List(events { userId.toLong }))))
  }

  def event(userId: String) = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.event_page(events { userId.toLong })))
  }

  def form() = withoutSession("") { implicit request => implicit td =>
    eventCounter += 1
    Future.successful(Ok(views.html.form(eventForm)))
  }

  def submit = Action { implicit request =>
    val event = eventForm.bindFromRequest.get
    events = events + (eventCounter -> event)
    Redirect(s"event/${eventCounter.toString}")
  }

}