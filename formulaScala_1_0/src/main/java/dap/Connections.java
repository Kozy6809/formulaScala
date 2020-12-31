package dap;

import java.sql.Connection;
import java.util.*;
import dap.*;
/**
 * 複数の接続を生成・管理するクラス。
 * 一つの接続は((Dap, DapClient,) | DapDirect,) & QueryManagerの組で構成される。
 * ダイレクト接続を生成する時はURLとユーザーアカウント、DBスキーマ名から
 * DapDirectとQueryManagerを生成する。通常接続ではこれにホストアドレスが加わり、
 * DapClientとQueryManger生成する。
 * 生成された接続はdbスキーマ名で指定できる。
 * 
 */
public class Connections implements IConsts {
  private HashMap<String, Object[]> cons = new HashMap<>();
  /**
   * QMFactory コンストラクター・コメント。
   */
  public Connections() {
    super();
  }
  /**
   * 指定された接続をクローズし、管理テーブルから取り除く。IDapClient, QueryManager共
   * 使用不能になるので、このメソッドはプログラムの終了時に呼び出すか、接続を使用している
   * オブジェクトに使用不能を通知するかしなければならない。
   * @param dbName java.lang.String
   */
  public void closeConnection(String dbName) {
    IDapClient dc = getDC(dbName);
    try {
      dc.dapCloseSession();
      dc.closePort();
    } catch (Exception e) {}
    cons.remove(dbName);
  }
  /**
   * 接続を作成する内部メソッド
   * @return boolean
   * @param dc dap.IDapClient
   * @param host java.lang.String
   * @param URL java.lang.String
   * @param user java.lang.String
   * @param password java.lang.String
   * @param dbName java.lang.String
   * @param charConversion java.lang.String
   */
  private boolean createConnection
    (IDapClient dc, String host, String URL, String user, String password, String dbName, String charConversion) {
    QueryManager qm = null;
    try {
      if (dc.openPort(host) == false) return false;
      if (dc.dapConnect(URL, user, password) == OK) {
	dc.dapSetCharConversion(charConversion);
	qm = new QueryManager(dc);
	Thread tqm = new Thread(qm);
	tqm.setDaemon(true);
	tqm.start();
	Object[] con = new Object[2];
	con[0] = dc;
	con[1] = qm;
	cons.put(dbName, con);
	return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return false;
  }
  /**
   * 通常の接続を生成し、管理テーブルに登録する。生成に失敗した場合はfalseを返す
   * @return boolean
   * @param host java.lang.String
   * @param URL java.lang.String
   * @param user java.lang.String
   * @param password java.lang.String
   * @param dbName java.lang.String
   */
  public boolean createConnection
    (String host,  String URL, String user, String password, String dbName) {
    IDapClient dc = new DapClient();
    return createConnection(dc, host, URL, user, password, dbName, null);
  }
  /**
   * 通常の接続を生成し、管理テーブルに登録する。生成に失敗した場合はfalseを返す
   * @return boolean
   * @param host java.lang.String
   * @param URL java.lang.String
   * @param user java.lang.String
   * @param password java.lang.String
   * @param dbName java.lang.String
   * @param charConversion java.lang.String
   */
  public boolean createConnection
    (String host,  String URL, String user, String password, String dbName, String charConversion) {
    IDapClient dc = new DapClient();
    return createConnection(dc, host, URL, user, password, dbName, charConversion);
  }
  /**
   * ダイレクト接続を生成し、管理テーブルに登録する。生成に失敗した場合はfalseを返す
   * @return boolean
   * @param URL java.lang.String
   * @param user java.lang.String
   * @param password java.lang.String
   * @param dbName java.lang.String
   */
  public boolean createDirectConnection
    (String URL, String user, String password, String dbName) {
    IDapClient dc = new DapDirect();
    return createConnection(dc, null, URL, user, password, dbName, null);
  }
  /**
   * ダイレクト接続を生成し、管理テーブルに登録する。生成に失敗した場合はfalseを返す
   * @return boolean
   * @param URL java.lang.String
   * @param user java.lang.String
   * @param password java.lang.String
   * @param dbName java.lang.String
   * @param charConversion java.lang.String
   */
  public boolean createDirectConnection
    (String URL, String user, String password, String dbName, String charConversion) {
    IDapClient dc = new DapDirect();
    return createConnection(dc, null, URL, user, password, dbName, charConversion);
  }
  /**
   * DBスキーマ名からIDapClientを返す
   * @return dap.IDapClient
   * @param dbName java.lang.String
   */
  public IDapClient getDC(String dbName) {
    Object[] con = (Object[])cons.get(dbName);
    if (con == null) return null;
    return (IDapClient)con[0];
  }
  /**
   * DBスキーマ名からQueryManagerを返す
   * @return dap.QueryManager
   * @param dbName java.lang.String
   */
  public QueryManager getQM(String dbName) {
    Object[] con = (Object[])cons.get(dbName);
    if (con == null) return null;
    return (QueryManager)con[1];
  }
}
