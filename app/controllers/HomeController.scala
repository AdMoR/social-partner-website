package controllers

import io.circe.Json
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.util.tracing.TraceData
import play.api.data._
import play.api.data.Forms._

import models.objects.Event

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application) extends AuthController("home") {
  ApplicationDatabase.migrateSafe()

  def old_home() = withSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.index(request.identity, app.config.debug)))
  }

  def home() = withoutSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.home(null /*request.identity*/ , app.config.debug)))
  }

  def listing() = withoutSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.category()))
  }

  def popo() = withoutSession("") { implicit request => implicit td =>
    Future.successful(Ok(views.html.popo()))
  }

  def form() = withoutSession("") { implicit request => implicit td =>
    val eventForm = Form(
      mapping(
        "name" -> text,
        "age" -> number
      )(Event.apply)(Event.unapply)
    )
    Future.successful(Ok(views.html.form(eventForm)))
  }

  def externalLink(url: String) = withSession("external.link") { implicit request => implicit td =>
    Future.successful(Redirect(if (url.startsWith("http")) { url } else { "http://" + url }))
  }

  def ping(timestamp: Long) = withSession("ping") { implicit request => implicit td =>
    Future.successful(Ok(Json.obj("timestamp" -> Json.fromLong(timestamp))))
  }

  def robots() = withSession("robots") { implicit request => implicit td =>
    Future.successful(Ok("User-agent: *\nDisallow: /"))
  }

}
