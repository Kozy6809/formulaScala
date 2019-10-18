package formula;

import java.util.*;
import java.sql.SQLException;
import javax.swing.*;
import dap.*;
import formula.ui.*;
/**
 * 新原料の登録
 */
public class NewMatC implements IConsts {
  private MainViewC mvc;
  private QueryManager qm;
  private static PQuery chkMcode = null;
  private static PUpdate setMcode = null;
  private static PUpdate delMcode = null;
  private static int ready = 0;
  private NewMatView nmv = null;
  private int mcode = 0;
  private String mname = null;
  private double price = 0.0;
  private boolean onProcess = false;

  private class Updater implements Runnable {
    public void run() {
      try {
	Vector r = chkMcode.queryAndWait(new Object[]{new Integer(mcode)});
	if (r.size() > 0) {
	  double rgdPrice = ((Double)SQLutil.get(r, 0, 3)).doubleValue();
	  if (price == 0.0 || price == rgdPrice) {
	    showAlreadyExistDlg();
	  } else setPrice((Object[])r.elementAt(0));
	  onProcess = false;
	  return;
	}
      } catch (SQLException e) {
	showSQLErrorDlg();
	onProcess = false;
	return;
      }
      int tID = 0;
      try {
	tID = qm.getTransactionID();
	Object[] data = new Object[] {
	  new Integer(mcode), mname, new Integer(0), new Double(price), new Date()
	};
	int rc = setMcode.updateAndWait(data, tID);
	qm.commitAndWait(tID);
	showSuccessDlg();
      } catch (SQLException e) {
	try {
	  qm.rollbackAndWait(tID);
	} catch (SQLException ee) {}
	showSQLErrorDlg();
      }
      onProcess = false;
    }
    private void setPrice(Object[] rowData) {
      // rowDataの並びはmcode, mname, status, price, date
      int tID = 0;
      try {
	tID = qm.getTransactionID();
	Object[] data = new Object[] {new Integer(mcode)};
	int rc = delMcode.updateAndWait(data, tID);
	data = new Object[5];
	data[0] = new Integer(((Integer)rowData[0]).intValue());
	data[1] = new String((String)rowData[1]);
	data[2] = new Integer(((Integer)rowData[2]).intValue());
	data[3] = new Double(price);
	data[4] = new Date();	
	rc = setMcode.updateAndWait(data, tID);
	qm.commitAndWait(tID);
	showPriceUpdateDlg(mcode, mname);
      } catch (SQLException e) {
	try {
	  qm.rollbackAndWait(tID);
	} catch (SQLException ee) {}
	showSQLErrorDlg();
      }
    }
  };
  private Updater updater = new Updater();
  /**
   * NewMatC コンストラクター・コメント。
   */
  public NewMatC(QueryManager qm, MainViewC mvc) {
    super();
    this.qm = qm;
    startPrepare();
    this.mvc = mvc;
    nmv = new NewMatView(this);
    nmv.pack();
    nmv.setVisible(true);
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
   */
  public void close() {
    nmv.setVisible(false);
  }
  /**
   * prepareが全て終了しているかチェック。
   * @return int
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = chkMcode.getReady();
    int i2 = setMcode.getReady();

    if (i1 < 0 || i2 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0) ready = 1;
    return ready;
  }
  /**
   */
  public void show() {
    nmv.setVisible(true);
  }
  /**
   */
  private void showAlreadyExistDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(nmv,
				      "既に同じコードの資材が存在します　\n" +
				      "もう一度確認してください　",
				      "資材コードが重複しています",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   */
  private void showPriceUpdateDlg(final int mcode, final String mname) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(nmv,
				      mcode + " " + mname + "の単価が更新されました　",
				      "単価更新",
				      JOptionPane.INFORMATION_MESSAGE);
      }
    });
  }
  /**
   */
  private void showSQLErrorDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(nmv,
				      "データベースに書き込めません　\n" +
				      "後でまたやり直してください　",
				      "データベースがエラーを返しました",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   */
  private void showSuccessDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(nmv,
				      "新規原料が登録されました　",
				      "登録完了",
				      JOptionPane.INFORMATION_MESSAGE);
      }
    });
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (chkMcode == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      chkMcode = new PQuery
	(qm, "select mcode, mname, status, price, date from mcode where mcode = ?", INtypes);
      chkMcode.prepare();
    }
	
    if (setMcode == null) {
      int[] INtypes = new int[5];
      INtypes[0] = INT;
      INtypes[1] = STRING;
      INtypes[2] = SHORT;
      INtypes[3] = FLOAT;
      INtypes[4] = TIMESTAMP;
      setMcode = new PUpdate
	(qm, "insert into mcode values(?, ?, ?, ?, ?)", INtypes);
      setMcode.prepare();
    }

    if (delMcode == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delMcode = new PUpdate(qm, "delete from mcode where mcode = ?", INtypes);
      delMcode.prepare();
    }
  }
  /**
   * 新原料を登録する
   */
  public void update() {
    if (!chkReady()) return;
    if (onProcess) return;
    mcode = nmv.getMcode();
    mname = nmv.getMname();
    price = nmv.getPrice();
    if (mname.length() == 0 || mcode == 0) {
      JOptionPane.showMessageDialog(nmv,
				    "資材コードか資材記号が不適切な値になっています　",
				    "値が不正です",
				    JOptionPane.WARNING_MESSAGE);
      return;
    }
    onProcess = true;
    Thread t = new Thread(updater);
    t.start();
  }
}
