package cz.sandbox.services

import cz.sandbox.models.App
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}


case class AppRow(appName: String,
                  description: String,
                  settings: String
                 )

trait AppTableComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class AppTable(tag: Tag) extends Table[AppRow](tag, "app") {
    def appName = column[String]("APP_NAME")
    def description = column[String]("DESCRIPTION")
    def settings = column[String]("SETTINGS")

    def * = (appName, description, settings) <> (AppRow.tupled, AppRow.unapply)
  }
}

@Singleton
class ConfigDatabaseService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AppTableComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val app = lifted.TableQuery[AppTable]
  def getAllApps: Future[Seq[App]] = {
    val query = app.result
    db.run(query).map { rows =>
      rows.map { row =>
        val settingsJson =  play.api.libs.json.Json.parse(row.settings)
        val isRestAllowed = (settingsJson \ "isRestAllowed").asOpt[Boolean]
        App(
          appName = row.appName,
          appDesc = row.description,
          isRestAllowed = isRestAllowed
        )
      }
    }
  }

  def checkConnection(): Future[Boolean] = {
    db.run(sql"SELECT 1".as[Int].headOption).map(_.isDefined)
  }
}