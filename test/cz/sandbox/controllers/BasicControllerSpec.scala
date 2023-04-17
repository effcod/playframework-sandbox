package cz.sandbox.controllers

import cz.sandbox.services.DatabaseService
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.{Result, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}


import scala.concurrent.{ExecutionContext, Future}

class BasicControllerSpec extends PlaySpec with MockitoSugar with Results {
  implicit val ec: ExecutionContext = ExecutionContext.global
  val mockDatabaseService: DatabaseService = mock[DatabaseService]
  val controller = new BasicController(Helpers.stubControllerComponents(), mockDatabaseService)

  "BasicController" should {
    "return OK status for healthCheck when database connection is working" in {
      when(mockDatabaseService.checkConnection()).thenReturn(Future.successful(true))
      val result: Future[Result] = controller.healthCheck().apply(FakeRequest())
      status(result) mustBe OK
      Helpers.contentAsString(result) mustBe "Database connection is working"
    }

    "return InternalServerError status for healthCheck when database connection is not working" in {
      when(mockDatabaseService.checkConnection()).thenReturn(Future.successful(false))
      val result: Future[Result] = controller.healthCheck().apply(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
      Helpers.contentAsString(result) mustBe "Database connection is not working"
    }

    "return version number for version" in {
      val result: Future[Result] = controller.version().apply(FakeRequest())
      status(result) mustBe OK
      Helpers.contentAsString(result) mustBe getClass.getPackage.getImplementationVersion
    }
  }
}
