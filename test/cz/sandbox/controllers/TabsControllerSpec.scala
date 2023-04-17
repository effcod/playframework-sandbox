package cz.sandbox.controllers

import cz.sandbox.services.{ConfigDatabaseService, QueryDatabaseService}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import cz.sandbox.models.App

import scala.concurrent.{ExecutionContext, Future}

class TabsControllerSpec extends PlaySpec with MockitoSugar with Results {
  implicit val ec: ExecutionContext = ExecutionContext.global
  val mockQueryDatabaseService: QueryDatabaseService = mock[QueryDatabaseService]
  val mockConfigDatabaseService: ConfigDatabaseService = mock[ConfigDatabaseService]
  val controller = new TabsController(stubControllerComponents(), mockQueryDatabaseService, mockConfigDatabaseService)

  "TabsController#getRows" should {
    "return BadRequest if appName or tabName are empty" in {
      val result1: Future[Result] = controller.getRows("", "tabName").apply(FakeRequest())
      status(result1) mustBe BAD_REQUEST
      contentAsString(result1) mustBe "appName and tabName must not be empty"

      val result2: Future[Result] = controller.getRows("appName", "").apply(FakeRequest())
      status(result2) mustBe BAD_REQUEST
      contentAsString(result2) mustBe "appName and tabName must not be empty"
    }

    "return Forbidden if REST is not allowed for the given appName" in {
      when(mockConfigDatabaseService.getAllApps).thenReturn(Future.successful(Seq(App("appName", "appDesc", Some(false)))))

      val result: Future[Result] = controller.getRows("appName", "tabName").apply(FakeRequest())
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe "Not allowed"
    }

    "return JSON data if appName and tabName are non-empty and REST is allowed for the given appName" in {
      when(mockConfigDatabaseService.getAllApps).thenReturn(Future.successful(Seq(App("appName", "appDesc", Some(true)))))
      when(mockQueryDatabaseService.getData("appName", "tabName")).thenReturn(Future.successful(List(Map("key" -> "value"))))

      val result: Future[Result] = controller.getRows("appName", "tabName").apply(FakeRequest())
      status(result) mustBe OK
      val json = contentAsJson(result)
      json mustBe Json.obj("records" -> Json.arr(Json.obj("key" -> "value")))
    }

    "return InternalServerError if an exception is thrown" in {
      when(mockConfigDatabaseService.getAllApps).thenReturn(Future.failed(new Exception("error message")))

      val result: Future[Result] = controller.getRows("appName", "tabName").apply(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "error message"
    }
  }

  "TabsController#newRows" should {
    "return BadRequest if appName or tabName are empty" in {
      val result1: Future[Result] = controller.newRows("", "tabName").apply(FakeRequest())
      status(result1) mustBe BAD_REQUEST
      contentAsString(result1) mustBe "appName and tabName must not be empty"

      val result2: Future[Result] = controller.newRows("appName", "").apply(FakeRequest())
      status(result2) mustBe BAD_REQUEST
      contentAsString(result2) mustBe "appName and tabName must not be empty"
    }

    "return BadRequest if request body is not valid JSON" in {
      val result: Future[Result] = controller.newRows("appName", "tabName").apply(FakeRequest().withTextBody("invalid"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Invalid JSON data"
    }

    "return Ok if request body is valid JSON and insertData succeeds" in {
      when(mockQueryDatabaseService.insertData("appName", "tabName", List(Map("key" -> "value")))).thenReturn(Future.successful(()))

      val json = Json.obj("records" -> Json.arr(Json.obj("key" -> "value")))
      val result: Future[Result] = controller.newRows("appName", "tabName").apply(FakeRequest().withJsonBody(json))
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("message" -> "Records processed successfully")
    }

    "return InternalServerError if insertData throws an exception" in {
      when(mockQueryDatabaseService.insertData("appName", "tabName", List(Map("key" -> "value")))).thenReturn(Future.failed(new Exception("error message")))

      val json = Json.obj("records" -> Json.arr(Json.obj("key" -> "value")))
      val result: Future[Result] = controller.newRows("appName", "tabName").apply(FakeRequest().withJsonBody(json))
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "error message"
    }
  }
}