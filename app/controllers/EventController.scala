package controllers

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.database.ApplicationDatabase
import play.filters.csrf._
import play.api.data._
import play.api.data.Forms._
import models.objects.{Event}
import play.api.mvc.MessagesActionBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class EventController @javax.inject.Inject() (override val app: Application, messagesAction: MessagesActionBuilder)
  extends AuthController("home") with play.api.i18n.I18nSupport {
  ApplicationDatabase.migrateSafe()

  var events = List[Event]()

  val eventForm = Form(
    mapping(
      "id" -> ignored(1234.toLong),
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "groupName" -> text,
      "tags" -> list(text),
      "eventType" -> text
    )(Event.apply)(Event.unapply)
  )

  def listing() = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.category(events)))
  }

  def form() = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.form(eventForm)))
  }

  def submit = Action { implicit request =>
    val event = eventForm.bindFromRequest.get
    events = events :+ event
    Ok("Hi %s".format(event))
  }

}