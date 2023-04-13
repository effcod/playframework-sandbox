package sandbox.services

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


//https://scala-slick.org/doc/3.3.3/database.html#connection-pool

class DatabaseService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                               (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._


  def checkConnection(): Future[Boolean] = {
    db.run(sql"SELECT 1".as[Int].headOption).map(_.isDefined)
  }

  implicit val getDynamicResult: GetResult[Map[String, Any]] = GetResult { r =>
    val metadata = r.rs.getMetaData
    val columnCount = metadata.getColumnCount
    (1 to columnCount).map { i =>
      val columnName = metadata.getColumnName(i)
      val columnType = metadata.getColumnType(i)

      columnType match {
        case java.sql.Types.TIMESTAMP =>
          columnName -> r.nextTimestamp().getTime
        case java.sql.Types.DATE =>
          columnName -> r.nextDate().getTime
        case _ =>
          columnName -> r.nextObject()
      }
    }.toMap
  }

  def getData(schema: String, tableName: String): Future[List[Map[String, Any]]] = {
    val query = sql"SELECT * FROM #$schema.#$tableName".as[Map[String, Any]]
    db.run(query).map(_.toList)
  }

  def insertData(schema: String, tableName: String, data: List[Map[String, Any]]): Future[Unit] = {
    val columns = data.head.keys.mkString(", ")
    val placeholders = data.head.keys.map(_ => "?").mkString(", ")
    val values = data.map(_.values.toSeq)
    val query = s"INSERT INTO $schema.$tableName ($columns) VALUES ($placeholders)"
    val actions = values.map { value =>
      SimpleDBIO[Unit] { session =>
        val preparedStatement = session.connection.prepareStatement(query)
        value.zipWithIndex.foreach {
          case (v, i) => preparedStatement.setObject(i + 1, v)
        }
        preparedStatement.executeUpdate()
      }
    }
    db.run(DBIO.seq(actions: _*).transactionally)
  }
}
