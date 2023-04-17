package cz.sandbox.services

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import java.nio.file.Paths
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Using

class DatabaseServiceSpec extends PlaySpec with GuiceOneAppPerSuite {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure("slick.dbs.default.profile" -> "slick.jdbc.H2Profile$")
      .configure("slick.dbs.default.db.driver" -> "org.h2.Driver")
      .configure("slick.dbs.default.db.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=Oracle")
      .build()
  }

  //https://www.playframework.com/documentation/2.8.x/api/java/play/Environment.html
  def loadTestData(): Unit = {
    //TODO: there must be better way how to read test resources, I tried those without success:
    // play.Environment.simple().resource("testdata.sql") - with different paths
    // play.Environment.simple().classLoader().getResource("testdata.sql") - also different args/paths

    val testpath = Paths.get(play.Environment.simple().rootPath().toPath.toAbsolutePath.toString, "test","resources","testdata.sql")
    val testdataSql = Using(scala.io.Source.fromFile(testpath.toString)) { inStream =>
      inStream
        .getLines()
        .mkString
    }.getOrElse(throw new Exception("Missing version.properties"))

    val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
    val db = dbConfigProvider.get.db
    Await.result(db.run(sqlu"""#$testdataSql"""), Duration.Inf)
  }

  "QueryDatabaseService" should {
    "retrieve data from a dynamic table" in {
      loadTestData()

      val schema = "my_schema"
      val tableName = "test_table"

      val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
      implicit val ec = app.injector.instanceOf[scala.concurrent.ExecutionContext]
      val databaseService = new QueryDatabaseService(dbConfigProvider)

      val resultFuture = databaseService.getData(schema, tableName)

      val result = Await.result(resultFuture, Duration.Inf)
      result mustBe a[List[_]]
      result.headOption.foreach { row =>
        row mustBe a[Map[_, _]]
        row("ID") mustBe a[Long]
        row("NAME") mustBe a[String]
        row("MEASURE_DATE") mustBe a[Timestamp]
        row("HEIGHT") mustBe a[java.math.BigDecimal]
      }
    }
  }
}
