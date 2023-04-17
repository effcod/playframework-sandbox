package cz.sandbox.controllers

import com.google.inject.Inject
import cz.sandbox.errors.UnimplementedException
import cz.sandbox.services.DatabaseService
import play.api.libs.json._
import play.api.mvc._

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

class TabsController @Inject()(val controllerComponents: ControllerComponents,
                               val databaseService: DatabaseService)(implicit ec: ExecutionContext)
  extends BaseController {

  private implicit val anyReads: Reads[Any] = {
    case JsString(s) => JsSuccess(s)
    case JsNumber(n) => JsSuccess(n.toInt) // double?
    case _ => JsError("Unsupported type")
  }

  private implicit val anyWrites: Writes[Any] = {
    case s: String => JsString(s)
    case i: Int => JsNumber(i)
    case l: Long => JsNumber(l)
    case bJ: java.math.BigDecimal => JsNumber(BigDecimal(bJ.toString))
    case bS: scala.math.BigDecimal => JsNumber(BigDecimal(bS.toString))
    case t: java.sql.Timestamp => JsNumber(TimeUnit.MILLISECONDS.toNanos(t.toInstant.toEpochMilli))
    case d: java.sql.Date => JsNumber(TimeUnit.MILLISECONDS.toNanos(d.toInstant.toEpochMilli))
    case null => JsNull
    case x => throw UnimplementedException(s"Conversion from database type: ${x.getClass.getName} to Json is missing")
  }

  def getRows(appName: String, tabName: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    handleEmptyParameters(appName, tabName) {
      databaseService.getData(appName, tabName).map { rows =>
        val records = rows.map(row => Json.toJson(row))
        val json = Json.obj("records" -> records)
        Ok(json)
      }.recover {
        case e: Exception => e.printStackTrace()
          InternalServerError(e.getMessage)
      }
    }
  }

  def newRows(appName: String, tabName: String): Action[AnyContent] = Action.async { implicit request =>
    handleEmptyParameters(appName, tabName) {
      request.body.asJson match {
        case Some(json) =>
          val records = (json \ "records").as[List[Map[String, Any]]]
          databaseService.insertData(appName, tabName, records).map { _ =>
            Ok(Json.obj("message" -> "Records processed successfully"))
          }.recover(handleExceptions)
        case None =>
          Future.successful(BadRequest("Invalid JSON data"))
      }
    }
  }

  private def handleEmptyParameters(appName: String, tabName: String)(f: => Future[Result]): Future[Result] = {
    if (appName.isEmpty || tabName.isEmpty) {
      Future.successful(BadRequest("appName and tabName must not be empty"))
    } else {
      f
    }
  }

  private def handleExceptions: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      InternalServerError(e.getMessage)
  }


}
