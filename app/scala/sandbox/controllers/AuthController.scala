package sandbox.controllers

import com.google.inject.{Inject, Singleton}
import io.swagger.annotations.Api
import play.api.mvc._

@Singleton
@Api(value = "/Auth controller")
class AuthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Results.NotFound("Unimplemented.")
  }

  def logout(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Results.NotFound("Unimplemented.")
  }
}