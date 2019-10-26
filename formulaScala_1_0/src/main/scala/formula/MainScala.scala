package formula
import javax.persistence._
import java.sql.DriverManager

object MainScala {

  def main(args: Array[String]): Unit = {
    println(Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"))
    val con = DriverManager.getConnection("jdbc:odbc:formula")
    val stmt = con.createStatement()
    val r = stmt.executeQuery("select * from form1 where pcode = 500013")
    println(r)
    new MainC()
  }

}

