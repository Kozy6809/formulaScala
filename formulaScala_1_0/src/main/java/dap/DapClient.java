package dap;

/**
DapClient - DBアクセスのクライアント
*/
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Vector;

public class DapClient implements IConsts, IDapClient {
  private Socket socket = null;
  private ObjectOutputStream dos = null;
  private ObjectInputStream dis = null;
  private int[] INtypes = null;
  private int[] colTypes = null;

  boolean checkObjectType(Object[] INparm) {
    for (int i=0; i < INparm.length; i++) {
      switch (INtypes[i]) {
      case BARRY:
	if (!(INparm[i] instanceof byte[])) return false;
	break;
      case STRING:
	if (!(INparm[i] instanceof String)) return false;
	break;
      case SHORT:
	if (!(INparm[i] instanceof Short)) return false;
	break;
      case INT:
	if (!(INparm[i] instanceof Integer)) return false;
	break;
      case LONG:
	if (!(INparm[i] instanceof Long)) return false;
	break;
      case FLOAT:
	if (!(INparm[i] instanceof Double)) return false;
	break;
      case DOUBLE:
	if (!(INparm[i] instanceof Double)) return false;
	break;
      case DATE:
	if (!(INparm[i] instanceof java.util.Date)) return false;
	break;
	/*
	  case TIME:
	  if (!(INparm[i] instanceof java.sql.Time)) return false;
	  break;
	  case TIMESTAMP:
	  if (!(INparm[i] instanceof java.sql.Timestamp)) return false;
	  break;
	*/
      default:			
      }
    }
    return true;
  }
  int checkOK() throws SQLException, IOException {
    int ret = dis.readInt();
    if (ret == OK) return ret;
    else if (ret == SQLERROR) {
      SQLException e = null;
      try {
	e = (SQLException)dis.readObject();
      } catch (ClassNotFoundException f) {}	
      System.err.println("SQL error");
      System.err.println(e.getErrorCode());
      System.err.println(e.getSQLState());
      System.err.println(new String(e.toString().getBytes("8859_1"), "SJIS"));
      throw e;
    } else if (ret == UNKNOWN_COMMAND) {
      System.err.println("unknown command");
      System.err.println("code =" + dis.readInt());
    }
    return ret;
  }
  public void closePort() {
    try {
      dos.close();
      dis.close();
      socket.close();
    } catch (IOException e) {}
  }
  public int dapAutoCommit(boolean mode) throws SQLException, IOException {
    dos.writeInt(AUTOCOMMIT);
    dos.writeBoolean(mode);
    dos.flush();
    return checkOK();
  }
  public int dapBeginTransaction() throws SQLException, IOException {
    dos.writeInt(GARBAGECOLLECTION);
    dos.flush();
    return checkOK();
  }
  public int dapCloseCON() throws SQLException, IOException {
    dos.writeInt(CLOSECON);
    dos.flush();
    return checkOK();
  }
  public int dapCloseRS() throws SQLException, IOException {
    dos.writeInt(CLOSERS);
    dos.flush();
    return checkOK();
  }
  public int dapCloseSession() throws SQLException, IOException {
    dos.writeInt(BYE);
    dos.flush();
    return checkOK();
  }
  /**
   * ステートメントをクローズする。入力はステートメントID
   * preparedステートメント以外の時はIDに負数をセットする
   */
  public int dapCloseSTMT(int ID) throws SQLException, IOException {
    dos.writeInt(CLOSESTMT);
    dos.writeInt(ID);
    dos.flush();
    return checkOK();
  }
  public int dapCommit() throws SQLException, IOException {
    dos.writeInt(COMMIT);
    dos.flush();
    return checkOK();
  }
  public int dapConnect(String DB, String s1, String s2) throws SQLException, IOException {
    dos.writeInt(URL);
    dos.writeUTF(DB);
    dos.writeUTF(s1);
    dos.writeUTF(s2);
    dos.flush();
    return checkOK();
  }
  public Vector dapGetRow() throws SQLException, IOException {
    try {
      dos.writeInt(GETROW);
      dos.flush();
      if (checkOK() != OK) return null;
      Vector v = new Vector();
      for (;;) {
	if (dis.readInt() != OK) break;
	Object[] o;
	if ((o = (Object[])dis.readObject()) == null) break;
	v.addElement(o);
      }
      return v;	
    } catch (ClassNotFoundException e) {}
    return null;
  }
  public int dapPQuery(int id, Object[] INparm) throws SQLException, IOException {
    //	if (!checkObjectType(INparm)) return PARMS_MISMATCH;
    dos.writeInt(PQUERY);
    dos.writeInt(id);
    dos.writeObject(INparm);
    dos.flush();
    return checkOK();
  }
  // 戻り値はprepared statement ID。エラーの時は負数
  public int dapPrepare(String stmt, int[] INtypes) throws SQLException, IOException {
    this.INtypes = INtypes;
    dos.writeInt(PREPARE);
    dos.writeUTF(stmt);
    dos.writeObject(INtypes);
    dos.flush();
    int ret;
    if ((ret = checkOK()) != OK) return ret;
    return dis.readInt();
  }
  public int dapPUpdate(int id, Object[] INparm) throws SQLException, IOException {
    //	if (!checkObjectType(INparm)) return PARMS_MISMATCH;
    dos.writeInt(PUPDATE);
    dos.writeInt(id);
    dos.writeObject(INparm);
    dos.flush();
    int ret;
    if ((ret = checkOK()) != OK) return ret;
    return dis.readInt();
  }
  public int dapQuery(String s) throws SQLException, IOException {
    dos.writeInt(QUERY);
    dos.writeUTF(s);
    dos.flush();
    return checkOK();
  }
  public int dapRollback() throws SQLException, IOException {
    dos.writeInt(ROLLBACK);
    dos.flush();
    return checkOK();
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @param s java.lang.String
   */
  public int dapSetCharConversion(String s) throws IOException, SQLException {
    dos.writeInt(SETCHARCONVERSION);
    dos.writeUTF(s);
    dos.flush();
    return checkOK();
  }
  public int dapSetColType(int[] types) throws SQLException, IOException {
    colTypes = types;
    dos.writeInt(SETCOLTYPE);
    dos.writeObject(colTypes);
    dos.flush();
    return checkOK();
  }
  // 戻り値は更新された行数。エラーがあった場合は負数にする
  public int dapUpdate(String s) throws SQLException, IOException {
    dos.writeInt(UPDATE);
    dos.writeUTF(s);
    dos.flush();
    int ret;
    if ((ret = checkOK()) != OK) return ret;
    return dis.readInt();
  }
  public boolean openPort(String host) {
    if (socket != null) return true;
    try {
      socket = new Socket(host, 4444);
      dos = new ObjectOutputStream(socket.getOutputStream());
      dis = new ObjectInputStream(socket.getInputStream());
      return true;
    } catch (UnknownHostException e) {
      System.err.println("DapClient: Don't know about host");
      return false;
    } catch (IOException e) {
      System.err.println("DapClient: Couldn't open port");
      return false;
    }
  }
}
