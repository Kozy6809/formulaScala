package dap;

/**
 * この クラス は SmartGuide によって生成されました。
 * 
 */
import java.util.*;
import java.io.*;
import java.sql.*;
public class Query {
  private QueryManager qm = null;
  private String sql = null;
  private boolean valid = false;
  public Query(QueryManager qm, String sql) {
    this.qm = qm;
    this.sql = sql;
  }
  public void query(IQueryClient client, int mode) {
    valid = false;
    qm.query(client, mode, sql);
  }
  /**
   * @return Vector
   */
  public Vector queryAndWait() throws SQLException {
    Vector result = qm.queryAndWait(sql);
    valid = true;
    return result;
  }
}
