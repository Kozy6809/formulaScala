package formula;

import dap.*;
import formula.ui.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * 履歴リストを表示する
 */
public class HistoryListC implements IConsts {
  private QueryManager qm;
  private FBrowseViewC fbvc; // 親ブラウザ
  private int pcode;
  private String series;
  private String name;
  private String title;
  private HistoryListView hlv = null;
  private Hashtable browsersHash = new Hashtable(); // 表示している処方ブラウザのリスト
  private static PQuery getArc2 = null;
  private static int ready = 0;
  private Vector history = null; // クエリーの結果
  private class HistoryTableModel extends AbstractTableModel {
    public int getColumnCount() {return 3;}
    public int getRowCount() {return (history == null) ? 0 : history.size();}
    public Object getValueAt(int row, int col) {
      Object o = SQLutil.get(history, row, col);
      if (col == 0) {
	return DateFormat.getDateInstance().format((Date)o);
      } else {
	return o;
      }
    }
  };
  HistoryTableModel htm = new HistoryTableModel();
  /**
   * HistoryListC コンストラクター・コメント。
   */
  public HistoryListC(QueryManager qm, FBrowseViewC fbvc, int pcode, String series, String name) {
    super();
    this.qm = qm;
    this.fbvc = fbvc;
    this.pcode = pcode;
    this.series = series;
    this.name = name;
    this.title = String.valueOf(pcode) + " " + series + " " + name;
    hlv = new HistoryListView(this, title);
    startPrepare();
    hlv.getTable().setModel(htm);
    load();
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
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getArc2.getReady();

    if (i1 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0) ready = 1;
    return ready;
  }
  /**
   * データをロードし、履歴リストを表示する
   */
  private void load() {
    if (!chkReady()) return;
    getArc2.query(new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) return;
	history = (Vector)o;
	SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    JTable t = hlv.getTable();
	    int h = t.getRowHeight() * history.size();
	    t.setPreferredScrollableViewportSize(new Dimension(512, h));
	    hlv.pack();
	    hlv.setVisible(true);
	  }
	});
      }
    }, 0, new Object[] {new Integer(pcode)});
  }
  /**
   * 既に履歴リストが表示されていれば再表示を行う
   */
  public void show() {
    hlv.setVisible(true);
  }
  /**
   * 指定された行の旧処方ブラウザを表示させる
   * @param index int[]
   */
  public void showBrowsers(int[] index) {
    for (int i=0; i < index.length; i++) {
      Date date = (Date)SQLutil.get(history, index[i], 0);
      if (browsersHash.containsKey(date)) {
	((FBrowseViewC)browsersHash.get(date)).show();
      } else {
	FBrowseViewC fbvc = new FBrowseViewC(qm, pcode, date, series, name);
	browsersHash.put(date, fbvc);
      }
    }
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (getArc2 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getArc2 = new PQuery
	(qm, "select date, person, reason from arc2 where pcode = ? order by date", INtypes);
      getArc2.prepare();
    }
  }
}
