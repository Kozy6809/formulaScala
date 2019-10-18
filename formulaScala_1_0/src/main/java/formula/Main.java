package formula.java;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import dap.*;
import formula.ui.*;
import myutil.*;


/**
 * この型は VisualAge で作成されました。
 */
public class Main implements IGlobalErrorHandler {
  private static Splash sp = null; // 接続試行ダイアログ
  private static ConnectErr cn = null; // 接続失敗ダイアログ
  private static final String dbName = "formula";
  private static Connections cons = null;
  private QueryManager qm = null;
  private MainC mvc = null;
  private static Vector wins = new Vector(); // ウィンドウコントローラのリスト

  /**
   * Formula2 コンストラクター・コメント。
   */
  public Main() {
    super();
  }
  /**
   * Formula2 コンストラクター・コメント。
   */
  public Main(QueryManager qm) {
    super();
    this.qm = qm;
  }
  /**
   * ウィンドウコントローラを登録する
   * @param wc myutil.IWinControl
   */
  public static void addWin(IWinControl wc) {
    wins.addElement(wc);
  }
  /**
   * このメソッドは VisualAge で作成されました。
   */
  private static void connectFailed() {
    cn = new ConnectErr();
    cn.setVisible(true);
    sp.setVisible(false);
    cons.closeConnection(dbName);
  }
  /**
   * プログラムを終了する。QueryManagerが終了するのを待ち、
   * 次いでConnectionをcloseする
   */
  public void exit() {
    try {
      qm.quitAndWait();
    } catch (java.sql.SQLException e) {}
    cons.closeConnection("formula");
    System.exit(0);
  }
  /**
   * グローバルエラーの処理。このメソッドはネットワークが切断された時に呼ばれる
   * ダイアログを表示し、全ての処理を終了させる
   * 処方データベースは単一の接続しか張らないので問題ないが、複数の接続を張る
   * アプリケーションでは、まだ生きている接続の処理をきちんと行わねばならない
   * @param source java.lang.Object
   * @param t java.lang.Throwable
   */
  public void globalError(Object source, Throwable t) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(mvc.getMV(),
                  "データベースとの接続が失われました　\n" +
                          "プログラムを終了します　\n\n" +
                          "更新途中のデータは全て失われます　",
                  "重大なエラー",
                  JOptionPane.ERROR_MESSAGE);
        }
      });
    } catch (Exception e) {}
    System.exit(1);
  }

  /**
   */
  private void go() {
    qm.setGEH(this);
    mvc = new MainC();
    sp.setVisible(false);
    String[] s = null;
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

    String host = "127.0.0.1";
//    String db = "jdbc:odbc:formula";
    String db = "jdbc:sqlserver://localhost\\SQLEXPRESS;database=formula;integratedSecurity=true;";
    String user = "formula";
    String password = "formula";
    String charConversion = "";
    //String charConversion = null;

    // 接続試行中ダイアログ表示
    sp = new Splash();
    sp.setLocation(100, 100);
    sp.setVisible(true);

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
      if (cons.createDirectConnection
              (db, user, password, dbName, charConversion) == false) {
        connectFailed();
        return;
      }
    } else {
      if (cons.createConnection
              (host, db, user, password, dbName, charConversion) == false) {
        connectFailed();
        return;
      }
    }
    Main f = new Main(cons.getQM(dbName));
    f.go();
    Env.setup();
  }
  /**
   * ウィンドウコントローラを削除する
   * @param wc myutil.IWinControl
   */
  public static void removeWin(IWinControl wc) {
    wins.removeElement(wc);
  }
  /**
   * 登録されたウィンドウコントローラにクローズリクエストを送る。
   * 全てのコントローラがリクエストを受付ければtrue、さもなくばfalseを返す
   * @return boolean
   */
  public boolean requestClose() {
    boolean allDone = true;
    // winsの要素は動的に変化するため、静的なコピーを使用する
    Vector winsF = (Vector)wins.clone();
    for (int i=0, n=winsF.size(); i < n; i++) {
      boolean rc = ((IWinControl)winsF.elementAt(i)).requestClose();
      if (!rc) allDone = false;
    }
    return allDone;
  }
}

/*
$Id: Main.java,v 1.1 2008/10/17 01:15:40 wakui Exp $
*/
