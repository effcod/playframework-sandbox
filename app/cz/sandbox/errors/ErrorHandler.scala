package cz.sandbox.errors


import com.typesafe.scalalogging.LazyLogging
import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Singleton
import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler with LazyLogging  {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.error(s"Client error: $statusCode - $message. RequestHeader:${request.toString}")
    statusCode match {
      case BAD_REQUEST => Future.successful(BadRequest("The request was invalid or cannot be served"))
      case FORBIDDEN => Future.successful(Forbidden("You are not authorized to access this resource"))
      case NOT_FOUND => Future.successful(NotFound("The requested page could not be found"))
      case _ => Future.successful(Status(statusCode)(s"A client error occurred: $message"))
    }
  }


  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(s"Server Error: ${exception.getMessage}. RequestHeader:${request.toString}", exception)
    exception match {
      case e: DatabaseException => Future.successful(BadRequest(e.getMessage))
      case e: InvalidRequestException => Future.successful(BadRequest(e.getMessage))
      case e: Exception => Future.successful(InternalServerError(e.getMessage))
      case _ => Future.successful(ServiceUnavailable("Unknown issue, please try again later"))
    }
  }
}
