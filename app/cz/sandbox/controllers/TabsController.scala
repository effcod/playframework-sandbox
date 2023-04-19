package cz.sandbox.controllers

import com.google.inject.Inject
import cz.sandbox.errors.UnimplementedException
import cz.sandbox.models.Col
import cz.sandbox.services.impl.{ConfigDatabaseService, QueryDatabaseService}
import play.api.libs.json._
import play.api.mvc._

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class TabsController @Inject()(val controllerComponents: ControllerComponents,
                               val queryDatabaseService: QueryDatabaseService,
                               val configDatabaseService: ConfigDatabaseService)(implicit ec: ExecutionContext)
  extends BaseController {

  private implicit val anyReads: Reads[Any] = {
    case JsString(s) => JsSuccess(s)
    case JsNumber(n) => JsSuccess(n) //for all numbers keep bigdecimal
    case JsNull => JsSuccess(None)
    case x => throw UnimplementedException(s"Conversion from Json type: ${x.getClass.getName} to Database is missing")
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
      for {
        appOpt <- configDatabaseService.getAllApps.map(_.find(_.appName.toUpperCase == appName.toUpperCase))
        tabOpt <- configDatabaseService.getAllTabs(appName).map(_.find(_.tabName.toUpperCase == tabName.toUpperCase))
        result <- (appOpt, tabOpt) match {
          case (Some(app), Some(tab)) if app.isAllowedRestDownload.contains(true) && tab.schema.nonEmpty =>
            // handle case where both app and tab are found and app.isRestAllowed is true and tab.schema is non-empty
            queryDatabaseService.getData(tab.schema, tab.tabName).map { rows =>
              val records = rows.map(row => Json.toJson(row))
              val json = Json.obj("records" -> records)
              val prettyJson = Json.prettyPrint(json)
              Ok(prettyJson)
            }.recover {
              case e: Exception => e.printStackTrace()
                InternalServerError(e.getMessage)
            }
          case (Some(app), Some(tab)) if !app.isAllowedRestDownload.getOrElse(false) =>
            Future.successful(BadRequest("Both app and tab found but app.isRestAllowed is false"))
          case _ =>
            // handle other cases
            Future.successful(BadRequest("Either app or tab not found"))
        }
      } yield result
    }.recover {
      case e: Exception =>
        e.printStackTrace()
        InternalServerError(e.getMessage)
    }
  }

  def retype(values: List[Map[String, Any]], conf: Seq[Col]): List[Map[String, Any]] = {
    val colTypMap = conf.map { column => column.colName.toLowerCase -> column.colType }.toMap
    values.map { rowMap =>
      rowMap
        .filter { case (colName, _) => colTypMap.contains(colName.toLowerCase) } // filter out columns which are not defined in xmdm gui
        .map { case (colName, colValue) => // retype values -> Long -> sql.Date, Number to Double, keep empty as None
          val isEmpty = colValue match {
            case option: Option[Any] => option.isDefined
            case _ => false
          }

          if (isEmpty) (colName, colValue)
          else {
            val recast = colTypMap(colName.toLowerCase).toLowerCase match {
              case "d" => Try(new java.sql.Date(TimeUnit.NANOSECONDS.toMillis(colValue.asInstanceOf[BigDecimal].longValue))).getOrElse(None)
              case "n" => Try(colValue.asInstanceOf[BigDecimal].doubleValue).getOrElse(None)
              case _ => colValue
            }
            (colName, recast)
          }
        }
    }
  }

  def newRows(appName: String, tabName: String): Action[AnyContent] = Action.async { implicit request =>
    handleEmptyParameters(appName, tabName) {
      for {
        appOpt <- configDatabaseService.getAllApps.map(_.find(_.appName.toUpperCase == appName.toUpperCase))
        tabOpt <- configDatabaseService.getAllTabs(appName).map(_.find(_.tabName.toUpperCase == tabName.toUpperCase))
        colsSeq <- configDatabaseService.getAllCols(appName, tabName)
        result <- (appOpt, tabOpt, colsSeq) match {
          case (Some(app), Some(tab), colsSeq) if app.isAllowedRestUpload.contains(true) && tab.schema.nonEmpty =>
            request.body.asJson match {
              case Some(json) =>
                val recordsFromRequest = (json \ "records").as[List[Map[String, Any]]]
                val records = retype(recordsFromRequest, colsSeq)
                queryDatabaseService.insertData(tab.schema, tab.tabName, records).map { _ =>
                  Ok(Json.obj("message" -> "Records processed successfully"))
                }.recover(handleExceptions)
              case None =>
                Future.successful(BadRequest("Invalid JSON data"))
            }
          case (Some(app), Some(tab), colsSeq) if !app.isAllowedRestUpload.getOrElse(false) =>
            Future.successful(BadRequest("Both app and tab found but app.isAllowedRestUpload is false"))
          case _ =>
            // handle other cases
            Future.successful(BadRequest("Either app or tab not found"))
        }
      } yield result
    }.recover(handleExceptions)
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
