package dap;

/**
 * 
 */
import java.util.*;
import java.io.*;
import java.sql.*;
public class Update {
  private QueryManager qm;
  private String sql = null;
  private boolean valid = false;
  public Update(QueryManager qm, String sql) {
    this.qm = qm;
    this.sql = sql;
  }
  public void update(IQueryClient client, int mode, int transactionID) {
    valid = false;
    qm.update(client, mode, sql, transactionID);
  }
  /**
   * @return int
   * @param transactionID int
   */
  public int updateAndWait(int transactionID) throws SQLException {
    int retrieved = qm.updateAndWait(sql, transactionID);
    valid = true;
    return retrieved;
  }
}
