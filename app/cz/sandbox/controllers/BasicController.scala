package cz.sandbox.controllers

import cz.sandbox.services.ConfigDatabaseService
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Using}

class BasicController @Inject()(cc: ControllerComponents, confDatabaseService: ConfigDatabaseService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  def version(): Action[AnyContent] = Action {
    val properties = new java.util.Properties()
    Using(getClass.getResourceAsStream("/version.properties")) { inStream =>
      properties.load(inStream)
      properties.getProperty("version")
    } match {
      case Failure(exception) => InternalServerError("Cannot determine version")
      case Success(value) => Ok(value)
    }
  }

  def healthCheck(): Action[AnyContent] = Action.async {
    confDatabaseService.checkConnection().map { isConnected =>
      if (isConnected) Ok("Database connection is working")
      else InternalServerError("Database connection is not working")
    }
  }
}