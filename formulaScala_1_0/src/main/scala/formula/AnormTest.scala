package formula

import java.sql.{Connection, DriverManager}

object AnormTest {
  import anorm._
  import anorm.SqlParser._
  import FormulaDatabase.connection

  case class PcodeRow(obsolete: Int, pcode: Int, series: String, name: String)
  private val parser = int("obsolete") ~ int("pcode") ~
    str("series") ~ str("name") map {
    case obsolete ~ pcode ~ series ~ name => PcodeRow(obsolete, pcode, series, name)
  }
  def main(args: Array[String]): Unit =
  {

    val result = SQL("Select series, name, obsolete, pcode from pcode where pcode between 500000 and 500100").
      as(parser.*)
//    val result = SQL("Select pcode, mcode, f.order, date from form1 f where pcode between 500000 and 500100").
//      as(date("date").*)
    println(result)
//    con.setAutoCommit(false)
//    val stmt = con.createStatement()
//    val r = stmt.executeUpdate("insert into pcode values (0, 100000, 'hhh', 'name')")

//    val r = SQL("delete from resolvf").execute()
//    println(r)
//    con.commit()

//    rs = stmt.executeQuery("select mcode from toxmcode where mcode = 315994")
//    rs.next()
//    println(rs.getInt(1))
  }

}
