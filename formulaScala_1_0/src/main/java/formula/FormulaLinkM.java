package formula;

import dap.*;
import java.util.*;
import java.sql.*;
/**
 * 処方のリンク状態を表現するオブジェクト。dbの処方リンクテーブルと連動する
 */
public class FormulaLinkM implements IConsts {
  private static int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private QueryManager qm;

  private static Query getAll = null;
  private static Query getOrder = null; // 最初にデータをロードした時にリンクグループの
  // 並び順を決定するためのデータ。リンクグループは
  // それが含む製造コードの最小値の順で並べられる
  private static PQuery code2name = null;
  private static PQuery getOne = null; // ある製造コードが既にテーブルに含まれているか
  private static PQuery getGrp = null; // ある製造コードを含むグループのメンバー
  private static PUpdate setLink = null;
  private static PUpdate delLink = null;
  private static PUpdate delLinkGrp = null;

  private boolean loaded = false;
  private Vector data = null;
  // dataの各要素には、リンクID、製造コード、品種名+色名が格納される
  // この内dbの処方リンクテーブルに格納されているのは前の2つだけなので、
  // dbからデータをロードした後、製造コードから名前を取り出す必要がある

  private int enumAll = 0; // 全データを列挙するカウンタ
  private int enumOne = 0; // あるリンクグループの処方を列挙するカウンタ
  /**
   * FormulaLinkM コンストラクター・コメント。
   */
  public FormulaLinkM(QueryManager qm) {
    super();
    this.qm = qm;
    startPrepare();
  }
  /**
   * トランザクションの一部としてリンクを追加する。コミットは行わない。ロールバックは行う
   * このメソッドはバックグランドのスレッドで実行する必要がある
   * dbのテーブルを更新し、内部に保持しているデータにitemを挿入する
   * itemにはlinkID, pcode, nameの全てを含む
   *
   * itemのlinkIDがnullになっている場合、新規リンクグループの作成と見なされる。この場合
   * linkIDには使用されていない最小の値がセットされて挿入が行われる
   *
   * 既にdbに同じpcodeが存在している場合は挿入を行わず、エラーになる。
   *
   * 挿入が成功した場合は、clientへの戻り値は挿入されたitemになる
   * @param client dap.IQueryClient
   * @param mode int
   * @param item java.lang.Object[]
   */
  public boolean chainInsert(int tID, Object[] item) {
    if (item[0] == null) {
      int newID = getMinAvailableID();
      if (newID < 0) return false;
      item[0] = new Integer(newID);
    }
    try {
      Object[] p = new Object[1];
      p[0] = item[1];
      Vector r = getOne.queryAndWait(p);
      if (r.size() > 0) return false;
    } catch (SQLException e) {
      return false;
    }
    try {
      Object[] p = new Object[2];
      p[0] = item[0];
      p[1] = item[1];
      setLink.updateAndWait(p, tID);
      int l0 = ((Integer)item[0]).intValue();
      int i=0;
      for (int n=data.size(); i < n; i++) {
	int l1 = ((Integer)SQLutil.get(data, i, 0)).intValue();
	if (l1 == l0) break;
      }
      data.insertElementAt(item, i);
    } catch (SQLException e) {
      try {
	qm.rollbackAndWait(tID);
      } catch (SQLException ee) {}
      return false;
    }
    return true;
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
   * itemで指定されるデータをdbと内部データから削除する。
   * @param client dap.IQueryClient
   * @param mode int
   * @param item java.lang.Object[]
   */
  public boolean delete(final IQueryClient client, final int mode, final Object[] item) {
    if (!chkReady()) return false;
    Runnable deleter = new Runnable() {
      public void run() {
	int tID = 0;
	try {
	  Object[] p = new Object[1];
	  p[0] = item[1]; // pcode
	  tID = qm.getTransactionID();
	  delLink.updateAndWait(p, tID);
	  qm.commitAndWait(tID);
	  data.removeElement(item);
	  client.queryCallBack(data, mode);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  client.queryCallBack(e, SQLERROR);
	}
      }
    };
    Thread t = new Thread(deleter);
    t.start();
    return true;
  }
  /**
   * linkIDで指定されるリンクグループをdbと内部データから削除する。
   * @param client dap.IQueryClient
   * @param mode int
   * @param int linkID
   */
  public boolean delGroup(final IQueryClient client, final int mode, final int linkID) {
    if (!chkReady()) return false;
    Runnable deleter = new Runnable() {
      public void run() {
	int tID = 0;
	try {
	  Object[] p = new Object[1];
	  p[0] = new Integer(linkID);
	  tID = qm.getTransactionID();
	  delLinkGrp.updateAndWait(p, tID);
	  qm.commitAndWait(tID);
	  for (int i=data.size()-1; i >= 0; i--) {
	    int id = ((Integer)((Object[])data.elementAt(i))[0]).intValue();
	    if (id == linkID) data.removeElementAt(i);
	  }
	  client.queryCallBack(data, mode);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  client.queryCallBack(e, SQLERROR);
	}
      }
    };
    Thread t = new Thread(deleter);
    t.start();
    return true;
  }
  /**
   * 全てのデータを列挙するEnumerationを返す
   * @return java.util.Enumeration
   */
  public Enumeration getAllEnum() {
    enumAll = 0;
    return new Enumeration() {
      public boolean hasMoreElements() {
	return (enumAll >= data.size()) ? false : true;
      }
      public Object nextElement() {
	return data.elementAt(enumAll++);
      }
    };
  }
  /**
   * indexで指定されたデータを返す
   * @return java.lang.Object[]
   * @param index int
   */
  public Object[] getData(int index) {
    return SQLutil.getRow(data, index);
  }
  /**
   * 与えられた製造コードを含むリンクグループに含まれる処方を返す。
   * 戻り値の各要素はObject[]で、順にlinkID, pcode, series, nameを含む
   * SQLエラーが発生した場合はnullを返す。与えられたコードがリンクしていない場合は
   * 要素数が0のVectorを返す
   * @return java.util.Vector
   * @param pcode int
   */
  public Vector getLinkedFormula(int pcode) {
    Object[] p = new Object[1];
    p[0] = new Integer(pcode);
    try {
      return getGrp.queryAndWait(p);
    } catch (SQLException e) {}
    return null;
  }
  /**
   * 使用されていないリンクIDの中で最小のものを返す。
   * リンクテーブルにデータが存在しなかった場合、戻り値は1になる
   * まだデータがロードされていなかった場合、戻り値は-1になる
   * @return int
   */
  private int getMinAvailableID() {
    if (!loaded) return -1;
    int n = data.size();
    if (n == 0) return 1;

    int[] linkID = new int[n];
    int k = 0;
    linkID[0] = ((Integer)SQLutil.get(data, 0, 0)).intValue();
    for (int i=1; i < n; i++) {
      int d = ((Integer)(((Object[])data.elementAt(i))[0])).intValue();
      if (d != linkID[k]) linkID[++k] = d;
    }
    // bubble sort
    for (;;) {
      boolean exchanged = false;
      for (int i=0; i < k; i++) { // k = linkID.length-1
	if (linkID[i] > linkID[i+1]) {
	  int t = linkID[i];
	  linkID[i] = linkID[i+1];
	  linkID[i+1] = t;
	  exchanged = true;
	}
      }
      if (!exchanged) break;
    }
			
    int id0 = linkID[0];
    if (id0 > 1) return 1;
    int idi0 = id0;
    for (int i=1; i <= k; i++) { // k = linkID.length-1
      int idi1 = linkID[i];
      if (idi1 - idi0 > 1) return idi0 + 1;
      idi0 = idi1;
    }
    return linkID[k] + 1;
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getOne.getReady();
    int i2 = setLink.getReady();
    int i3 = delLink.getReady();
    int i4 = code2name.getReady();
    int i5 = delLinkGrp.getReady();
    int i6 = getGrp.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0 || i4 < 0 || i5 < 0 || i6 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0 && i4 > 0 && i5 > 0 && i6 > 0) ready = 1;
    return ready;
  }
  /**
   * リンクを追加する。dbのテーブルを更新し、内部に保持しているデータにitemを挿入する
   * itemにはlinkID, pcode, nameの全てを含む
   *
   * itemのlinkIDがnullになっている場合、新規リンクグループの作成と見なされる。この場合
   * linkIDには使用されていない最小の値がセットされて挿入が行われる
   *
   * 既にdbに同じpcodeが存在している場合は挿入を行わず、エラーになる。この場合clientの
   * queryCallBackに渡される値は(null, SQLERROR)になる。SQLExceptionが発生した場合は
   * (SQLException e, SQLERROR)になる
   *
   * 挿入が成功した場合は、clientへの戻り値は挿入されたitemになる
   * @param client dap.IQueryClient
   * @param mode int
   * @param item java.lang.Object[]
   */
  public boolean insert(final IQueryClient client, final int mode, final Object[] item) {
    if (!chkReady()) return false;
    if (item[0] == null) {
      int newID = getMinAvailableID();
      if (newID < 0) return false;
      item[0] = new Integer(newID);
    }
    Runnable inserter = new Runnable() {
      public void run() {
	try {
	  Object[] p = new Object[1];
	  p[0] = item[1];
	  Vector r = getOne.queryAndWait(p);
	  if (r.size() > 0) {
	    client.queryCallBack(null, SQLERROR);
	    return;
	  }
	} catch (SQLException e) {
	  client.queryCallBack(e, SQLERROR);
	  return;
	}

	int tID = 0;
	try {
	  Object[] p = new Object[2];
	  p[0] = item[0];
	  p[1] = item[1];
	  tID = qm.getTransactionID();
	  setLink.updateAndWait(p, tID);
	  qm.commitAndWait(tID);
	  int l0 = ((Integer)item[0]).intValue();
	  int i=0;
	  for (int n=data.size(); i < n; i++) {
	    int l1 = ((Integer)SQLutil.get(data, i, 0)).intValue();
	    if (l1 == l0) break;
	  }
	  data.insertElementAt(item, i);
	  client.queryCallBack(item, mode);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  client.queryCallBack(e, SQLERROR);
	}
      }
    };
    Thread t = new Thread(inserter);
    t.start();
    return true;
  }
  /**
   * 与えられたコードが既にリンクテーブルに存在するならtrueを返す
   * @return boolean
   * @param pcode int
   */
  public boolean isExist(int pcode) {
    for (int i=0, n=data.size(); i < n; i++) {
      Object[] o = SQLutil.getRow(data, i);
      if (((Integer)o[1]).intValue() == pcode) return true;
    }
    return false;
  }
  /**
   * データに整合性があればtrueを返す
   * @return boolean
   */
  public boolean isValid() {
    return (loaded) ? true : false;
  }
  /**
   * データを処方リンクテーブルから読みだす
   */
  public boolean load(final IQueryClient client, final int mode) {
    if (!chkReady()) return false;
    Runnable loader = new Runnable() {
      public void run() {
	Vector data0 = null;
	Vector order = null;
	try {
	  loaded = false;
	  order = getOrder.queryAndWait();
	  data0 = getAll.queryAndWait();
	  data = reorder(data0, order);
	  loaded = true;
	} catch (SQLException e) {
	  data = null;
	  client.queryCallBack(e, SQLERROR);
	  return;
	}
	client.queryCallBack(data, mode);
      }
    };
    Thread t = new Thread(loader);
    t.setPriority(Thread.NORM_PRIORITY);
    t.start();
    return true;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
  }
  /**
   * 読み出したリンクテーブルのデータを、orderで指定されるリンクグループ順に並べ替える
   * @return java.util.Vector
   * @param data java.util.Vector
   * @param order java.util.Vector
   */
  private Vector reorder(Vector data, Vector order) {
    int s = data.size();
    Vector r = new Vector(s);
    for (int i=0, n=order.size(); i < n; i++) {
      Integer linkID = (Integer)SQLutil.get(order, i, 0);
      int begin = -1;
      int end = -1;
      for (int j=0; j < s; j++) {
	Integer dl = (Integer)SQLutil.get(data, j, 0);
	if (dl.equals(linkID) && begin < 0) {
	  begin = j;
	  continue;
	}
	if (!dl.equals(linkID) && begin >= 0) {
	  end = j;
	  break;
	}
      }
      if (end < 0) end = s;

      for (int j=begin; j < end; j++) {
	r.addElement(data.elementAt(j));
      }
    }
    return r;
  }
  /**
   * 製造コードから名前を取り出すサービスメソッド
   * このメソッドはリンク追加ダイアログからの照会を受付けるために用意されている
   * OOPの原則に反する代物
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   */
  public void searchName(IQueryClient client, int mode, int pcode) {
    if (!chkReady()) return;
    Object[] p = new Object[1];
    p[0] = new Integer(pcode);
    code2name.query(client, mode, p);
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (getAll == null) {
      getAll = new Query
	(qm, "select linkID, f.pcode, series, name from flink f, pcode p " +
	 "where f.pcode = p.pcode order by linkID, f.pcode");
    }
	
    if (getOrder == null) {
      getOrder = new Query
	(qm, "select linkID from flink group by linkID order by min(pcode)");
    }
	
    if (code2name == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      code2name = new PQuery(qm,
			     "select series, name from pcode where pcode = ?", INtypes);
      code2name.prepare();
    }

    if (getOne == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getOne = new PQuery(qm, 
			  "select linkID from flink where pcode = ? order by linkID", INtypes);
      getOne.prepare();
    }

    if (getGrp == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getGrp = new PQuery(qm, 
			  "select linkID, f.pcode, series, name from flink f, pcode p " +
			  "where linkID in (select linkID from flink where pcode = ?) " +
			  "and f.pcode = p.pcode", INtypes);
      getGrp.prepare();
    }

    if (setLink == null) {
      int[] INtypes = new int[2];
      INtypes[0] = INT;
      INtypes[1] = INT;
      setLink = new PUpdate(qm, "insert into flink values(?, ?)", INtypes);
      setLink.prepare();
    }

    if (delLink == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delLink = new PUpdate(qm, "delete from flink where pcode = ?", INtypes);
      delLink.prepare();
    }

    if (delLinkGrp == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delLinkGrp = new PUpdate(qm, "delete from flink where linkID = ?", INtypes);
      delLinkGrp.prepare();
    }
  }
}
