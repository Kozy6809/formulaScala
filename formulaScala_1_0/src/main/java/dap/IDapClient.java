package dap;

/**
 * この インターフェース は SmartGuide によって生成されました。
 * 
 */
import java.io.*;
import java.sql.*;
import java.util.Vector;
public interface IDapClient {
  void closePort();
  int dapAutoCommit(boolean mode) throws SQLException, IOException;
  /**
   * トランザクションの開始時に呼び出すメソッド。JdbcOdbcDriverのバグに対処するため、
   * ガベージコレクションを実行する
   */
  int dapBeginTransaction() throws SQLException, IOException;
  int dapCloseCON() throws SQLException, IOException;
  int dapCloseRS() throws SQLException, IOException;
  int dapCloseSession() throws SQLException, IOException;
  int dapCloseSTMT(int ID) throws SQLException, IOException;
  int dapCommit() throws SQLException, IOException;
  int dapConnect(String DB, String s1, String s2) throws SQLException, IOException;
  Vector dapGetRow() throws SQLException, IOException;
  int dapPQuery(int id, Object[] INparm) throws SQLException, IOException;
  int dapPrepare(String stmt, int[] INtypes) throws SQLException, IOException;
  int dapPUpdate(int id, Object[] INparm) throws SQLException, IOException;
  int dapQuery(String s) throws SQLException, IOException;
  int dapRollback() throws SQLException, IOException;
  /**
   * JDBCドライバが使用する文字コードを設定する。パラメータはString.getBytes()の
   * エンコード指定文字列で与え、例えばシフトJISなら"SJIS"とする。
   * UNICODEの場合はパラメータをnullにすると
   * @param s java.lang.String
   */
  int dapSetCharConversion(String s) throws IOException, SQLException;
  int dapSetColType(int[] types) throws SQLException, IOException;
  int dapUpdate(String s) throws SQLException, IOException;
  boolean openPort(String host);
}
