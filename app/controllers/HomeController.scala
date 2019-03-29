package controllers

import io.circe.Json
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.util.tracing.TraceData
import play.filters.csrf._
import play.api.data._
import play.api.data.Forms._
import models.objects.{Event, Contact, ContactInformation}
import play.api.mvc.MessagesActionBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application, messagesAction: MessagesActionBuilder)
  extends AuthController("home") with play.api.i18n.I18nSupport {
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

  val contactForm: Form[Contact] = Form(

    // Defines a mapping that will handle Contact values
    mapping(
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "company" -> optional(text),

      // Defines a repeated mapping
      "informations" -> seq(
        mapping(
          "label" -> nonEmptyText,
          "email" -> optional(email),
          "phones" -> list(
            text verifying pattern("""[0-9.+]+""".r, error="A valid phone number is required")
          )
        )(ContactInformation.apply)(ContactInformation.unapply)
      )
    )(Contact.apply)(Contact.unapply)
  )

  def editContact = Action { implicit request =>
    val existingContact = Contact(
      "Fake", "Contact", Some("Fake company"), informations = List(
        ContactInformation(
          "Personal", Some("fakecontact@gmail.com"), List("01.23.45.67.89", "98.76.54.32.10")
        ),
        ContactInformation(
          "Professional", Some("fakecontact@company.com"), List("01.23.45.67.89")
        ),
        ContactInformation(
          "Previous", Some("fakecontact@oldcompany.com"), List()
        )
      )
    )
    Ok(views.html.contact.form(contactForm.fill(existingContact)))
  }

  def saveContact = Action { implicit request =>
    contactForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.contact.form(formWithErrors))
      },
      contact => {
        val contactId = Contact.save(contact)
        Redirect(routes.HomeController.showContact(contactId)).flashing("success" -> "Contact saved!")
      }
    )
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
