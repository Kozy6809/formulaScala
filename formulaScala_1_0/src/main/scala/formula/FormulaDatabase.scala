package formula

import java.sql.{Connection, DriverManager}

import scala.util.control.{ControlThrowable, NonFatal}

/**
  * トレイトDatabaseのサブセットを独自実装し、Connectionを保持
  */
object FormulaDatabase {
  println(Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"))

  implicit val connection: Connection = DriverManager.getConnection("jdbc:odbc:formula")


  def withConnection[A](autocommit: Boolean)(block: Connection => A): A = {
    connection.setAutoCommit(autocommit)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }
  def withTransaction[A](block: Connection => A): A = {
    withConnection(autocommit = false) { connection =>
      try {
        val r = block(connection)
        connection.commit()
        r
      } catch {
        case e: ControlThrowable =>
          connection.commit()
          throw e
        case e: Throwable =>
          connection.rollback()
          throw e
      }
    }
  }
}
