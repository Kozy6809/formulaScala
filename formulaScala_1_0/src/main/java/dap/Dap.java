package dap;

/**
Dap - データベース検索のプロトコルを解釈し、クエリーを実行して結果を返す
入力 DataInputStream
出力 DataOutputStream

プロトコルの詳細は"dap"を参照のこと
99-6-2
run()メソッドの中でSystem.gc()を実行するようにした
*/
import java.io.*;
import java.net.*;
import java.sql.*;
import java.math.*;
import java.util.Vector;

class Dap implements Runnable, dap.IConsts {
  private Socket s = null;
  private ObjectInputStream dis = null;
  private ObjectOutputStream dos = null;
  private Connection con = null;
  private Statement stmt = null;
  private Vector ppStmts = null; // prepared statements
  private ResultSet rs = null;
  private ResultSetMetaData rsmd = null;
  private int[] colTypes = null; //column types of result set
  private String charConversion;

  Dap(Socket s) throws IOException {
    this.s = s;
    dis = new ObjectInputStream(s.getInputStream());
    dos = new ObjectOutputStream(s.getOutputStream());
    ppStmts = new Vector();
  }
  void dapAutoCommit() throws IOException {
    try {
      boolean mode = dis.readBoolean();
      if (con != null) {
	con.setAutoCommit(mode);
	dos.writeInt(OK);
      } else dos.writeInt(INVALID_CONNECTION);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  /**
   * トランザクションの開始。JdbcOdbcDriverのバグに対処するため、トランザクションの開始時に
   * ガベージコレクションを実行する
   */
  void dapBeginTransaction() throws IOException {
    SyncGc.begin();
    dos.writeInt(OK);
    dos.flush();
  }
  void dapCloseCON() throws IOException {
    try {
      if (con != null) {
	con.close();
	con = null;
	dos.writeInt(OK);
      } else dos.writeInt(INVALID_CONNECTION);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  void dapCloseRS() throws IOException {
    try {
      if (rs != null) {
	rs.close();
	rs = null;
	dos.writeInt(OK);
      } else dos.writeInt(INVALID_RESULTSET);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  void dapCloseSession() throws IOException {
    if (con != null) dapCloseCON();
  }
  void dapCloseSTMT() throws IOException {
    boolean success = false;
    try {
      int ID = dis.readInt();
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
      if (success) dos.writeInt(OK);
      else dos.writeInt(INVALID_STATEMENT);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  /**
   * トランザクションのコミット。トランザクションの終了をSyncGcに通知すること
   */
  void dapCommit() throws IOException {
    try {
      if (con != null) {
	SyncGc.end();
	con.commit();
	dos.writeInt(OK);
      } else dos.writeInt(INVALID_CONNECTION);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  void dapConnect() throws IOException {
    try {
      Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
    } catch (ClassNotFoundException e) {
      System.out.println(e.getMessage());
    }
    try {
      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
      charConversion = "SJIS";
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String DB = dis.readUTF();
    String s1 = dis.readUTF();
    String s2 = dis.readUTF();
    if (con != null) {
      dapCloseCON();
      con = null;
    }
    try {
      con = DriverManager.getConnection(DB, s1, s2);
      stmt = con.createStatement();
      dos.writeInt(OK);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  void dapGetRow() throws IOException {
    if (rs == null || rsmd == null) {
      dos.writeInt(INVALID_RESULTSET);
      dos.flush();
      return;
    }
    dos.writeInt(OK);
    /* 一行分のデータを取り出している途中でSQLExceptionが発生した場合は、
       　SQLERRORを送信し、リザルトセットをクローズする
    */
    try {
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
	dos.writeInt(OK);
	dos.writeObject(o);
      }
      dos.writeInt(OK);
      dos.writeObject(null);
    } catch (SQLException e) {
      dos.writeInt(SQLERROR);
    } finally {
      dos.flush();
      try {
	rs.close();
      } catch (SQLException e) {}	
    }
  }
  void dapPQuery() throws IOException {
    try {
      int index = dis.readInt();
      Object[] parms = (Object[])dis.readObject();
      PpStmt p = (PpStmt)ppStmts.elementAt(index);
      if (p == null) {
	invalidID();
	return;
      }
      if (setINTypes(p, parms)) {
	rs = p.getStmt().executeQuery();
	rsmd = rs.getMetaData();
	colTypes = new int[rsmd.getColumnCount()];
	for (int i=0; i < colTypes.length; i++) colTypes[i] = rsmd.getColumnType(i+1);
	dos.writeInt(OK);
      } else dos.writeInt(PARMS_MISMATCH);	
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    } catch (ArrayIndexOutOfBoundsException e) {
      invalidID();
    } catch (ClassNotFoundException e) {}
  }
  void dapPrepare() throws IOException {
    try {
      String stmt = dis.readUTF();
      int[] types = (int[])dis.readObject();
      if (con == null) {
	dos.writeInt(INVALID_CONNECTION);
	dos.flush();
	return;
      }
      PpStmt p = new PpStmt();
      p.setTypes(types);
      ppStmts.addElement(p);
      p.setStmt(con.prepareStatement(stmt));
      dos.writeInt(OK);
      dos.writeInt(ppStmts.size()-1);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    } catch (ClassNotFoundException e) {}
  }
  void dapPUpdate() throws IOException {
    try {
      int index = dis.readInt();
      Object[] parms = (Object[])dis.readObject();
      PpStmt p = (PpStmt)ppStmts.elementAt(index);
      if (p == null) {
	invalidID();
	return;
      }
      if (setINTypes(p, parms)) {
	int rc = p.getStmt().executeUpdate();
	dos.writeInt(OK);
	dos.writeInt(rc);
      } else dos.writeInt(PARMS_MISMATCH);	
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    } catch (ArrayIndexOutOfBoundsException e) {
      invalidID();
    } catch (ClassNotFoundException e) {}
  }
  void dapQuery() throws IOException {
    try {
      String sql = dis.readUTF();
      if (stmt == null) dos.writeInt(INVALID_STATEMENT);
      else {
	rs = stmt.executeQuery(sql);
	rsmd = rs.getMetaData();
	colTypes = new int[rsmd.getColumnCount()];
	for (int i=0; i < colTypes.length; i++) colTypes[i] = rsmd.getColumnType(i+1);
	dos.writeInt(OK);
      }
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  /**
   * ロールバック。この時もトランザクションの終了をSyncGcに通知する
   */
  void dapRollback() throws IOException {
    try {
      if (con != null) {
	SyncGc.end();
	con.rollback();
	dos.writeInt(OK);
      } else dos.writeInt(INVALID_CONNECTION);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  /**
   * このメソッドは VisualAge で作成されました。
   */
  private void dapSetCharConversion() throws IOException {
    charConversion = dis.readUTF();
    dos.writeInt(OK);
    dos.flush();
    System.out.println("char conversion mode is set to " + charConversion);
  }
  void dapSetColType() throws IOException {
    try {
      colTypes = (int[])dis.readObject();
      dos.writeInt(OK);
      dos.flush();
    } catch (ClassNotFoundException e) {}
  }
  void dapUpdate() throws IOException {
    try {
      String sql = dis.readUTF();
      if (stmt != null) {
	int rc = stmt.executeUpdate(sql);
	dos.writeInt(OK);
	dos.writeInt(rc);
      } else dos.writeInt(INVALID_STATEMENT);
      dos.flush();
    } catch (SQLException e) {
      sendSQLError(e);
    }
  }
  // 不正なprepared statement IDを受け取った時の返答
  void invalidID () throws IOException {
    dos.writeInt(INVALID_ID);
    dos.flush();
  }
  /** command dispatch
   * 1999-6-2
   * JDBC-ODBCブリッジを使用する際、トランザクションの途中でガベージコレクションが
   * 発生すると、ODBCバッファのポインタが不整合になり、データが化ける等の障害が発生する
   * バグがあることが判明。これを回避するために、JDBC呼び出しの前に明示的な
   * ガベージコレクションを実行するようにした
   */
  public void run() {
    try {
    QUIT:
      for (;;) {
	int in = dis.readInt();
	switch (in) {
	case URL: dapConnect(); break;
	case QUERY: dapQuery(); break;
	case GETROW: dapGetRow(); break;
	case UPDATE: dapUpdate(); break;
	case PREPARE: dapPrepare(); break;
	case SETCOLTYPE: dapSetColType(); break;
	case PQUERY: dapPQuery(); break;
	case PUPDATE: dapPUpdate(); break;
	case CLOSERS: dapCloseRS(); break;
	case CLOSESTMT: dapCloseSTMT(); break;
	case CLOSECON: dapCloseCON(); break;
	case AUTOCOMMIT: dapAutoCommit(); break;
	case COMMIT: dapCommit(); break;
	case ROLLBACK: dapRollback(); break;
	case SETCHARCONVERSION: dapSetCharConversion(); break;
	case GARBAGECOLLECTION: dapBeginTransaction(); break;
	case BYE:
	  dapCloseSession();
	  System.out.println(this + ": session finished");
	  break QUIT;
	default:
	  dos.writeInt(UNKNOWN_COMMAND);
	  dos.writeInt(in);
	  dos.flush();
	}
      }
    } catch (Exception e) {
      System.err.println(this + ": network I/O error!");
      //		e.printStackTrace();
      try {
	if (con != null) {
	  con.rollback();
	  con.close();
	}	
      } catch (Exception f) {}
    }
    try {	
      dos.close();
      dis.close();
      s.close();
      System.out.println(this + ": connection closed");
    } catch (Exception e) {}
  }
  void sendSQLError(SQLException e) throws IOException {
    //	e.printStackTrace();
    dos.writeInt(SQLERROR);
    dos.writeObject(e);
    System.err.println(new String(e.getMessage().getBytes("8859_1"), "SJIS"));
  }
  boolean setINTypes(PpStmt p, Object[] o) throws IOException, SQLException {
    PreparedStatement stmt = p.getStmt();
    try {
      if (o.length != p.getTypeNum()) return false;
      for (int i=0; i < p.getTypeNum(); i++) {
	int type = p.getType(i);
	switch (type) {
	case BARRY:
	  stmt.setBytes(i+1, (byte[])o[i]); break;
	case STRING:
	  String s = (String)o[i];
	  if (s == null) s = "";
	  try {
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
