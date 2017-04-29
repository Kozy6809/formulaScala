package dap;

/**
 * 
 */
import java.util.*;
import java.io.*;
import java.sql.*;

public class PQuery implements dap.IQueryClient, dap.IConsts {
  private QueryManager qm;
  private int ready = 0; // prepare成功で1、未了で0、失敗で-1
  private String sql = null;
  private int[] INtypes = null;
  private int stmtID = -1;
  private boolean valid = false;
  /**
   * このメソッドは SmartGuide によって作成されました。
   * @param sql java.lang.String
   * @param INtypes int[]
   */
  public PQuery(QueryManager qm, String sql, int[] INtypes) {
    this.qm = qm;
    this.sql = sql;
    this.INtypes = INtypes;
  }
  /**
   * @return java.lang.Object
   */
  public Object clone() {
    PQuery t = new PQuery(qm, sql, INtypes);
    t.ready = ready;
    t.stmtID = stmtID;
    t.valid = false;
    return t;
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
   * このメソッドは SmartGuide によって作成されました。
   */
  public boolean query(IQueryClient client, int mode, Object[] INparms) {
    if (stmtID < 0) return false;
    valid = false;
    qm.pQuery(client, mode, stmtID, INparms);
    return true;
  }
  /**
   * @return Vector
   * @param params java.lang.Object[]
   */
  public Vector queryAndWait(Object[] params) throws SQLException {
    if (stmtID < 0) return null;
    Vector result = qm.pQueryAndWait(stmtID, params);
    valid = true;
    return result;
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
}
