package cz.sandbox.errors

final case class DatabaseException(private val message: String = "", private val cause: Throwable = None.orNull) extends Exception(message, cause)
final case class InvalidRequestException(private val message: String = "", private val cause: Throwable = None.orNull) extends Exception(message, cause)
final case class UnimplementedException(private val message: String = "", private val cause: Throwable = None.orNull) extends Exception(message, cause)