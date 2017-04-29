package formula;

import java.util.*;
import dap.*;
/**
 * 全ての製品の分解処方を再計算する
 */
public class ResolvAll {
  private static final String dbName = "formula";
  private static Connections cons;
  private QueryManager qm;
  private Decomposer dcmp;
  private Query getAllPcode;
  private Vector pcode = null;
  /**
   * ResolvAll コンストラクター・コメント。
   */
  public ResolvAll() {
    super();
  }
  /**
   * ResolvAll コンストラクター・コメント。
   */
  public ResolvAll(QueryManager qm) {
    super();
    this.qm = qm;
    dcmp = new Decomposer(qm);
    getAllPcode = new Query(qm, "select pcode from pcode");
  }
  /*
    コマンドライン引数の仕様
    0個の場合…ダイレクトモード。ローカルの"odbc:formula"に接続
    1個の場合…ダイレクトモードで指定されたdbに接続
    2個の場合…引数はホスト名(or IPアドレス)及びdb名。db名にサブプロトコルが
    指定されていない場合は、デフォルトで"odbc:"が付加される

    まず接続試行中のダイアログを表示。
    ダイレクトモードでは利用可能なJDBCドライバをロードし、
    DapDirectのインスタンスを生成する。
    通常モードではDapClientのインスタンスを生成し、指定されたホストに接続する。


  */
  public static void main(String[] args) {
    boolean direct = false;
    boolean specialFunc = false;

    String host = "127.0.0.1";
    String db = "jdbc:odbc:formula";
    String user = "formula";
    String password = "formula";
    String charConversion = "SJIS";

    // パラメータ解析
    switch (args.length) {
    case 0:
      direct = true;
      break;
    case 1:
      direct = true;
      if (args[0].indexOf((int)':') < 0) {
	db = "jdbc:odbc:" + args[0];
      } else db = args[0];
      break;
    case 2:
      host = args[0];
      if (args[1].indexOf((int)':') < 0) {
	db = "jdbc:odbc:" + args[1];
      } else db = args[1];
      break;
    default:
    }

    cons = new Connections();
    if (direct) {
      try {
	Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
      } catch (ClassNotFoundException e) {
	System.out.println(e.getMessage());
      }
      if (cons.createDirectConnection
	  (db, user, password, dbName, charConversion) == false) {
	System.out.println("connection failed");
	return;
      }
    } else {
      if (cons.createConnection
	  (host, db, user, password, dbName, charConversion) == false) {
	System.out.println("connection failed");
	return;
      }
    }
    ResolvAll ra = new ResolvAll(cons.getQM(dbName));
    ra.process();
  }
  /**
   * 分解処方の再計算を実行する
   */
  private void process() {
    try {
      pcode = getAllPcode.queryAndWait();
    } catch (java.sql.SQLException e) {}
    if (dcmp.chkReady()) {
      for (int i=0, n=pcode.size(); i < n; i++) {
	Integer p = (Integer)SQLutil.get(pcode, i, 0);
	System.out.println(p);
	dcmp.resolvAll(p);
      }
    }
    try {
      qm.quitAndWait();
    } catch (java.sql.SQLException e) {}
    cons.closeConnection("formula");
    System.exit(0);
  }
}
