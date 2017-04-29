package formula;

import dap.*;
import java.util.*;
import java.text.*;
import java.sql.SQLException;
import myutil.*;
/**
 * 処方が業務部で登録されたか確認するためのクラス。form2テーブルのconfdateカラムを
 * チェックし、このカラムがnullになっている製品をリストして表示する
 * 登録が確認された製品はリストから削除する。この処理は該当する製品のconfdateカラムに
 * 登録日付(=現在時刻)を記入することで行われる
 */
public class ConfRegM implements IConsts, IQueryClient {
  private int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private QueryManager qm;
  private IQueryClient client = null;
  private int mode = -1;

  private static Query chkConfDate = null;
  private static PUpdate setConfDate = null;

  private Vector data = null; // date, pcode, series, name, person
  /**
   * ConfReg コンストラクター・コメント。
   */
  public ConfRegM(QueryManager qm) {
    this.qm = qm;
    startPrepare();
  }
  /**
   * このオブジェクトの準備(即ち全てのprepareの完了)ができているかチェックし、
   * 未了ならば完了を待ってtrueを返す。もしエラーになればfalseを返す。
   * @return boolean
   */
  public boolean chkReady() {
    for (; getReady() == 0;) {
      Thread.yield();
    }
    return (getReady() > 0) ? true : false;
  }
  /**
   * @return java.lang.Object
   * @param row int
   * @param col int
   */
  public Object getData(int row, int col) {
    return SQLutil.get(data, row, col);
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = setConfDate.getReady();

    if (i1 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0) ready = 1;
    return ready;
  }
  /**
   * @return int
   */
  public int getSize() {
    return data.size();
  }
  /**
   * load メソッド・コメント。
   */
  public boolean load(IQueryClient client, int mode) {
    if (!chkReady()) return false;
    this.client = client;
    this.mode = mode;
    chkConfDate.query(this, 1);
    return true;
  }
  /**
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      client.queryCallBack(o, mode);
      return;
    }
    switch (mode) {
    case 1:
      data = (Vector)o;
      client.queryCallBack(o, this.mode); // このmodeはclientによって指定された値!!
      break;
    default :
    }
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (chkConfDate == null) {
      chkConfDate = new Query
	(qm, "select f.date, f.pcode, series, name, person from pcode p, form2 f " +
	 "where f.pcode = p.pcode and f.confdate is null order by f.date, f.pcode");
    }

    if (setConfDate == null) {
      int[] INtypes = new int[2];
      INtypes[0] = TIMESTAMP;
      INtypes[1] = INT;
      setConfDate = new PUpdate
	(qm, "update form2 set confdate = ? where pcode = ?", INtypes);
      setConfDate.prepare();
    }
  }
  /**
   * 与えられた製造コードのリストに基づき、form2テーブルのconfDateカラムに現在時刻を書き込む
   * @return boolean
   * @param pcodes java.util.Vector
   */
  public boolean update(IQueryClient client, int mode, final Vector pcodes) {
    if (!chkReady()) return false;
    this.client = client;
    this.mode = mode;
    Runnable updater = new Runnable() {
      public void run() {
	int tID = qm.getTransactionID();
	try {
	  Object[] p = new Object[2];
	  p[0] = new Date();
	  for (int i=0, n=pcodes.size(); i < n; i++) {
	    p[1] = pcodes.elementAt(i);
	    setConfDate.updateAndWait(p, tID);
	  }
	  qm.commitAndWait(tID);
	  ConfRegM.this.client.queryCallBack(null, ConfRegM.this.mode);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  ConfRegM.this.client.queryCallBack(e, SQLERROR);
	}
      }
    };

    Thread t = new Thread(updater);
    t.start();
    return true;
  }
}
