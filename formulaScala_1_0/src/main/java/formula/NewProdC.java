package formula.java;

import java.util.*;
import java.sql.SQLException;
import javax.swing.*;
import dap.*;
import formula.ui.*;
/**
 * 新製品を登録する
 */
public class NewProdC implements IConsts {
  private QueryManager qm;
  private static int ready = 0;
  private static PQuery chkPcode = null;
  private static PUpdate setPcode = null;
  private static PUpdate setForm2 = null;
  private MainViewC mvc;
  private Series series;
  private Vector seriesData = null;
  private String selectedSeries = null;
  private int pcode = 0;
  private String name = null;
  private boolean onProcess = false;
  private NewProdView npv = new NewProdView(this);
  private NewSeriesDialog nsd = new NewSeriesDialog(npv);
	
  private class SeriesListModel extends AbstractListModel<Object> {
    public Object getElementAt(int index) {
      if (seriesData == null) return null;
      return SQLutil.get(seriesData, index, 0);
    }
    public int getSize() {
      return (seriesData == null) ? 0 : seriesData.size();
    }
    public void loaded() {
      super.fireContentsChanged(this, 0, seriesData.size()-1);
    }
  };
  private SeriesListModel slm = new SeriesListModel();

  private class Updater implements Runnable {
    public void run() {
      try {
	Vector r = chkPcode.queryAndWait(new Object[]{new Integer(pcode)});
	if (r.size() > 0) {
	  showAlreadyExistDlg();
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
	  new Integer(0), new Integer(pcode), selectedSeries, name
	};
	setPcode.updateAndWait(data, tID);

	Date d = new Date();
	data = new Object[] {
	  new Integer(pcode), d, new Double(0.0), "unknown", "", "unknown"
	};
	setForm2.updateAndWait(data, tID);

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
  };
  private Updater updater = new Updater();
  /**
   * NewProdC コンストラクター・コメント。
   */
  public NewProdC(QueryManager qm, MainViewC mvc, Series series) {
    super();
    this.qm = qm;
    this.mvc = mvc;
    this.series = series;
    init();
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
    npv.setVisible(false);
    series.load(); // シリーズ名リストをアップデート
  }
  /**
   * prepareが全て終了しているかチェック。
   * @return int
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = chkPcode.getReady();
    int i2 = setForm2.getReady();

    if (i1 < 0 || i2 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0) ready = 1;
    return ready;
  }
  /**
   */
  private void init() {
    startPrepare();
    seriesData = series.getResult();
    npv.getSeries().setModel(slm);
    npv.pack();
    npv.setVisible(true);
  }
  /**
   */
  public void show() {
    npv.setVisible(true);
  }
  /**
   */
  private void showAlreadyExistDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(npv,
				      "既に同じコードの製品が存在します　\n" +
				      "もう一度確認してください　",
				      "製品コードが重複しています",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   * 新シリーズ名の入力ダイアログを表示する
   */
  public void showNewSeriesDlg() {
    nsd.pack();
    nsd.setLocationRelativeTo(npv);
    nsd.setVisible(true);
    npv.setSelectedSeries(nsd.getNewSeries());
  }
  /**
   */
  private void showSQLErrorDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(npv,
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
	JOptionPane.showMessageDialog(npv,
				      "新製品が登録されました　",
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
    if (chkPcode == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      chkPcode = new PQuery
	(qm, "select pcode from pcode where pcode = ?", INtypes);
      chkPcode.prepare();
    }
	
    if (setPcode == null) {
      int[] INtypes = new int[4];
      INtypes[0] = SHORT;		// obsolete
      INtypes[1] = INT;		// pcode
      INtypes[2] = STRING;	// series
      INtypes[3] = STRING;	// name
      setPcode = new PUpdate
	(qm, "insert into pcode values(?, ?, ?, ?)", INtypes);
      setPcode.prepare();
    }

    if (setForm2 == null) {
      int[] INtypes = new int[6];
      INtypes[0] = INT;		// pcode
      INtypes[1] = TIMESTAMP;	// date
      INtypes[2] = FLOAT;		// sg
      INtypes[3] = STRING;	// person
      INtypes[4] = STRING;	// comment
      INtypes[5] = STRING;	// reason
      setForm2 = new PUpdate
	(qm, "insert into form2 values(?, ?, ?, ?, ?, ?, null)", INtypes);
      setForm2.prepare();
    }
  }
  /**
   * 新製品を登録する
   */
  public void update() {
    if (!chkReady()) return;
    if (onProcess) return;
    pcode = npv.getPcode();
    name = npv.getName();
    selectedSeries = npv.getSelectedSeries();
    if (selectedSeries.length() == 0 || name.length() == 0 || pcode == 0) {
      JOptionPane.showMessageDialog(npv,
				    "シリーズ名、コードまたは製品名が不適切な値になっています　",
				    "値が不正です",
				    JOptionPane.WARNING_MESSAGE);
      return;
    }
    Thread t = new Thread(updater);
    onProcess = true;
    t.start();
  }
}
