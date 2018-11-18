package formula;

import dap.*;
import myutil.*;
import formula.ui.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
/**
 * 毒性レポートのコントローラ
 */
public class PoisonListC implements IConsts, IQueryClient {
  private QueryManager qm;
  private FBrowseViewC fvc;
  private int pcode;
  private String title; // series + name
  private PoisonListM plm = null;
  private PoisonListV plv = null;
  private boolean deferredView = false; // 指示されるまでビューを表示しない。
  private Vector resolved = null; // FBrowseViewCから取得する分解処方。
  // 更新中処方の毒性計算に用いる

  private class PoisonTableModel extends AbstractTableModel {
    public int getRowCount() {
      return plm.getPoisonCount();
    }
    public int getColumnCount() {return 4;} // fixed
    public Object getValueAt(int row, int column) {
      return plm.getPoisonValueAt(row, column);
    }
  }
  private PoisonTableModel ptm = new PoisonTableModel();
	
  private class ContrbTableModel extends AbstractTableModel {
    public int getRowCount() {
      return plm.getContrbCount();
    }
    public int getColumnCount() {return 4;} // fixed
    public Object getValueAt(int row, int column) {
      return plm.getContrbValueAt(row, column);
    }
  }
  private ContrbTableModel ctm = new ContrbTableModel();
  /**
   * PoisonListC コンストラクター・コメント。
   */
  public PoisonListC(QueryManager qm, FBrowseViewC fvc, int pcode, String title) {
    this(qm, fvc, pcode, title, false);
  }
  /**
   * PoisonListC コンストラクター・コメント。
   */
  public PoisonListC
    (QueryManager qm, FBrowseViewC fvc, int pcode, String title, boolean deferredView) {
    this.qm = qm;
    this.fvc = fvc;
    this.pcode = pcode;
    this.title = title;
    this.deferredView = deferredView;
    plm = new PoisonListM(qm);
    plv = new PoisonListV(this, plm, title);
    plm.load(this, 0, pcode);
  }
  /**
   * 毒性データをクリップボードへコピーする。毒性表と寄与率表が一緒にコピーされる
   */
  public void copyToClip() {
    StringBuffer sb = new StringBuffer();
    sb.append("毒性リスト\n");
    for (int i=0, n=plm.getPoisonCount(); i < n; i++) {
      sb.append(pcode);
      for (int j=0; j < 4; j++) {
	sb.append("\t");
	Object o = plm.getPoisonValueAt(i, j);
	if (o instanceof Double) o = ExpFormat.format(((Double)o).doubleValue(), 3);
	sb.append(o);
      }
      sb.append("\n");
    }
	
    sb.append("\n各原料の寄与\n");
	
    for (int i=0, n=plm.getContrbCount(); i < n; i++) {
      sb.append(pcode);
      for (int j=0; j < 4; j++) {
	sb.append("\t");
	Object o = plm.getContrbValueAt(i, j);
	if (o instanceof Double) o = ExpFormat.format(((Double)o).doubleValue(), 3);
	sb.append(o);
      }
      sb.append("\n");
    }
    Clipboard cb = plv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(sb.toString());
    cb.setContents(ss, ss);
  }
  /**
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) return;
    fvc.setPoison(plm.isPoison());
    if (plv.isVisible() || !deferredView) show();
  }
  /**
   * 毒性レポートを表示する
   */
  public void show() {
    if (plv.isVisible()) {
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  ptm.fireTableDataChanged();
	  ctm.fireTableDataChanged();
	}
      });
    } else {
      plv.setModel(ptm, ctm);
      plv.pack();
      plv.setVisible(true);
    }
  }
  /**
   * 毒性の再計算を実行する。FBrowseViewCから分解処方を取得し、それに基づいて求める
   */
  public void update() {
    class Updater implements Runnable, IQueryClient, dap.IConsts {
      public void run() {
	fvc.getDecomp(this, 0);
      }
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) return;
	Vector resolved = (Vector)o;
	plm.update(PoisonListC.this, 0, resolved);
      }
    }
    Thread t = new Thread(new Updater());
    t.start();
  }
}
