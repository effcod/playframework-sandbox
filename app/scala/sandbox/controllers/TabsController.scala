package sandbox.controllers

import com.google.inject.Inject
import play.api.libs.json._
import play.api.mvc._
import sandbox.services.DatabaseService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class TabsController @Inject()(val controllerComponents: ControllerComponents,
                               val databaseService: DatabaseService)(implicit ec: ExecutionContext)
  extends BaseController {

  def getRows(appName: String, tabName: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    implicit val anyWrites = new Writes[Any] {
      def writes(o: Any): JsValue = o match {
        case s: String => JsString(s)
        case i: Int => JsNumber(i)
        case _ => JsNull
      }
    }


    val resultFuture = databaseService.getData(appName, tabName)
    val result = Await.result(resultFuture, Duration.Inf)

    val data: List[Map[String, Any]] = List(Map("name" -> "Peter", "age" -> 10), Map("name" -> "Julia", "age" -> 20))

    val json = Json.obj(
      "records" -> data.map(Json.toJson(_))
    )

    println(Json.prettyPrint(json))

    Results.NotFound(s"Unimplemented. $appName, $tabName")
  }

  def newRows(appName: String, tabName: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val anyReads = new Reads[Any] {
      def reads(json: JsValue): JsResult[Any] = json match {
        case JsString(s) => JsSuccess(s)
        case JsNumber(n) => JsSuccess(n.toInt) // double?
        case _ => JsError("Unsupported type")
      }
    }

    val json = request.body.asJson

    val records = (json.get \ "records").as[List[Map[String, Any]]]

    databaseService.insertData(appName, tabName, records)

    //Results.NotFound(s"Unimplemented. $appName, $tabName")
    Future.successful(Ok(Json.obj("message" -> "Records processed successfully")))
  }

  /*
  def newRows(schemaName: String, tableName: String): Action[AnyContent] = Action.async { implicit request =>
    val recordsResult: JsResult[List[Map[String, Any]]] =
      request.body
        .asJson
        .flatMap(json => json("records").asOpt[List[Map[String, Any]]]) match {
      case Some(records) => JsSuccess(records)
      case None => JsError("Invalid JSON format")
    }

    recordsResult match {
      case JsSuccess(records, _) =>
        // Process the records here, for example, insert them into the database
        // ...
        // After processing, return a successful response
        Future.successful(Ok(Json.obj("message" -> "Records processed successfully")))

      case JsError(errors) =>
        // Handle the error case
        Future.successful(BadRequest(Json.obj("message" -> "Invalid JSON format")))
    }
  }*/
}
