package dap;

import java.io.*;
import java.util.*;
import java.sql.*;
import myutil.*;
/**
 * マルチスレッド環境でのデータベースへのリクエストをシリアライズするクラス。
 * リクエストはcommandパターンのオブジェクトとしてキューイングされ、順次実行される。
 * リクエストにはブロック版も存在し、そちらはリクエストが処理されるまでリターンしない。
 *
 * トランザクション処理について
 * データベースの内容に変更を及ぼす処理は、まずgetTransactionID()でトランザクションIDを
 * 受け取り、引き続く処理ではこのIDを使用する。
 * コミットモードはトランザクションリクエストで自動的に非自動になり、コミットで自動になる。
 * トランザクションの途中でSQLエラーが発生した場合、トランザクションは自動的にロールバック
 * され、以降エラーが発生したのと同じIDを持つリクエストに対しては全てSQLエラーが返される。
 * この状態は同じIDを持つコミットかロールバックに対してエラーを返したところで終了する。
 * 従ってクライアントはトランザクションを必ずコミットかロールバックで終了しなければならない。
 */
public class QueryManager implements Runnable, IConsts {
  private IDapClient dc = null;
  private IGlobalErrorHandler geh = null;
  // コマンドキュー
  private List<Command> commands = new ArrayList<>();

  // ステートメントIDからSQL文を逆引きするためのハッシュ。デバッグ用
  private Hashtable id2sql = new Hashtable();

  // トランザクション管理用変数
  private boolean onTransaction = false;
  private int currentTransactionID = 0;
  private int nextTransactionID = 0;
  private boolean quitRequested = false;
  private boolean transactionErrored = false;

  // commandパターンを実装するインナークラスのインターフェイス
  interface Command {
    // 現在のトランザクションIDにマッチするかチェックするメソッド。
    // トランザクションに関係しないコマンドでは常にtrueを返す。
    boolean isTransactable(int currentTransactionId);
    void exec();
    String toString(); // このコマンドが発行するSQL文を表示
  }
  /**
   * QueryManager コンストラクター・コメント。
   */
  public QueryManager(IDapClient dc) {
    super();
    this.dc = dc;
  }
  /**
   * トランザクション開始時に呼び出されるメソッド。コミットモードを非自動にし、
   * トランザクションを開始させる
   */
  private boolean beginTransaction() {
    if (transactionErrored) return false;
    if (!onTransaction) {
      if (setAutoCommit(false) == false) {
	transactionErrored = true;
	return false;
      }
      onTransaction = true;
    }
    return true;
  }
  /**
   * コミットリクエストをキューに入れる。
   * このリクエストはトランザクションに係わる。
   * @param client dap.IQueryClient
   * @param mode int コールバックモード
   * @param transactionID int
   */
  public synchronized void
    commit(final IQueryClient client, final int mode, final int transactionID) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {
	return (transactionID == currentTransactionID) ? true : false;
      }
      public String toString() {
	return "commit";
      }
      public void exec() {
	if (transactionErrored) {
	  client.queryCallBack(null, SQLERROR);
	  transactionErrored = false;
	  onTransaction = false;
	  currentTransactionID++;
	  return;
	}
	try {
	  int rc = dc.dapCommit();
	  client.queryCallBack(new Integer(rc), mode);
	  onTransaction = false;
	  currentTransactionID++;
	  setAutoCommit(true);
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするコミット。
   * @param transactionID int
   */
  public void commitAndWait(int transactionID) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    commit(client, 0, transactionID);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
  }
  /**
   * グローバルエラーを通知する。具体的にはサーバーとの接続でIOExceptionが発生した場合、
   * 続行は不可能なのでそれに対処する
   * @param t java.lang.Throwable
   */
  private void fireGlobalError(Throwable t) {
    if (geh != null) geh.globalError(this, t);

  }
  /**
   * 次に利用可能なトランザクションIDを発行する
   * intがラウンドアップして次のIDが現在のIDに追いついてしまうという事態は想定していない
   * IDapClient:dapBeginTransaction()メソッドを呼び出し、ガベージコレクションを実行する
   */
  public synchronized int getTransactionID() {
    try {
      dc.dapBeginTransaction();
    } catch (Exception e) {
      fireGlobalError(e);
    }
    return nextTransactionID++;
  }
  /**
   * IDapClientがSQLExceptionを返した場合、エラーメッセージをログファイルに書き出す。
   * @param e SQLException
   */
  private void logSQLError(SQLException e) {
    LogManager lm = LogManager.getInstance();
    //    try {
	//      lm.write("formula.log", new String(e.getMessage().getBytes("8859_1"), "SJIS"));
    //    } catch (UnsupportedEncodingException ee) {}
    lm.write("formula.log", e.getMessage());
  }
  /**
   * prepared queryをリクエストする。
   * @param client dap.IQueryClient
   * @param callBackMode int
   * @param stmtID int
   * @param params java.lang.Object[]
   */
  public synchronized void
    pQuery(final IQueryClient client, final int callBackMode, final int stmtID, final Object[] params) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {return true;}
      public String toString() {
	return (String)id2sql.get(new Integer(stmtID));
      }
      public void exec() {
	try {
	  int rc = dc.dapPQuery(stmtID, params);
	  Object result = (rc == OK) ? dc.dapGetRow() : null;
	  client.queryCallBack(result, callBackMode);
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするpQuery
   * @return java.util.Vector
   * @param stmtID int
   * @param params java.lang.Object[]
   * @exception java.sql.SQLException
   */
  public Vector pQueryAndWait(int stmtID, Object[] params) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    pQuery(client, 0, stmtID, params);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
    return (Vector)o[0];
  }
  /**
   * SQL文のprepareをリクエストする
   * @param client dap.IQueryClient
   * @param callBackMode int
   * @param sql java.lang.String
   * @param INtypes int[]
   */
  public synchronized void
    prepare(final IQueryClient client, final int callBackMode, final String sql, final int[] INtypes) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {return true;}
      public String toString() {
	return "prepare";
      }
      public void exec() {
	try {
	  int stmtID = dc.dapPrepare(sql, INtypes);
	  id2sql.put(new Integer(stmtID), sql);
	  client.queryCallBack(new Integer(stmtID), callBackMode);
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするprepare
   * @return int
   * @param sql java.lang.String
   * @param INtypes int[]
   */
  public int prepareAndWait(String sql, int[] INtypes) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    prepare(client, 0, sql, INtypes);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
    return ((Integer)o[0]).intValue();
  }
  /**
   * prepared updateをリクエストする。
   * このexec()メソッドはトランザクションに係わる。
   * @param client dap.IQueryClient
   * @param mode int コールバックモード
   * @param stmtID int
   * @param params java.lang.Object[]
   * @param transactionID int
   */
  public synchronized void pUpdate
    (final IQueryClient client, final int mode, final int stmtID, final Object[] params, final int transactionID) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {
	return (transactionID == currentTransactionID) ? true : false;
      }
      public String toString() {
	return (String)id2sql.get(new Integer(stmtID));
      }
      public void exec() {
	if (beginTransaction() == false) {
	  client.queryCallBack(null, SQLERROR);
	  return;
	}
	try {
	  int rc = dc.dapPUpdate(stmtID, params);
	  client.queryCallBack(new Integer(rc), mode);
	} catch (SQLException e) {
	  transactionError(client, mode, e);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするpUpdate
   * @return int
   * @param stmtID int
   * @param params java.lang.Object[]
   * @param transactionID int
   */
  public int pUpdateAndWait(int stmtID, Object[] params, int transactionID) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    pUpdate(client, 0, stmtID, params, transactionID);
    lock.get(1);

    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
    return ((Integer)o[0]).intValue();
  }
  /**
   * queryをリクエストする。
   * @param client dap.IQueryClient
   * @param callBackMode int
   * @param sql java.lang.String
   */
  public synchronized void
    query(final IQueryClient client, final int callBackMode, final String sql) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {return true;}
      public String toString() {
	return "static query";
      }
      public void exec() {
	try {
	  int rc = dc.dapQuery(sql);
	  Object result = (rc == OK) ? dc.dapGetRow() : null;
	  client.queryCallBack(result, callBackMode);
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするquery
   * @return java.util.Vector
   * @param sql java.lang.String
   */
  public Vector queryAndWait(String sql) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    query(client, 0, sql);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
    return (Vector)o[0];
  }
  /**
   * 処理を終了する。現在のトランザクションはロールバックされ、このリクエストより
   * 後に発行されたリクエストは全て無効となる。
   */
  public synchronized void quit(final IQueryClient client, final int callBackMode) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {return true;}
      public String toString() {
	return "quit";
      }
      public void exec() {
	try {
	  quitRequested = true;
	  int rc = dc.dapRollback();
	  client.queryCallBack(new Integer(rc), callBackMode);
	  onTransaction = false;
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするquit
   */
  public void quitAndWait() throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    quit(client, 0);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
  }
  /**
   * ロールバックをリクエストする。
   * このリクエストはトランザクションに係わる。
   * @param client dap.IQueryClient
   * @param callBackMode int
   * @param transactionID int
   */
  public synchronized void
    rollback(final IQueryClient client, final int callBackMode, final int transactionID) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {
	return (transactionID == currentTransactionID) ? true : false;
      }
      public String toString() {
	return "rollback";
      }
      public void exec() {
	if (transactionErrored) {
	  client.queryCallBack(null, SQLERROR);
	  transactionErrored = false;
	  onTransaction = false;
	  currentTransactionID++;
	  return;
	}
	try {
	  int rc = dc.dapRollback();
	  client.queryCallBack(new Integer(rc), callBackMode);
	  onTransaction = false;
	  currentTransactionID++;
	  setAutoCommit(true);
	} catch (SQLException e) {
	  logSQLError(e);
	  client.queryCallBack(e, SQLERROR);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするrollback
   * @param transactionID int
   */
  public void rollbackAndWait(int transactionID) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    rollback(client, 0, transactionID);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw new SQLException();
  }
  /**
   * キューイングされたリクエストを順次実行するループ。
   * トランザクションをシリアライズするため、トランザクション実行中には
   * リクエストを取り出す前にトランザクションIDをチェックしている。
   */
  public void run() {
    Command cmd = null;
    for (;;) {
      waitRequest();
      int i, n;
      synchronized(commands) {
	for (i=0, n=commands.size(); i < n; i++) {
	  cmd = commands.get(i);
	  if (onTransaction &&
	      !cmd.isTransactable(currentTransactionID)) continue;
	  else {
	    commands.remove(i);
	    break;
	  }
	}
      }
      if (i == n) continue; // no request is transactable
      // System.out.print(cmd.toString());
      // long t0 = System.currentTimeMillis();	
      cmd.exec();
      // long t1 = System.currentTimeMillis();
      // System.out.println(" " + (t1-t0));
      if (quitRequested) break;
    }
  }
  /**
   * コミットモードを設定する。成功すればtrueを返す
   * @param b boolean
   */
  private boolean setAutoCommit(boolean b) {
    try {
      int rc = dc.dapAutoCommit(b);
      return (rc == SQLERROR) ? false : true;
    } catch (SQLException e) {
      logSQLError(e);
      return false;
    } catch (IOException e) {
      fireGlobalError(e);
      return false;
    }
  }
  /**
   * @param geh myutil.IGlobalErrorHandler
   */
  public void setGEH(IGlobalErrorHandler geh) {
    this.geh = geh;
  }
  /**
   * トランザクションの途中でSQLERRORが発生した際に後処理をするメソッド
   * エラーをクライアントに通知し、ロールバックを行い、現在のトランザクションを終了させ、
   * コミットモードを自動にする
   */
  private void transactionError(IQueryClient client, int mode, SQLException e) {
    try {
      transactionErrored = true;
      logSQLError(e);
      client.queryCallBack(e, SQLERROR);
      dc.dapRollback();
      setAutoCommit(true);
    } catch (SQLException ee) {
      logSQLError(ee);
    } catch (IOException ee) {
      fireGlobalError(e);
    }
  }
  /**
   * updateをリクエストする。
   * このexec()メソッドはトランザクションに係わる。
   * @param client dap.IQueryClient
   * @param mode int コールバックモード
   * @param sql java.lang.String
   * @param transactionID int
   */
  public synchronized void update
    (final IQueryClient client, final int mode, final String sql, final int transactionID) {
    Command cmd = new Command() {
      public boolean isTransactable(int currentTransactionID) {
	return (transactionID == currentTransactionID) ? true : false;
      }
      public String toString() {
	return "static update";
      }
      public void exec() {
	if (beginTransaction() == false) {
	  client.queryCallBack(null, SQLERROR);
	  return;
	}
	try {
	  int rc = dc.dapUpdate(sql);
	  client.queryCallBack(new Integer(rc), mode);
	} catch (SQLException e) {
	  transactionError(client, mode, e);
	} catch (IOException e) {
	  fireGlobalError(e);
	}
      }
    };

    commands.add(cmd);
    notifyAll();
  }
  /**
   * ブロックするupdate
   * @return int
   * @param sql java.lang.String
   * @param transactionID int
   */
  public int updateAndWait(String sql, int transactionID) throws SQLException {
    final SpinLock lock = new SpinLock(0);
    final Object[] o = new Object[1];
    final int[] callBackMode = new int[1];
    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object iO, int iCallBackMode) {
	o[0] = iO;
	callBackMode[0] = iCallBackMode;
	lock.set(1);
      }
    };

    update(client, 0, sql, transactionID);
    lock.get(1);
    if (callBackMode[0] == SQLERROR) throw (SQLException)o[0];
    return ((Integer)o[0]).intValue();
  }
  /**
   * コマンドキューにリクエストが到着するのを待つ
   */
  private synchronized void waitRequest() {
    while (commands.size() == 0) {
      try {
	wait();
      } catch (InterruptedException e) {}
    }
  }
}
