package dap;

/**
 * 1999-6-2
 * JDBC-ODBCブリッジを使用する際、トランザクションの途中でガベージコレクションが
 * 発生すると、ODBCバッファのポインタが不整合になり、データが化ける等の障害が発生する
 * バグがあることが判明。これを回避するために、JDBC呼び出しの前に明示的な
 * ガベージコレクションを実行するようにした。パフォーマンスへの影響を考慮し、実行は
 * dapBeginTransaction()のみになっている。
 */
import java.io.*;
import java.sql.*;
import java.util.*;

public class DapDirect implements IConsts, IDapClient {
  private Connection con = null;
  private Statement stmt = null;
  private Vector ppStmts = null; // prepared statements
  private ResultSet rs = null;
  private ResultSetMetaData rsmd = null;
  private int[] colTypes = null; //column types of result set
  private String charConversion = null;

  public DapDirect() {
    this((String)null);
  }
  public DapDirect(String convMode) {
      //    this.charConversion = convMode;
    ppStmts = new Vector();
  }
  /**
   * closePort メソッド・コメント
   */
  public void closePort() {
  }
  /**
   * dapAutoCommit メソッド・コメント
   */
  public int dapAutoCommit(boolean mode) throws java.sql.SQLException, java.io.IOException {
      // this feature is disabled
      //    if (con != null) {
      //try {
      // con.setAutoCommit(mode);
      //} catch (SQLException e) {
      //    System.out.println(e.getMessage());
      //    throw e;
      //}
      //}	
    return OK;
  }
  /**
   * トランザクションの開始。JdbcOdbcDriverのバグに対処するため、トランザクションの開始時に
   * ガベージコレクションを実行する
   */
  public int dapBeginTransaction() {
    SyncGc.begin();
    return OK;
  }
  /**
   * dapCloseCON メソッド・コメント
   */
  public int dapCloseCON() throws java.sql.SQLException, java.io.IOException {
    if (con != null) {
      con.close();
      con = null;
    }
    return OK;	
  }
  /**
   * dapCloseRS メソッド・コメント
   */
  public int dapCloseRS() throws java.sql.SQLException, java.io.IOException {
    if (rs != null) {
      rs.close();
      rs = null;
    }
    return OK;	
  }
  /**
   * dapCloseSession メソッド・コメント
   */
  public int dapCloseSession() throws java.sql.SQLException, java.io.IOException {
    if (con != null) dapCloseCON();
    return OK;
  }
  /**
   * dapCloseSTMT メソッド・コメント
   */
  public int dapCloseSTMT(int ID) throws java.sql.SQLException, java.io.IOException {
    boolean success = false;
    if (ID < 0 && stmt != null) {
      stmt.close();
      success = true;
    } else if (ID < ppStmts.size()) {
      PpStmt p = (PpStmt)ppStmts.elementAt(ID);
      if (p != null) {
	p.getStmt().close();
	ppStmts.setElementAt(null, ID);
	success = true;
      }
    }
    return success ? OK : INVALID_STATEMENT;
  }
  /**
   * トランザクションのコミット。トランザクションの終了をSyncGcに通知すること
   */
  public int dapCommit() throws java.sql.SQLException, java.io.IOException {
    if (con != null) {
      SyncGc.end();
      con.commit();
      return OK;
    } else return INVALID_CONNECTION;
  }
  /**
   * dapConnect メソッド・コメント
   */
  public int dapConnect(String DB, String s1, String s2) throws java.sql.SQLException, java.io.IOException {
    if (con != null) {
      dapCloseCON();
      con = null;
    }
    con = DriverManager.getConnection(DB, s1, s2);
    con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    con.setAutoCommit(false);
    stmt = con.createStatement();
    return OK;
  }
  /**
   * dapGetRow メソッド・コメント
   */
  public java.util.Vector dapGetRow() throws SQLException, IOException {
    if (rs == null || rsmd == null) return null;
    Vector v = new Vector();
    while (rs.next()) {
      Object[] o = new Object[colTypes.length+1];
      for (int i=0; i < colTypes.length; i++) {
	switch (colTypes[i]) {
	case Types.BINARY:
	case Types.VARBINARY:
	case Types.LONGVARBINARY:
	  o[i] = rs.getBytes(i+1); break;
	case Types.CHAR:
	case Types.VARCHAR:
	case Types.LONGVARCHAR:
	  String s = rs.getString(i+1);
	  if (s == null) o[i] = null;
	  else if (charConversion == null) o[i] = s;
	  else o[i] = new String(s.getBytes("8859_1"), charConversion);
	  break;
	case Types.TINYINT:
	  o[i] = new Integer(rs.getByte(i+1)); break;
	case Types.SMALLINT:
	  o[i] = new Integer(rs.getShort(i+1)); break;
	case Types.INTEGER:
	  o[i] = new Integer(rs.getInt(i+1)); break;
	case Types.BIGINT:
	  o[i] = new Long(rs.getLong(i+1)); break;
	case Types.REAL:
	  // o[i] = new Float(rs.getFloat(i+1)); break;
	  o[i] = new Double((double)rs.getFloat(i+1)); break;
	case Types.FLOAT:
	case Types.DOUBLE:
	  o[i] = new Double(rs.getDouble(i+1)); break;
	case Types.DECIMAL:
	case Types.NUMERIC:
	  o[i] = rs.getBigDecimal(i+1, rsmd.getScale(i+1)); break;
	case Types.BIT:
	  o[i] = new Boolean(rs.getBoolean(i+1)); break;
	case Types.DATE:
	  long t = rs.getDate(i+1).getTime();
	  o[i] = new java.util.Date(t); break;
	case Types.TIME:
	  o[i] = new java.util.Date(rs.getTime(i+1).getTime()); break;
	case Types.TIMESTAMP:
	  /*
	    t = rs.getDate(i+1).getTime();
	    o[i] = new java.util.Date(t); break;
	  */
	  java.sql.Timestamp ts = rs.getTimestamp(i+1);
	  long tt = ts.getTime()+ts.getNanos()/1000000;
	  java.util.Date d = new java.util.Date(tt);
	  o[i] = d;
	  // o[i] = new java.util.Date(rs.getTimestamp(i).getTime()); break;
	  break;

	default:
	  o[i] = rs.getObject(i+1); break;
	}
      }
      v.addElement(o);
    }
    rs.close();
    return v;
  }
  /**
   * dapPQuery メソッド・コメント
   */
  public int dapPQuery(int id, java.lang.Object[] INparm) throws java.sql.SQLException, java.io.IOException {
    try {
      int index = id;
      Object[] parms = INparm;
      PpStmt p = (PpStmt)ppStmts.elementAt(index);
      if (p == null) return INVALID_ID;
      if (setINTypes(p, parms)) {
	rs = p.getStmt().executeQuery();
	rsmd = rs.getMetaData();
	colTypes = new int[rsmd.getColumnCount()];
	for (int i=0; i < colTypes.length; i++) colTypes[i] = rsmd.getColumnType(i+1);
	return OK;
      } else return PARMS_MISMATCH;
    } catch (ArrayIndexOutOfBoundsException e) {
      return INVALID_ID;
    }
  }
  /**
   * dapPrepare メソッド・コメント
   */
  public int dapPrepare(String stmt, int[] INtypes) throws java.sql.SQLException, java.io.IOException {
    int[] types = INtypes;
    if (con == null) return INVALID_CONNECTION;
    PpStmt p = new PpStmt();
    p.setTypes(types);
    ppStmts.addElement(p);
    p.setStmt(con.prepareStatement(stmt));
    return ppStmts.size()-1;
  }
  /**
   * dapPUpdate メソッド・コメント
   */
  public int dapPUpdate(int id, java.lang.Object[] INparm) throws java.sql.SQLException, java.io.IOException {
    try {
      int index = id;
      Object[] parms = INparm;
      PpStmt p = (PpStmt)ppStmts.elementAt(index);
      if (p == null) return INVALID_ID;
      if (setINTypes(p, parms)) {
	int rc = p.getStmt().executeUpdate();
	return rc;
      } else return PARMS_MISMATCH;
    } catch (ArrayIndexOutOfBoundsException e) {
      return INVALID_ID;
    }
  }
  /**
   * dapQuery メソッド・コメント
   */
  public int dapQuery(String sql) throws java.sql.SQLException, java.io.IOException {
    if (stmt == null) return INVALID_STATEMENT;
    rs = stmt.executeQuery(sql);
    rsmd = rs.getMetaData();
    colTypes = new int[rsmd.getColumnCount()];
    for (int i=0; i < colTypes.length; i++) colTypes[i] = rsmd.getColumnType(i+1);
    return OK;
  }
  /**
   * ロールバック。この時もトランザクションの終了をSyncGcに通知する
   */
  public int dapRollback() throws java.sql.SQLException, java.io.IOException {
    if (con != null) {
      SyncGc.end();
      con.rollback();
      return OK;
    } else return INVALID_CONNECTION;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @param s java.lang.String
   */
  public int dapSetCharConversion(String s) {
      //    charConversion = s;
      //    if (s.length() == 0) charConversion = null;
    return OK;
  }
  /**
   * dapSetColType メソッド・コメント
   */
  public int dapSetColType(int[] types) throws java.sql.SQLException, java.io.IOException {
    colTypes = types;
    return OK;
  }
  /**
   * dapUpdate メソッド・コメント
   */
  public int dapUpdate(String sql) throws java.sql.SQLException, java.io.IOException {
    if (stmt != null) {
      int rc = stmt.executeUpdate(sql);
      return rc;
    } else return INVALID_STATEMENT;
  }
  /**
   * openPort メソッド・コメント
   */
  public boolean openPort(String host) {
    return true;
  }
  boolean setINTypes(PpStmt p, Object[] o) throws SQLException {
    PreparedStatement stmt = p.getStmt();
    try {
      if (o.length != p.getTypeNum()) return false;
      for (int i=0; i < p.getTypeNum(); i++) {
	int type = p.getType(i);
	switch (type) {
	case BARRY:
	  stmt.setBytes(i+1, (byte[])o[i]); break;
	case STRING:
	  try {
	    String s = (String)o[i];
	    if (s == null) s = "";
	    if (charConversion != null)
	      s = new String(s.getBytes(charConversion), "8859_1");
	    stmt.setString(i+1, s);
	  } catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	  }
	  break;
	case SHORT:
	  stmt.setShort(i+1, ((Integer)o[i]).shortValue()); break;
	case INT:
	  stmt.setInt(i+1, ((Integer)o[i]).intValue()); break;
	case LONG:
	  stmt.setLong(i+1, ((Long)o[i]).longValue()); break;
	case FLOAT:
	  // stmt.setDouble(i+1, ((Float)o[i]).doubleValue()); break;
	case DOUBLE:
	  stmt.setDouble(i+1, ((Double)o[i]).doubleValue()); break;
	case DATE:
	  long t = ((java.util.Date)o[i]).getTime();
	  java.sql.Date d = new java.sql.Date(t);
	  stmt.setDate(i+1, d); break;
	  /*
	    case TIME:
	    stmt.setTime(i+1, (java.sql.Time)o[i]); break;
	  */
	case TIMESTAMP:
	  t = ((java.util.Date)o[i]).getTime();
	  java.sql.Timestamp ts = new java.sql.Timestamp(t);
	  stmt.setTimestamp(i+1, ts); break;
	default:
	}
      }
    } catch (ClassCastException e) {
      return false;
    }	
    return true;
  }
}
