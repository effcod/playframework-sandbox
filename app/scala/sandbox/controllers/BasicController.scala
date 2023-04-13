package sandbox.controllers

import play.api.mvc._
import sandbox.services.DatabaseService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BasicController @Inject()(cc: ControllerComponents, databaseService: DatabaseService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def healthCheck(): Action[AnyContent] = Action.async {
    databaseService.checkConnection().map { isConnected =>
      if (isConnected) Ok("Database connection is working")
      else InternalServerError("Database connection is not working")
    }
  }
  def version(): Action[AnyContent] = Action {
    val version = getClass.getPackage.getImplementationVersion
    Ok(version)
  }
}