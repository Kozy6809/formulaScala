package formula

import java.sql.{Connection, DriverManager}

object AnormTest {
  import anorm._
  def main(args: Array[String]): Unit =
  {
    println(Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"))
    implicit val con: Connection = DriverManager.getConnection("jdbc:odbc:formula")
    val result = SQL("Select 1").executeQuery()
    println(result)
  }
}
