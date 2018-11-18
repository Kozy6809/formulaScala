package formula;

import dap.*;
import formula.ui.*;
import myutil.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
/**
 * FBrowseViewのコントローラ
 */
public class FBrowseViewC implements IConsts, IWinControl {
  private QueryManager qm;
  private MatDeterminer md = null;
  private HistoryListC hlc = null;
  private FormulaLinkC flc = null;
  private FBrowseViewC fvc = null;
  private PoisonListC plc = null;
  private int pcode;
  private String series;
  private String name;
  private String title;
  private IFormulaModel fm = null;
  private FBrowseView fbv = null;
  private boolean editing = false;
  private boolean showDecomp = false;
  private static Authenticator auth = null;
  private String authPerson = null;
  // 処方リンクの追加によって処方が更新された場合は、連絡書の印刷を強制し、印刷が
  // 終わらない限りウィンドウを閉じられないようにする。下はそのためのフラグ
  private boolean unprinted = false;
  private boolean deferredView = false; // 指示されるまでビューを表示しない。
  // デフォルトではこのオブジェクトが構築されると自動的にビューが表示される。

  private class DecompTableModel extends AbstractTableModel {
    public int getRowCount() {
      return fm.getDecompDataSize();
    }
    public int getColumnCount() {return 3;} // fixed
    public Object getValueAt(int row, int column) {
      return fm.getDecompValueAt(row, column);
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {return false;}
  }
  private class NormTableModel extends AbstractTableModel implements MatDeterminListener {
    private int row;
    public int getRowCount() {
      return fm.getNormDataSize();
    }
    public int getColumnCount() {return 3;} // fixed
    public Object getValueAt(int row, int column) {
      return fm.getValueAt(row, column);
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      row = rowIndex;
      switch (columnIndex) {
      case 0:
	md.searchByCode(this, ((Integer)aValue).intValue());
	break;
      case 1:
	md.searchByName(this, (String)aValue);
	break;
      case 2:
	fm.setValueAt(aValue, rowIndex, columnIndex);
	fbv.getTotal().setText(fm.getTotalText());
	break;
      default:
      }
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return (editing) ? true : false;
    }
    public void matDetermined(boolean canceled, int code, String name, int status) {
      if (canceled) return;
      if (code < 0) {
	fm.setValueAt(new Integer(0), row, 0);
	fm.setValueAt("該当なし", row, 1);
	fm.setValueAt(new Integer(0), row, 3);
      } else {
	fm.setValueAt(new Integer(code), row, 0);
	fm.setValueAt(name, row, 1);
	fm.setValueAt(new Integer(status), row, 3);
      }
      fireTableRowsUpdated(row, row);
    }
  }
  private DecompTableModel dtm = new DecompTableModel();
  private NormTableModel ntm = new NormTableModel();
  private class NormLoader implements IQueryClient {
    public void queryCallBack(Object o, int mode) {
      if (mode == SQLERROR) return;
      startView();
    }
  };
  private NormLoader normLoader = new NormLoader();
  /**
   * 与えられたパラメータからFormulaModelを生成するコンストラクタ。デフォルトでビューを表示
   */
  public FBrowseViewC(QueryManager qm, int pcode, Date date, String series, String name) {
    this(qm, pcode, date, series, name, false);
  }
  /**
   * 与えられたパラメータからFormulaModelを生成するコンストラクタ
   */
  public FBrowseViewC(QueryManager qm, int pcode, Date date, String series, String name, boolean deferredView) {
    super();
    Main.addWin(this);
    this.qm = qm;
    this.pcode = pcode;
    this.series = series;
    this.name = name;
    this.deferredView = deferredView;
    if (date == null) {
      fm = new FormulaModel(qm);
    } else {
      fm = new ArcModel(qm);
    }
    initView();
    fm.load(normLoader, 1, pcode, date);
  }
  /**
   * あらかじめ用意されたFormulaModelを使うコンストラクタ。リンク処方のコピー用
   * @param qm dap.QueryManager
   * @param fm formula.AbstractFormulaModel
   */
  public FBrowseViewC(QueryManager qm, IFormulaModel fm, int pcode, String series, String name) {
    super();
    Main.addWin(this);
    this.qm = qm;
    this.fm = fm;
    this.pcode = pcode;
    fm.setPcode(pcode);
    this.series = series;
    this.name = name;
    deferredView = true;
    initView();
    startView();
  }
  /**
   * 処方の更新権限を設定する
   */
  public void authorize() {
    if (!fm.isEditable()) return;
    Authenticator at = getAuth();
    if (at.authorize(fbv) == false) return;
    authPerson = at.getResult();
    if (authPerson == null) return;
    startEdit();
  }
  /**
   * データをクリップボードにコピーする
   */
  public void copyToClip(boolean showNorm) {
    if (editing) syncModel(new Date());
    String s = (showNorm) ? fm.getNormForCopy(series, name) : fm.getDecompForCopy(series, name);
    Clipboard cb = fbv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(s);
    cb.setContents(ss, ss);
  }
  /**
   * テーブルから1行削除する
   * @param index int
   */
  public void deleteRow(int index) {
    fm.deleteRow(index);
    fbv.getTotal().setText(fm.getTotalText());
    ntm.fireTableRowsDeleted(index, index);
  }
  /**
   * 指定行とその下の行を交換する
   * @param index int
   */
  public void exchangeRow(int index) {
    if (index >= fm.getNormDataSize()) return;
    fm.exchangeRow(index);
    ntm.fireTableRowsUpdated(index, index+1);
  }
  /**
   * 印刷を強制する。印刷が終わらない限りウィンドウを閉じられないようにする
   */
  public void forceToPrint(boolean b) {
    unprinted = b;
  }
  /**
   * 処方の更新を行うスレッドを起動する
   */
  private void forkUpdater(final Vector followers) {
    Runnable updater = new Runnable() {
      public void run() {
	int tID = qm.getTransactionID();
	FBrowseViewC[] fbs = null;
	if (followers.size() > 1) { // リンクグループに自分以外のメンバーがいる場合
	  fbs = new FBrowseViewC[followers.size() - 1];
	  for (int i=0, j=0; i < followers.size(); i++) {
	    Object[] follower = SQLutil.getRow(followers, i);
	    int pcodef = ((Integer)follower[1]).intValue();
	    String seriesf = (String)follower[2];
	    String namef = (String)follower[3];
	    if (pcodef == pcode) continue;
	    FormulaModel newFm = new FormulaModel(qm);
	    newFm.copyData(fm);
	    fbs[j] = new FBrowseViewC(qm, newFm, pcodef, seriesf, namef);
	    if (!fbs[j].linkFormula(tID)) {
	      showSQLErrDlg();
	      return;
	    }
	    j++;
	  }
	}

	// 自分自身の更新
	if (!fm.chainUpdate(tID)) {
	  showSQLErrDlg();
	  return;
	}
	try {
	  qm.commitAndWait(tID);
	} catch (java.sql.SQLException e) {}
			
	if (fbs != null) {
				// 更新された処方につき、分解処方も更新する
	  for (int i=0; i < fbs.length; i++) {
	    fbs[i].updateResolvf();
	  }
	}
	fm.updateResolvf(); // 自分の分解処方も更新する
	// 更新された処方ブラウザを表示し、印刷を強制する
	if (fbs != null) {
	  final FBrowseViewC[] fbsf = fbs;
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      for (int i=0; i < fbsf.length; i++) {
		//					fbsf[i].forceToPrint(true);
		fbsf[i].show();
	      }
	      JOptionPane.showMessageDialog(fbv,
					    "リンクしている処方が同時に更新されました　\n" +
					    "忘れずに連絡書を発行してください　",
					    "処方リンク",
					    JOptionPane.INFORMATION_MESSAGE);
	    }
	  });
	}
	SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    fbv.setTitle("更新完了 " + title);
	    fbv.setEditable(false);
	  }
	});
	editing = false;
	fm.setEditing(false);
      }
    };
    Thread t = new Thread(updater);
    t.start();
  }
  /**
   * @return formula.Authenticator
   */
  private Authenticator getAuth() {
    if (auth == null) auth = new Authenticator(qm);
    return auth;
  }
  /**
   * 分解処方を返す
   * @param client dap.IQueryClient
   * @param mode int
   */
  public void getDecomp(IQueryClient client, int mode) {
    if (!editing && fm.isDecompDataValid()) {
      client.queryCallBack(fm.getDecompData(), mode);
      return;
    }
    fm.loadDecomp(client, mode, pcode);
  }
  /**
   * @return formula.ui.FBrowseView
   */
  public FBrowseView getFBV() {
    if (fbv == null) {
      initView();
      startView();
    }
    return fbv;
  }
  /**
   * @return formula.AbstractFormulaModel
   */
  public IFormulaModel getFM() {
    return fm;
  }
  /**
   * Viewを初期化する
   */
  private void initView() {
    String showOld = (fm instanceof ArcModel) ? "旧処方 " : "";
    this.title = showOld + String.valueOf(pcode) + "　" + series + "　" + name;
    fbv = new FBrowseView(this, title);
    md = new MatDeterminer(qm, fbv);
    md.setDialogLocator(fbv);
    fbv.getTable().setModel(ntm);
  }
  /**
   * テーブルに1行挿入する
   * @param index int
   */
  public void insertRow(int index) {
    fm.insertRow(index);
    ntm.fireTableRowsInserted(index, index);
  }
  /**
   * @return boolean
   */
  public boolean isEditing() {
    return editing;
  }
  /**
   * 処方がリンクされた時の更新を実行する。別処方のデータをコピーして更新する
   * このメソッドはバックグランドで実行される必要がある
   * 更新に失敗した場合はfalseを返す
   * @param master formula.AbstractFormulaModel
   */
  public boolean linkFormula(int transactionID) {
    boolean rc = fm.chainUpdate(transactionID);
    if (rc) fbv.setVisible(true);
    return rc;
  }
  /**
   * 印刷を実行する
   * @param showNorm boolean
   */
  public void print(boolean showNorm) {
    if (editing) syncModel(new Date());
    PrintCanvas pc = new PrintCanvas();
    if (showNorm) {
      pc.setModeResolv(false);
      pc.setMr(fm.getNormForPrint());
    } else {
      pc.setModeResolv(true);
      pc.setMr(fm.getDecompForPrint());
    }
    pc.setPname(title);
    pc.setComment(fm.getComment());
    pc.setPerson(fm.getPerson());
    pc.setReason(fm.getReason());
    pc.setRegDate(DateFormat.getDateInstance().format(fm.getDate()));
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    pc.setSg(nf.format(fm.getSG()));
    if (!fm.isPriceValid()) {
      pc.setPrice("原料単価不正");
    } else {
      double price = fm.getPrice();
      if (price < 0.0) pc.setPrice("---");
      else {
	nf.setMinimumFractionDigits(3);
	nf.setMaximumFractionDigits(3);
	pc.setPrice(nf.format(price));
      }
    }
		
    PrintPreview pp = new PrintPreview(pc);
    pp.setVisible(true);
    pp.print();
    pp.dispose();
    // 通常処方の印刷が終わったら強制印刷状態を解除する
    if (showNorm) unprinted = false;
  }
  /**
   * 処方の再計算を実行する
   */
  public void recalcPoison() {
    if (plc == null) plc = new PoisonListC(qm, this, pcode, title, true);
    plc.update();
  }
  /**
   */
  public boolean requestClose() {
    // 印刷を強制されているかチェック
    if (unprinted) {
      JOptionPane.showMessageDialog(fbv,
				    "連絡書を発行しないと　\n" +
				    "この処方ウィンドウを閉じることはできません　",
				    "処方が印刷されていません",
				    JOptionPane.WARNING_MESSAGE);
      return false;
    }
	
    if (editing) {
      Object[] o = {"書き込む", "書き込まない", "キャンセル"};
      int n = JOptionPane.showOptionDialog(fbv,
					   "更新中の処方をデータベースに書き込みますか？",
					   "処方が更新されています",
					   JOptionPane.YES_NO_CANCEL_OPTION,
					   JOptionPane.QUESTION_MESSAGE,
					   null,
					   o,
					   o[2]);
      switch (n) {
      case 0:
	update(); // yes
	return false;
      case 1:
	editing = false;
	break; // no
      case 2: // cancel
	return false;
      default:
	return false;
      }
    }
    Main.removeWin(this);
    fbv.dispose();
    return true;
  }
  /**
   * orgで指定された資材コードが処方に含まれているなら、それをaltに変更し、
   * 更新者をauthPerson、理由をreasonにし、編集状態にする
   * @param ae doubl
   */
  public void setMcode(int org, int alt, String authPerson, String reason) {
    for (int i=0, n=fm.getNormDataSize(); i < n; i++) {
      Integer mcode = (Integer)fm.getValueAt(i, 0);
      if (mcode.intValue() == org) {
	fm.setValueAt(new Integer(alt), i, 0);
	this.authPerson = authPerson;
	startEdit();
	fm.setReason(reason); // startEdit()は更新理由をクリアしてしまうので、ここで設定する
      }
    }
  }
  /**
   * 処方ウィンドウに"毒性あり"の表示を行わせるかどうか指定する
   * @param b boolean
   */
  public void setPoison(boolean b) {
    fbv.setPoison(b);
  }
  /**
   * 処方編集時に、製品単価が変わると呼び出されるコールバックメソッド
   * @param price double
   */
  public void setPrice(double price) {
    if (!fm.isPriceValid()) {
      fbv.getPrice().setText("原料単価不正");
      return;
    }
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    fbv.getPrice().setText(nf.format(price));
  }
  /**
   * このメソッドは VisualAge で作成されました。
   */
  public void show() {
    if (fbv != null) fbv.setVisible(true);
    else {
      initView();
      startView();
    }
  }
  /**
   * 分解処方を表示する
   */
  public void showDecomp() {
    if (!editing && fm.isDecompDataValid()) {
      fbv.getTable().setModel(dtm);
    } else {
      fm.loadDecomp(new IQueryClient() {
	public void queryCallBack(Object o, int mode) {
	  if (mode == SQLERROR) return;
	  fbv.getTable().setModel(dtm);
	}
      }, 0, pcode);
    }
  }
  /**
   * 履歴リストを表示する
   */
  public void showHistory() {
    if (hlc != null) hlc.show();
    else hlc = new HistoryListC(qm, this, pcode, series, name);
  }
  /**
   * 処方を更新する際、この処方が別の処方とリンクしていることをユーザーに注意し、
   * 本当に更新するか尋ねる。更新する場合はtrueを返す
   * @return boolean
   */
  private boolean showLinkedDlg() {
    Object[] o = {"更新する", "キャンセル"};
    int n = JOptionPane.showOptionDialog(fbv,
					 "この処方は別製品の処方とリンクしています　\n" +
					 "これを更新するとリンクしている処方も更新されますが　\n" +
					 "更新しますか?　",
					 "リンク処方です",
					 JOptionPane.YES_NO_OPTION,
					 JOptionPane.QUESTION_MESSAGE,
					 null,
					 o,
					 o[1]);
    if (n == JOptionPane.YES_OPTION) return true;
    return false;
  }
  /**
   * 処方のリンクリストを表示する
   */
  public void showLinkList() {
    FormulaLinkC flc = FormulaLinkC.getInstance(qm);
    if (!flc.isReady()) return;
    if (flc.makeVisible(pcode) == false) {
      JOptionPane.showMessageDialog(fbv,
				    "この処方は他の処方とリンクしていません　",
				    "処方リンク",
				    JOptionPane.INFORMATION_MESSAGE);
    }
  }
  /**
   * 通常処方を表示する
   */
  public void showNorm() {
    fbv.getTable().setModel(ntm);
  }
  /**
   */
  public void showPoisonList() {
    if (plc == null) plc = new PoisonListC(qm, this, pcode, title);
    else plc.show();
  }
  /**
   */
  private void showSQLErrDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(fbv,
				      "データベースに書き込めませんでした　\n\n" +
				      "原因を確認の上やりなおしてください　",
				      "更新に失敗しました",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   * 処方の編集を開始する
   */
  private void startEdit() {
    editing = true;
    fm.setFBC(this);
    fm.setEditing(true);
    fm.setReason("");
    fm.setDate(new Date());
    fm.setPerson(authPerson);
    if (fbv == null) return;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	fbv.setTitle("更新中　" + title);
	fbv.setEditable(true);
	fbv.getDate().setText(DateFormat.getDateInstance().format(fm.getDate()));
	fbv.getPerson().setText(fm.getPerson());
	fbv.getReason().setText(fm.getReason());
      }
    });
    showNorm();
    fbv.selectShowNormMenu();
  }
  /**
   * モデルからデータを取得してビューにセット、表示する
   */
  private void startView() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	ntm.fireTableDataChanged();
	JTable t = fbv.getTable();
	int h = t.getRowHeight() * fm.getNormData().size();
	t.setPreferredScrollableViewportSize(new Dimension(256, h));
	fbv.getDate().setText(DateFormat.getDateInstance().format(fm.getDate()));
	double sg = fm.getSG();
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMinimumFractionDigits(3);
	nf.setMaximumFractionDigits(3);
	fbv.getSG().setText(nf.format(sg));
	if (!fm.isPriceValid()) {
	  fbv.getPrice().setText("原料単価不正");
	} else {
	  double price = fm.getPrice();
	  if (price < 0.0) fbv.getPrice().setText("---");
	  else setPrice(price);
	}
	fbv.setPoison(fm.isPoison());
	fbv.getPerson().setText(fm.getPerson());
	fbv.getComment().setText(fm.getComment());
	fbv.getReason().setText(fm.getReason());
	fbv.getTotal().setText(fm.getTotalText());
	fbv.pack();
	if (!deferredView) fbv.setVisible(true);
      }
    });
  }
  /**
   * 編集中のデータをモデルに反映させる。dがnullの場合、日付は更新しない
   * @param d java.util.Date
   */
  private void syncModel(Date d) {
    if (d != null) fm.setDate(d);
    fm.setPerson(authPerson);
    if (fbv == null) return;
    fm.setSG(new Double(fbv.getSG().getText()).doubleValue());
    fm.setComment(fbv.getComment().getText());
    fm.setReason(fbv.getReason().getText());
  }
  /**
   * 処方を更新する。まずデータの整合性をチェックし、不正であればダイアログを出す
   */
  public void update() {
    if (!editing) return;
    final Date date = new Date();
    syncModel(date);
    if (!fm.isLegal()) {
      JOptionPane.showMessageDialog(fbv,
				    "カラムに不正な値が入っているか、　\n" +
				    "または空白にできないカラムが空白になっています　\n\n" +
				    "更新理由は記入しましたか？　\n" +
				    "更新理由は空白にできません　",
				    "値が不正です",
				    JOptionPane.WARNING_MESSAGE);
      return;
    }

    Runnable chkLink = new Runnable() {
      public void run() {
	Vector followers = FormulaLinkC.getInstance(qm).getLinkedFormula(pcode);
	if (followers == null) {
	  showSQLErrDlg();
	  return;
	}
	if (followers.size() >1 && fbv != null) { // ビューを持っていない場合は常に次に進む
	  boolean go = showLinkedDlg();
	  if (!go) return; // リンク注意ダイアログでキャンセルされた場合
	}
	// 1999-7-27
	// 一度ダイアログを表示してAWTイベントスレッドに切替わると、
	// このスレッドにはどうも戻ってこないようなので、実際の更新は
	// 更に新しいスレッドを起動して行っている。この症状はinvokeAndWait
	// メソッドを使用しても変わらなかった
	forkUpdater(followers);
      }
    };
    Thread t = new Thread(chkLink);
    t.start();
  }
  /**
   * 分解処方テーブルを更新するようFormulaModelにリクエストする
   */
  public void updateResolvf() {
    fm.updateResolvf();
  }
}
