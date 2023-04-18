package cz.sandbox.services

trait QueryService {
  def getData (schema: String, tableName: String): List[Map[String, Any]]
  def insertData (schema: String, tableName: String, data: List[Map[String, Any]]): Unit
}