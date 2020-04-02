package formula

import java.sql.{Connection, DriverManager}

object AnormTest {
  import anorm._
  import anorm.SqlParser._

  case class PcodeRow(obsolete: Int, pcode: Int, series: String, name: String)
  private val parser = int("obsolete") ~ int("pcode") ~
    str("series") ~ str("name") map {
    case obsolete ~ pcode ~ series ~ name => PcodeRow(obsolete, pcode, series, name)
  }
  def main(args: Array[String]): Unit =
  {
    println(Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"))
    implicit val con: Connection = DriverManager.getConnection("jdbc:odbc:formula")
    val result = SQL("Select pcode, mcode, f.order, date from form1 f where pcode between 500000 and 500100").
      as((date("date")).*)
    println(result)
    val r = SQL("insert into toxmcode values(0, \"HHH\", 0.0)").executeInsert()
    println(r)
  }

}
