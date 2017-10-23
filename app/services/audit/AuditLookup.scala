package services.audit

import models.result.data.DataFieldModel
import services.ServiceRegistry
import services.audit.AuditArgs._
import util.Logging
import util.tracing.TraceData

@javax.inject.Singleton
class AuditLookup @javax.inject.Inject() (registry: ServiceRegistry) extends Logging {
  def getByPk(model: String, pk: String*)(implicit traceData: TraceData) = getModel(model, getArg(pk, _))

  private[this] def getModel(key: String, arg: (Int) => String)(implicit traceData: TraceData): Option[DataFieldModel] = key.toLowerCase match {
    /* Start registry lookups */

    case "auditrecord" => registry.auditServices.auditRecordService.getByPrimaryKey(uuidArg(arg(0)))
    case "note" => registry.noteServices.noteService.getByPrimaryKey(uuidArg(arg(0)))
    case "user" => registry.userServices.userService.getByPrimaryKey(uuidArg(arg(0)))

    /* End registry lookups */
    case _ =>
      log.warn(s"Attempted to load invalid object type [$key:${arg(0)}].")
      None
  }
}
