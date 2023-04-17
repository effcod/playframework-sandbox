package cz.sandbox.errors

import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Singleton
import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      // Your custom error page can go here.
      Status(statusCode)(s"A client error occurred: $message")
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception.printStackTrace()
    exception match {
      case e: DatabaseException => Future.successful(BadRequest(e.getMessage))
      case e: InvalidRequestException => Future.successful(BadRequest(e.getMessage))
      case e: Exception => Future.successful(InternalServerError(e.getMessage))
      case _ => Future.successful(ServiceUnavailable("Unknown issue, please try again later"))
    }
  }
}
