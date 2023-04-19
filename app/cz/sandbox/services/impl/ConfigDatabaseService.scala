package cz.sandbox.services.impl

import cz.sandbox.models.{App, Col, Tab}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


case class AppRow(appName: String,
                  description: String,
                  settings: String
                 )

case class TabRow(appName: String,
                  tabName: String,
                  schema: String
                 )

case class ColRow(appName: String,
                  tabName: String,
                  colName: String,
                  colPk: String,
                  colRequired: String,
                  colType: String,
                  colFormat: Option[String]
                 )

trait AppTableComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class AppTable(tag: Tag) extends Table[AppRow](tag/*, _schemaName = Some("main_schema")*/, _tableName = "APP") {
    def appName = column[String]("APP_NAME")
    def description = column[String]("APP_DESC")
    def settings = column[String]("SETTINGS")

    def * = (appName, description, settings) <> (AppRow.tupled, AppRow.unapply)
  }
}
trait TabTableComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._
  class TabTable(tag: Tag) extends Table[TabRow](tag/*, _schemaName = Some("main_schema")*/, _tableName = "TAB") {
    def appName = column[String]("APP_NAME")
    def tabName = column[String]("TBL_NAME")
    def schema = column[String]("TBL_SCHEMA")

    def * = (appName, tabName, schema) <> (TabRow.tupled, TabRow.unapply)
  }
}

trait ColTableComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class ColTable(tag: Tag) extends Table[ColRow](tag/*, _schemaName = Some("main_schema")*/, _tableName = "COL") {
    def appName = column[String]("APP_NAME")
    def tabName = column[String]("TBL_NAME")
    def colName = column[String]("COL_NAME")
    def colPk = column[String]("COL_PK")
    def colRequired = column[String]("COL_REQUIRED")
    def colType = column[String]("COL_DATATYPE")
    def colFormat = column[Option[String]]("COL_FORMAT")

    def * = (appName, tabName, colName, colPk, colRequired, colType, colFormat) <> (ColRow.tupled, ColRow.unapply)
  }
}

@Singleton
class ConfigDatabaseService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends AppTableComponent with TabTableComponent with ColTableComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val appTable = lifted.TableQuery[AppTable]
  def getAllApps: Future[Seq[App]] = {
    val query = appTable.result
    db.run(query).map { rows =>
      rows.map { row =>
        val settingsJson =  play.api.libs.json.Json.parse(row.settings)
        val isAllowedRestDownload = (settingsJson \ "isAllowedRestDownload").asOpt[Boolean]
        val isAllowedRestUpload = (settingsJson \ "isAllowedRestUpload").asOpt[Boolean]
        App(
          appName = row.appName,
          appDesc = row.description,
          isAllowedRestDownload = isAllowedRestDownload,
          isAllowedRestUpload = isAllowedRestUpload
        )
      }
    }
  }

  val tabTable = lifted.TableQuery[TabTable]
  def getAllTabs(appName: String): Future[Seq[Tab]] = {
    val query = tabTable.filter(_.appName.toLowerCase === appName.toLowerCase).result
    db.run(query).map { rows =>
      rows.map { row => Tab(
          appName = appName,
          tabName = row.tabName,
          schema = row.schema
        )
      }
    }
  }

  val colTable = lifted.TableQuery[ColTable]

  def getAllCols(appName: String, tabName: String): Future[Seq[Col]] = {
    val query = colTable.filter(x => x.appName.toLowerCase === appName.toLowerCase && x.tabName.toLowerCase === tabName.toLowerCase).result
    db.run(query).map { rows =>
      rows.map { row =>
        Col(appName = appName,
          tabName = row.tabName,
          colName = row.colName,
          isPk = row.colPk.equalsIgnoreCase("Y"),
          isRequired = row.colRequired.equalsIgnoreCase("Y"),
          colType = row.colType,
          colFormat = row.colFormat
        )
      }
    }
  }

  def checkConnection(): Future[Boolean] = {
    db.run(sql"SELECT 1".as[Int].headOption).map(_.isDefined)
  }
}