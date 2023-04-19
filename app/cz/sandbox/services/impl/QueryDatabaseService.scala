package cz.sandbox.services.impl

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


//https://scala-slick.org/doc/3.3.3/database.html#connection-pool
class QueryDatabaseService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                   (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._


  def getData(schema: String, tableName: String): Future[List[Map[String, Any]]] = {
    implicit val getMapResult: GetResult[Map[String, Any]] = GetResult[Map[String, Any]] { r =>
      val rs = r.rs // <- r is PositionedResult which wraps ResultSet
      val md = rs.getMetaData
      val res = (1 to r.numColumns).map { i =>
        md.getColumnName(i) -> rs.getObject(i)
      }.toMap
      res
    }

    val query = sql"SELECT * FROM #$schema.#$tableName".as[Map[String, Any]]
    db.run(query).map(_.toList)
  }

  def insertData(schema: String, tableName: String, data: List[Map[String, Any]]): Future[Unit] = {
    val columnsList = data.head.keys.toList
    val columns = columnsList.mkString(", ")
    val placeholders = columnsList.map(_ => "?").mkString(", ")
    val values = data.map(_.values.toSeq)
    val query = s"INSERT INTO $schema.$tableName ($columns) VALUES ($placeholders)"
    println(query)
    val actions = values.map { value =>
      SimpleDBIO[Unit] { session =>
        val preparedStatement = session.connection.prepareStatement(query)
        value.zipWithIndex.foreach {
          case (Some(v), i) => preparedStatement.setObject(i + 1, v)
          case (None, i) => preparedStatement.setNull(i + 1, java.sql.Types.NULL) // needs manually convert to null -> because it uses plain sql
          case (v, i) => preparedStatement.setObject(i + 1, v)
        }
        preparedStatement.executeUpdate()
      }
    }
    db.run(DBIO.seq(actions: _*).transactionally)
  }
}
