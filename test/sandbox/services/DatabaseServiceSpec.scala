package sandbox.services

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import java.io.File
import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatabaseServiceSpec extends PlaySpec with GuiceOneAppPerSuite {

  override def fakeApplication() = {
    new GuiceApplicationBuilder()
      .configure("slick.dbs.default.profile" -> "slick.jdbc.H2Profile$")
      .configure("slick.dbs.default.db.driver" -> "org.h2.Driver")
      .configure("slick.dbs.default.db.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
      .build()
  }

  def loadTestData(): Unit = {
    val testdataPath = getClass.getResource("/testdata.sql").getPath
    val testdataSql = new String(Files.readAllBytes(new File(testdataPath).toPath))
    val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
    val db = dbConfigProvider.get.db
    Await.result(db.run(sqlu"""#$testdataSql"""), Duration.Inf)
  }

  "DatabaseService" should {
    "retrieve data from a dynamic table" in {
      loadTestData()

      val schema = "my_schema"
      val tableName = "test_table"

      val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
      implicit val ec = app.injector.instanceOf[scala.concurrent.ExecutionContext]
      val databaseService = new DatabaseService(dbConfigProvider)

      val resultFuture = databaseService.getData(schema, tableName)

      val result = Await.result(resultFuture, Duration.Inf)
      result mustBe a[List[_]]
      result.headOption.foreach { row =>
        row mustBe a[Map[_, _]]
        row("id") mustBe a[Long]
        row("name") mustBe a[String]
        row("measure_date") mustBe a[Long] // The date is converted to Long millisec from epoch
        row("height") mustBe a[java.math.BigDecimal]
      }
    }
  }
}
