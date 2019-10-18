package formula;

import dap.*;
import java.util.*;
import java.text.*;
import java.sql.SQLException;
/**
 * ある製品の毒性リストのデータを保持するクラス。次の2系統のデータを保持する
 * 毒性表
 * 毒性番号(toxNo)-摘要(chemical)-閾値(threshold)-含有量(含有量 >= 閾値なら赤字で表示)
 * 原料寄与率表
 * 毒性番号(toxNo)-摘要(chemical)-資材名(mcode)-区分含有量
 * 毒性表で赤字で表示される毒性番号の行は、こちらでも赤字で表示
 * データは毒性テーブルと分解処方テーブルから求められるが、処方が更新中の場合は更新中の
 * データから求める必要があることに注意
 */
public class PoisonListM implements IConsts, IQueryClient {
  private int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private QueryManager qm;
  private FBrowseViewC fbc;
  private IFormulaModel fm = null;
  private int pcode;
  private IQueryClient client = null;
  private int clientMode;
  private Vector poison = null;
  private boolean[] pover = null; // 毒性表で閾値を越えている行でtrue
  private Vector contrb = null;
  private boolean[] cover = null; // 原料寄与率表で毒性表での閾値を越えた行に含まれる
  // 原材料の行でtrue
  private Vector poisonMcode = null;

  private static PQuery getPoison = null; // 製造コードから毒性表データを求む
  private static PQuery getContrb = null; // 製造コードから原料寄与率を求む
  private static Query getPoisont = null; // getPoisonの更新中版
  private static Query getContrbt = null; // getContrbの更新中版
  private static PUpdate setResolvt = null; // resolvtmpへの書き込み
  private static Update delResolvt = null; // resolvtmpをクリア
  /**
   * PoisonListM コンストラクター・コメント。
   */
  public PoisonListM(QueryManager qm) {
    this.qm = qm;
    startPrepare();
  }
  /**
   * 毒性が閾値を越えているものをマーキングする
   */
  private void chkOver() {
    int n = poison.size();
    pover = new boolean[n];
    Hashtable ht = new Hashtable();
    Boolean TRUE = new Boolean(true);
    Boolean FALSE = new Boolean(false);
    for (int i=0; i < n; i++) {
      Object[] oa = SQLutil.getRow(poison, i);
      double thr = ((Double)oa[2]).doubleValue();
      double inc = ((Double)oa[3]).doubleValue();
      pover[i] = (inc >= thr) ? true : false;
      ht.put((String)oa[0], pover[i] ? TRUE : FALSE);
    }

    n = contrb.size();
    cover = new boolean[n];
    for (int i=0; i < n; i++) {
      Boolean b = (Boolean)ht.get(SQLutil.get(contrb, i, 0));
      cover[i] = b.booleanValue();
    }
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
   * @return int
   */
  public int getContrbCount() {
    return contrb.size();
  }
  /**
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getContrbValueAt(int row, int column) {
    return SQLutil.get(contrb, row, column);
  }
  /**
   * @return int
   */
  public int getPoisonCount() {
    return poison.size();
  }
  /**
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getPoisonValueAt(int row, int column) {
    return SQLutil.get(poison, row, column);
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getPoison.getReady();
    int i2 = getContrb.getReady();
    int i3 = setResolvt.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0) ready = 1;
    return ready;
  }
  /**
   * 原料寄与率の行が閾値を越えているかどうかを返す
   * @return boolean
   * @param n int
   */
  public boolean isCover(int n) {
    return cover[n];
  }
  /**
   * 毒性の閾値を越える成分があればtrueを返す
   * @return boolean
   */
  public boolean isPoison() {
    for (int i=0; i < pover.length; i++) {
      if (pover[i]) return true;
    }
    return false;
  }
  /**
   * 毒性表の行が閾値を越えているかどうかを返す
   * @return boolean
   * @param n int
   */
  public boolean isPover(int n) {
    return pover[n];
  }
  /**
   * load メソッド・コメント。
   */
  public boolean load(IQueryClient client, int mode, int pcode) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    this.pcode = pcode;
    Object[] p = {new Integer(pcode)};
    getPoison.query(this, 1, p);
    getContrb.query(this, 2, p);
    return true;
  }
  /**
   * modeが1でgetPoison、2でgetContrb、3でgetPosonMcodeを扱うことを示す
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      //		valid = false;
      client.queryCallBack(o, mode);
      return;
    }
    switch (mode) {
    case 1:
      poison = (Vector)o;
      break;
    case 2:
      contrb = (Vector)o;
      chkOver();
      client.queryCallBack(o, clientMode);
      break;
    case 3:
      poisonMcode = (Vector)o;
      break;
    default :
    }
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (getPoison == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getPoison = new PQuery
	(qm, "select m.toxNo, i.chemical, i.threshold, sum(m.percent * r.percent / 100.0) " +
	 "from resolvf r, toxMcode m, toxIx i " +
	 "where r.pcode = ? and r.mcode = m.mcode and i.toxNo = m.toxNo " +
	 "group by m.toxNo, i.chemical, i.threshold order by m.toxNo", INtypes);
      getPoison.prepare();
    }

    if (getPoisont == null) {
      getPoisont = new Query
	(qm, "select m.toxNo, i.chemical, i.threshold, sum(m.percent * r.percent / 100.0) " +
	 "from resolvtmp r, toxMcode m, toxIx i " +
	 "where r.mcode = m.mcode and i.toxNo = m.toxNo " +
	 "group by m.toxNo, i.chemical, i.threshold order by m.toxNo");
    }

	
    if (getContrb == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getContrb = new PQuery
	(qm, "select m.toxNo, i.chemical, mname, (m.percent * r.percent / 100.0) " +
	 "from resolvf r, toxMcode m, toxIx i, mcode where r.pcode = ? " +
	 "and r.mcode = m.mcode and i.toxNo = m.toxNo and mcode.mcode = r.mcode " +
	 "order by m.toxNo, (m.percent * r.percent / 100.0) desc" , INtypes);
      getContrb.prepare();
    }

    if (getContrbt == null) {
      getContrbt = new Query
	(qm, "select m.toxNo, i.chemical, mname, (m.percent * r.percent / 100.0) " +
	 "from resolvtmp r, toxMcode m, toxIx i, mcode " +
	 "where r.mcode = m.mcode and i.toxNo = m.toxNo and mcode.mcode = r.mcode " +
	 "order by m.toxNo, (m.percent * r.percent / 100.0) desc");
    }

    if (setResolvt == null) {
      int[] INtypes = new int[2];
      INtypes[0] = INT;
      INtypes[1] = DOUBLE;
      setResolvt = new PUpdate
	(qm, "insert into resolvtmp values (?, ?)", INtypes);
      setResolvt.prepare();
    }

    if (delResolvt == null) {
      delResolvt = new Update
	(qm, "delete from resolvtmp");
    }
  }
  /**
   * 与えられた分解処方をresolvtmpに書き込み、それに基づいて毒性を求める
   * @param client dap.IQueryClient
   * @param mode int
   * @param resolved java.util.Vector 分解処方。並びはmcode, mname, percentになっている
   */
  public boolean update(final IQueryClient client, final int mode, final Vector resolved) {
    if (!chkReady()) return false;
    Runnable updater = new Runnable() {
      public void run() {
	int tID = qm.getTransactionID();
	try {
	  delResolvt.updateAndWait(tID);
	  for (int i=0, n=resolved.size(); i < n; i++) {
	    Object[] o = SQLutil.getRow(resolved, i);
	    Object[] out = {o[0], o[2]};
	    setResolvt.updateAndWait(out, tID);
	  }
	  qm.commitAndWait(tID);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  client.queryCallBack(e, SQLERROR);
	  return;
	}
	try {
	  poison = getPoisont.queryAndWait();
	  contrb = getContrbt.queryAndWait();
	  chkOver();
	  client.queryCallBack(null, mode);
	} catch (SQLException e) {
	  client.queryCallBack(e, SQLERROR);
	}
      }
    };
    Thread t = new Thread(updater);
    t.start();
    return true;
			
  }
}
