package controllers

import io.circe.Json
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.database.ApplicationDatabase
import play.api.mvc.MessagesActionBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application, messagesAction: MessagesActionBuilder)
  extends AuthController("home") with play.api.i18n.I18nSupport {
  ApplicationDatabase.migrateSafe()

  def home() = withoutSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.home(null /*request.identity*/ , app.config.debug)))
  }

  def about() = withoutSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.about()))
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
