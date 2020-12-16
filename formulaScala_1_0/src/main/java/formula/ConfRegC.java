package formula;

import java.text.*;
import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;
import dap.*;
import formula.ui.*;
/**
 * 業務部での処方登録確認のコントローラ
 */
public class ConfRegC implements IConsts, IQueryClient {
  private MainViewC mvc;
  private QueryManager qm;
  private ConfRegM cr =null;
  private ConfRegV crv = null;
  private Vector checks = null; // 登録のチェックマークの値の並び
  private boolean showAnyway = false; // リストが空でも表示するかどうか

  private class CRTableModel extends AbstractTableModel {
    public int getRowCount() {
      return cr.getSize();
    }
    public int getColumnCount() {return 4;} // チェック、登録日、品名、登録者
    public Object getValueAt(int row, int column) {
      switch (column) {
      case 0:
	return checks.elementAt(row);
      case 1:
	return DateFormat.getDateInstance().format((Date)cr.getData(row, 0));
      case 2:
	return (Integer)cr.getData(row, 1) + " " + (String)cr.getData(row, 2) + " " + (String)cr.getData(row, 3);
      case 3:
	String person = (String)cr.getData(row, 4);
	return (person == null) ? "" : person; // nullを返すとgetColumnClass()が困る
      default:
	return null;
      }
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex != 0) return;
      checks.setElementAt(aValue, rowIndex);
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return (columnIndex == 0) ? true : false;
    }
    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

  };
	
  private CRTableModel crtm = new CRTableModel();
  /**
   * ConfRegC コンストラクター・コメント。
   */
  public ConfRegC(QueryManager qm, MainViewC mvc) {
    this.qm = qm;
    this.mvc = mvc;
    cr = new ConfRegM(qm);
    cr.load(this, 1);
  }
  /**
   * TableModel中のチェックマークを全部falseにする
   */
  public void clearCheck() {
    for (int i=0, n=crtm.getRowCount(); i < n; i++) {
      crtm.setValueAt(new Boolean(false), i, 0);
    }
  }
  /**
   * データをクリップボードにコピーする。登録日、製造コード、品種、品名、登録者
   * 
   */
  public void copyToClip() {
    DateFormat df = DateFormat.getDateInstance();
    StringBuffer sb = new StringBuffer();
    for (int i=0, n=cr.getSize(); i < n; i++) {
      sb.append(df.format((Date)cr.getData(i, 0)));
      for (int j=1; j < 5; j++) {
	sb.append("\t");
	Object o = cr.getData(i, j);
	sb.append((o == null) ? "" : o);
      }
      sb.append("\n");
    }
    Clipboard cb = crv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(sb.toString());
    cb.setContents(ss, ss);
  }
  /**
   * データをロードし、表示する
   */
  public void load() {
    cr.load(this, 1);
  }
  /**
   * 1で未確認製品の取り出し(load)、2で未確認から確認への更新(update)
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      return;
    }
    switch (mode) {
    case 1:
      showCRV();
      break;
    case 2:
      Runnable r = new Runnable() {
	public void run() {
	  cr.load(ConfRegC.this, 1);
	}
      };
      Thread t = new Thread(r);
      t.start();
      break;
    default :
    }
  }
  /**
   * 必要なデータをセットし、ビューを表示する。このメソッドはqueryCallBackから呼ばれる
   * 初めて呼ばれた時のみ、リストが空ならばビューを表示しない
   */
  private void showCRV() {
    int n = cr.getSize();
    checks = new Vector(n);
    for (int i=0; i < n; i++) {
      checks.addElement(new Boolean(false));
    }
    if (!showAnyway) {
      showAnyway = true;
      if (cr.getSize() == 0) return;
    }
    if (crv == null) {
      crv = new ConfRegV(this, crtm);
      crv.pack();
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	crtm.fireTableDataChanged();
	crv.setVisible(true);
      }
    });
  }
  /**
   * 指定された製品を業務部での登録済みに変更する
   */
  public void update() {
    Vector pcodes = new Vector();
    for (int i=0, n=checks.size(); i < n; i++) {
      if (((Boolean)checks.elementAt(i)).booleanValue()) {
	pcodes.addElement(cr.getData(i, 1));
      }
    }
    if (pcodes.size() == 0) return;
    cr.update(this, 2, pcodes);
  }
}
