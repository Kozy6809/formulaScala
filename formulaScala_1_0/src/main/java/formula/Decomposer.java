package formula;

import java.util.*;
import java.sql.*;
import dap.*;
import myutil.*;
import myutil.Queue;
/**
 * 分解処方テーブルをハンドリングする。次の機能を実装する
 * 分解処方の取り出し
 * 通常処方から分解処方を計算
 * 分解処方の書き込み
 */
public class Decomposer implements IConsts, IQueryClient {
  private static final int COMPOUND = 500000; // 資材コードで資材と中間品を判別するmagic number
  private QueryManager qm;
  private static PQuery getResolv = null;
  private static PQuery getDependant = null; // ある製品に依存する製品のコードをリストアップ
  private static PQuery getShortForm = null; // 短形式(pcode, mcode, percent)の製品処方
  private static PUpdate setResolv = null;
  private static PUpdate delResolv = null;
  private static int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private boolean valid = false; // データが正しい検索結果かどうか示す
  private IQueryClient client = null;
  private int clientMode = 0;
  private static Queue resolvRequests = new Queue(false); // 処方分解のリクエストを蓄積する
  private static boolean bgProcessing = false; // バックグランドで処方分解を実行中であることを示す
	
  // 指定された製品に依存する製品をリストし、その分解処方を削除して再計算する内部クラス
  private class Resolver implements Runnable {
    public void run() {
      for (;; resolvRequests.get()) {
	Integer p = (Integer)resolvRequests.peek();
	if (p == null) {
	  bgProcessing = false;
	  return;
	}
	Vector dependants = listDependants(p);
	dependants.addElement(new Object[]{p});

	// 依存製品の分解処方を削除する。エラーが発生した場合はロールバックし、
	// 次のリクエストの処理を開始する
	int tID = qm.getTransactionID();
	try {
	  for (int i=0, n=dependants.size(); i < n; i++) {
	    Integer pcode = (Integer)SQLutil.get(dependants, i, 0);
	    Vector r = getResolv.queryAndWait(new Object[]{pcode});
	    // resolvf表にデータがある場合に限り削除を実行する。データが無い時に
	    // 削除するとSQLWarningがthrowされるが、これはSQLExceptionのサブクラス
	    // であるため、SQLExceptionとしてキャッチされてしまい、正常な処理が
	    // 実行できなくなる
	    if (r == null || r.size() == 0) continue;
	    delResolv.updateAndWait(new Object[]{pcode}, tID);
	  }
	  qm.commitAndWait(tID);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  continue;
	}
	// 依存製品の分解処方を作成する。エラーが発生した場合、次の依存製品へ進む
	for (int i=0, n=dependants.size(); i < n; i++) {
	  Integer pcode = (Integer)SQLutil.get(dependants, i, 0);
	  try {
	    Vector r = getResolv.queryAndWait(new Object[]{pcode});
	    if (r != null && r.size() > 0) continue; 
	    int code = pcode.intValue();
	    Vector t = getShortForm.queryAndWait(new Object[]{pcode});
	    r = calcDecomp(code, t, true);
	    writeResolvf(code, r);
	  } catch (SQLException e) {}
	}
      }
    }
  };
  /**
   * Decomposer コンストラクター・コメント。
   */
  public Decomposer(QueryManager qm) {
    super();
    this.qm = qm;
    startPrepare();
  }
  /**
   * 与えられた通常処方から分解処方を計算し、clientに渡す
   * normDataにはmcode, mname, percent, status, orderの順にデータが格納されている
   * 循環参照のチェックは、自分自身を原料に直接含むケースのみをサポートしている
   *
   * このメソッドは*AndWait()メソッドを呼び出しているため、QueryManagerのスレッドから
   * 呼び出してはならない。さもないとデッドロックが発生する
   * @param client dap.IQueryClient
   * @param mode int
   * @param normData java.util.Vector
   */
  private Vector calcDecomp(int pcode, Vector normData, boolean bgMode) {
    if (!chkReady()) return null;
    double cyclicInclusionFactor = 0.0; // 循環参照している場合、その比率
    Vector accum = new Vector(); // 途中結果を格納する

    for (int i=0, n=normData.size(); i < n; i++) {
      Object[] normRow = SQLutil.getRow(normData, i);
      Integer mcodeInteger = (Integer)normRow[0];
      int mcode = mcodeInteger.intValue();
      if (mcode == pcode) {
	cyclicInclusionFactor += ((Double)normRow[2]).doubleValue();
	continue;
      }
      if (mcode < COMPOUND) {
	accum.addElement(normRow);
	continue;
      }
      Vector r = null;
      try {
	r = getResolv.queryAndWait(new Object[]{mcodeInteger});
      } catch (SQLException e) {}
      try {
	if (r == null || r.size() == 0) {
	  Vector t = getShortForm.queryAndWait(new Object[]{mcodeInteger});
	  r = calcDecomp(mcode, t, bgMode);
	  // 処方を書き込むのはバックグランドのスレッドのみにする
	  if (bgMode) writeResolvf(mcode, r);
	}
	double f = ((Double)normRow[2]).doubleValue() / 100.0;
	r = multiply(r, f);
	for (int j=0, k=r.size(); j < k; j++) {
	  accum.addElement(r.elementAt(j));
	}
      } catch (SQLException e) {}
    }
    Vector result = eliminate0(sumUp(sort(accum)));
    if (cyclicInclusionFactor > 0.0) {
      result = multiply(result, 1.0 / (1.0 - cyclicInclusionFactor / 100.0));
    }
    return result;
  }
  /**
   * パブリックなcalcDecomp。これに渡される処方は編集中か、旧処方のものであるので、
   * 再計算された分解処方はdbに書き込まない
   * privateなcalcDecomp()は再計算の際に与えられたデータオブジェクト自体を書き換るため、
   * 一旦データをコピーして渡している
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   * @param normData java.util.Vector
   */
  public void calcDecomp
    (final IQueryClient client, final int mode, final int pcode, Vector normData) {
    final Vector data = new Vector();
    for (int i=0, n=normData.size(); i < n; i++) {
      Object[] o = (Object[])SQLutil.getRow(normData, i).clone();
      data.addElement(o);
    }
    Thread t = new Thread(new Runnable() {
      public void run() {
	Vector r = calcDecomp(pcode, data, false);
	client.queryCallBack(r, (r == null) ? SQLERROR : mode);
      }
    });
    t.start();
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
   * 入力から資材比率が0になっている行を除去するフィルタ
   * @return java.util.Vector
   * @param in java.util.Vector
   */
  private Vector eliminate0(Vector in) {
    Vector out = new Vector();
    for (int i=0, n=in.size(); i < n; i++) {
      Object[] oa = SQLutil.getRow(in, i);
      Double percent = (Double)oa[2];
      if (percent.doubleValue() > 0.0) out.addElement(oa);
    }
    return out;
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getResolv.getReady();
    int i2 = setResolv.getReady();
    int i3 = delResolv.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0) ready = 1;
    return ready;
  }
  /**
   * @return boolean
   */
  public static boolean isBgProcessing() {
    return bgProcessing;
  }
  /**
   * ある製品に直接的及び間接的に依存する製品を全てリストアップする。
   * エラーが発生した場合はnullを返す
   * @return java.util.Vector
   * @param pcode int
   */
  public Vector listDependants(Integer pcode) {
    Vector result = new Vector();
    Object[] oa = {pcode};
    try {
      Vector r = getDependant.queryAndWait(oa);
      for (int i=0, n=r.size(); i < n; i++) {
	oa = SQLutil.getRow(r, i);
	if (pcode.equals((Integer)oa[0])) continue; // skip cyclic reference
	result.addElement(oa);
	Vector subResult = listDependants((Integer)oa[0]);
	for (int j=0, k=subResult.size(); j < k; j++) {
	  result.addElement(subResult.elementAt(j));
	}
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    return result;
  }
  /**
   * 分解処方テーブルから指定されたpcodeのデータを取り出す。
   * 分解処方が存在しなかった場合、通常処方から再作成するが、
   * クライアントへの戻り値はサイズ0のVectorになる
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   */
  public boolean load(final IQueryClient client, final int mode, final int pcode, final Vector normData) {
    if (!chkReady()) return false;
    Object[] p = {new Integer(pcode)};
    getResolv.query(new IQueryClient() {
      public void queryCallBack(Object o, int mode0) {
	if (mode0 == SQLERROR) {
	  client.queryCallBack(null, SQLERROR);
	  return;
	}
	Vector r = (Vector)o;
	if (r.size() == 0) {
	  updateResolvf(pcode);
	}
	client.queryCallBack(r, mode);
      }
    }, mode, p);
    return true;
  }
  /**
   * 処方データのpercentにfactorを乗じる
   * @return java.util.Vector
   * @param data java.util.Vector
   * @param factor double
   */
  private Vector multiply(Vector data, double factor) {
    for (int i=0, n=data.size(); i < n; i++) {
      Object[] oa = SQLutil.getRow(data, i);
      oa[2] = new Double(((Double)oa[2]).doubleValue() * factor);
    }
    return data;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
  }
  /**
   * ResolvAllから呼び出される、全処方の再計算を実行するメソッド
   * @param pcode java.lang.Integer
   */
  public void resolvAll(Integer pcode) {
    try {
      Vector r = getResolv.queryAndWait(new Object[]{pcode});
      if (r.size() > 0) return;
      Vector n = getShortForm.queryAndWait(new Object[]{pcode});
      r = calcDecomp(pcode.intValue(), n, true);
      writeResolvf(pcode.intValue(), r);
    } catch (SQLException e) {}
  }
  /**
   * 与えられた処方データを資材コード(各行の最初の列)でソートする。アルゴリズムはバブル
   * 与えられたVectorオブジェクト自体がソートされる
   * @return java.util.Vector
   * @param in java.util.Vector
   */
  private Vector sort(Vector data) {
    for (;;) {
      boolean exchanged = false;
      for (int i=0, n=data.size(); i < n-1; i++) {
	int m0 = ((Integer)SQLutil.get(data, i, 0)).intValue();
	int m1 = ((Integer)SQLutil.get(data, i+1, 0)).intValue();
	if (m0 > m1) {
	  Object to = data.elementAt(i);
	  data.setElementAt(data.elementAt(i+1), i);
	  data.setElementAt(to, i+1);
	  exchanged = true;
	}
      }
      if (!exchanged) break;
    }
    return data;
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (getResolv == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getResolv = new PQuery
	(qm, "select r.mcode, m.mname, r.percent, m.status from resolvf r, mcode m " +
	 "where r.mcode = m.mcode and r.pcode = ? " + 
	 "order by r.mcode", INtypes);
      getResolv.prepare();
    }

    if (getDependant == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getDependant = new PQuery
	(qm, "select distinct pcode from form1 where mcode = ?", INtypes);
      getDependant.prepare();
    }

    // 短形式の処方を取得する。ここでmcodeがダブっているのは、calcDecomp()の受付ける
    // 通常処方がmcode, mname, percent...という形式になっており、このメソッドを使用
    // する上でpercentのカラム位置を3列目に持っていくため
    if (getShortForm == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getShortForm = new PQuery
	(qm, "select mcode, mcode, percent from form1 where pcode = ?", INtypes);
      getShortForm.prepare();
    }

    if (setResolv == null) {
      int[] INtypes = new int[3];
      INtypes[0] = INT;
      INtypes[1] = INT;
      INtypes[2] = FLOAT;
      setResolv = new PUpdate(qm, "insert into resolvf values(?, ?, ?)", INtypes);
      setResolv.prepare();
    }
	
    if (delResolv == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delResolv = new PUpdate(qm, "delete from resolvf where pcode = ?", INtypes);
      delResolv.prepare();
    }
  }
  /**
   * 与えられた処方データの同じ資材の比率を加算する。入力はあらかじめ資材コードで
   * ソートされている必要がある
   * @return java.util.Vector
   * @param data java.util.Vector
   */
  private Vector sumUp(Vector data) {
    // dataの末尾にsentinnelを配置する。正当な資材コードは負にならないことが仮定されている
    data.addElement(new Object[]{new Integer(-1), null, new Double(0.0)});
    Vector out = new Vector();
    int m0 = ((Integer)SQLutil.get(data, 0, 0)).intValue();
    double p0 = ((Double)SQLutil.get(data, 0, 2)).doubleValue();
    for (int i=1, n=data.size(); i < n; i++) {
      int m1 = ((Integer)SQLutil.get(data, i, 0)).intValue();
      double p1 = ((Double)SQLutil.get(data, i, 2)).doubleValue();
      if (m1 != m0) {
	Object[] oa = SQLutil.getRow(data, i-1);
	oa[2] = new Double(p0);
	out.addElement(oa);
	m0 = m1;
	p0 = p1;
      } else {
	p0 += p1;
      }
    }
    return out;
  }
  /**
   * 分解処方テーブルの更新を実行する。指定された製造コードの製品及びその製品に依存する
   * 全ての製品の分解処方を再帰的に更新する
   * 依存する製品に自分自身が出現する可能性もある
   */
  public void updateResolvf(int pcode) {
    if (!chkReady()) return;
    resolvRequests.put(new Integer(pcode));
    if (!bgProcessing) {
      bgProcessing = true;
      Thread thread = new Thread(new Resolver());
      thread.setPriority(Thread.NORM_PRIORITY-1);
      thread.start();
    }
  }
  /**
   * 単一の処方の分解処方を更新する。この処方に依存する処方の更新は行わない
   * @param pcode int
   */
  public boolean updateSingle(int pcode) {
    int tID = qm.getTransactionID();
    try {
      Integer pc = new Integer(pcode);
      Object[] param = new Object[]{pc};
      // resolvf表にデータが無い時に削除を実行するとSQLWarningがthrowされる
      try {
	delResolv.updateAndWait(param, tID);
      } catch (SQLWarning e) {}
      Vector t = getShortForm.queryAndWait(param);
      // 被依存処方をdbに書き込まないモードでcalcDecompを呼び出す
      // 分解処方表が整合状態にあったなら、被依存処方が必ず表中に存在するため
      Vector r = calcDecomp(pcode, t, false);
      writeResolvf(pcode, r, tID);
      qm.commitAndWait(tID);
      return true;
    } catch (SQLException e) {
      try {
	qm.rollbackAndWait(tID);
      } catch (SQLException ee) {}
      return false;
    }
  }
  /**
   * 分解処方を書き込む。エラーが発生した場合は書き込まれない
   * このメソッドは処理が終了するまで返らない
   * @param pcode int
   * @param form java.util.Vector
   */
  private void writeResolvf(int pcode, Vector form) {
    if (!chkReady()) return;
    Integer p = new Integer(pcode);
    int tID = qm.getTransactionID();
    boolean failed = false;
    try {
      for (int i=0, n=form.size(); i < n; i++) {
	Object[] o = new Object[3];
	o[0] = p;
	o[1] = SQLutil.get(form, i, 0); // mcode
	o[2] = SQLutil.get(form, i, 2); // percent is stored in the 3rd column!
	int rc = setResolv.updateAndWait(o, tID);
	if (rc < 0) {
	  failed = true;
	  break;
	}
      }
    } catch (SQLException e) {failed = true;}

    try {
      if (failed) {
	qm.rollbackAndWait(tID); // エラーが起きた場合には既にロールバックされているが、
	// エラー状態を終了させるためにこれが必要
      } else {
	qm.commitAndWait(tID);
      }
    } catch (SQLException e) {}
  }
  /**
   * 分解処方を書き込む。失敗したらfalseを返す。トランザクションIDをパラメータに取り、
   * 上位のトランザクションの一部として機能する
   * @param pcode int
   * @param form java.util.Vector
   */
  private boolean writeResolvf(int pcode, Vector form, int tID) {
    if (!chkReady()) return false;
    Integer p = new Integer(pcode);
    try {
      for (int i=0, n=form.size(); i < n; i++) {
	Object[] o = new Object[3];
	o[0] = p;
	o[1] = SQLutil.get(form, i, 0); // mcode
	o[2] = SQLutil.get(form, i, 2); // percent is stored in the 3rd column!
	int rc = setResolv.updateAndWait(o, tID);
	if (rc < 0) return false;
      }
      return true;
    } catch (SQLException e) {
      return false;
    }
  }
}
