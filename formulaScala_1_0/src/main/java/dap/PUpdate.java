package dap;

/**
 * 
 */
import java.io.*;
import java.sql.*;
public class PUpdate implements IConsts, IQueryClient {
  private QueryManager qm;
  private int ready = 0; // prepare成功で1、未了で0、失敗で-1
  private String sql = null;
  private int[] INtypes = null;
  private int stmtID = -1;
  private boolean valid = false;
  public PUpdate(QueryManager qm, String sql, int[] INtypes) {
    this.qm = qm;
    this.sql = sql;
    this.INtypes = INtypes;
  }
  /**
   * @return int
   */
  public int getReady() {
    return ready;
  }
  /**
   */
  public void prepare() {
    if (ready > 0) return;
    qm.prepare(this, 0, sql, INtypes);
  }
  /**
   * prepareに成功すればready=1、失敗したら-1のままになる。
   */
  public void prepareAndWait() throws SQLException {
    ready = -1;
    stmtID = qm.prepareAndWait(sql, INtypes);
    ready = 1;
  }
  /**
   * queryCallBack メソッド・コメント
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      ready = -1;
    } else {
      ready = 1;
      stmtID = ((Integer)o).intValue();
    }
  }
  public boolean update(IQueryClient client, int mode, Object[] INparms, int transactionID) {
    if (stmtID < 0) return false;
    valid = false;
    qm.pUpdate(client, mode, stmtID, INparms, transactionID);
    return true;
  }
  /**
   * @return int
   * @param params java.lang.Object[]
   * @param transactionID int
   */
  public int updateAndWait(Object[] params, int transactionID) throws SQLException {
    if (stmtID < 0) return -1;
    int retrieved = qm.pUpdateAndWait(stmtID, params, transactionID);
    valid = true;
    return retrieved;
  }
}
