package formula.java;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.text.*;
import dap.*;
import formula.ui.*;
/**
 * この型は VisualAge で作成されました。
 */
public class MainViewC implements IQueryClient, IConsts, MatDeterminListener {
  private Main f;
  private QueryManager qm;
  private MainView mv = null;
  private NewMatC nmc = null;
  private NewProdC npc = null;
  private FormulaLinkC flc = null;
  private ConfReg crc = null;
  private MatGlobalUpdateC mguc = null;
  private Series series = null;
  private ProductResolver pr = null;
  private InclusionResolver ir = null;

  private Vector seriesData = null;
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

  private Vector resultData = null;
  private boolean resultProd = true;
  private class ResultListModel extends AbstractListModel<Object> {
    public Object getElementAt(int index) {
      if (resultData == null) return null;
      if (resultData.size() == 0) return "該当なし";
      Object[] o = SQLutil.getRow(resultData, index);
      return (resultProd) ? formatResultProd(o) : formatResultMat(o);
    }
    public int getSize() {
      if (resultData == null) return 0;
      int c = resultData.size();
      return (c == 0) ? 1 : c; // データが無い時に1行だけ"該当なし"の表示をさせるため
    }
    public void loaded() {
      if (resultData == null) super.fireContentsChanged(this, 0, 0);
      else super.fireContentsChanged(this, 0, resultData.size()-1);
    }
  };
  private ResultListModel rlm = new ResultListModel();

  private MatDeterminer md = null;
  private int determinedCode = -1;
  private String determinedName = null;
  private int determinedStatus = -1;
  /**
   * MainWinC コンストラクター・コメント。
   */
  public MainViewC(Main f, QueryManager qm) {
    super();
    this.f = f;
    this.qm = qm;
    series = new Series(qm);
    pr = new ProductResolver(qm);
    ir = new InclusionResolver(qm);
    mv = new MainView(this);
    flc = FormulaLinkC.getInstance(qm);
    init();
  }
  /**
   * 検索結果をクリアする。新たな検索を始める際に、一旦リストの表示を空白にするため
   */
  public void clearResult() {
    resultData = null;
    rlm.loaded();
  }
  /**
   * 製品による検索結果をフォーマットしてクリップボードにコピーする
   * 検索結果はpcode, series, name, percent, obsoleteの順で格納されており、
   * これをpcode, series, name, percent, obsoleteの順に並べる
   * @param v java.util.Vector
   */
  private void copyResultMat(Vector v) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    StringBuffer sb = new StringBuffer();
    for (int i=0, n=v.size(); i < n; i++) {
      Object[] o = SQLutil.getRow(v, i);
      sb.append((Integer)o[0]);
      sb.append('\t');
      sb.append((String)o[1]);
      sb.append('\t');
      sb.append((String)o[2]);
      sb.append('\t');
      sb.append(nf.format((Double)o[3]));
      sb.append('\t');
      switch (((Integer)o[4]).intValue()) {
      case 0:
      case 4:
	break;
      case 1:
	sb.append("廃番予定 ");
	break;
      case 2:
	sb.append("廃番 ");
	break;
	/*
	  case 4:
	  sb.append("韓国 ");
	  break;
	*/
      default:
      }
      sb.append('\n');
    }
    Clipboard cb = mv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(sb.toString());
    cb.setContents(ss, ss);
  }
  /**
   * 製品による検索結果をフォーマットしてクリップボードにコピーする
   * 検索結果はpcode, series, name, obsoleteの順で格納されており、
   * これをpcode, series, name, obsoleteの順に並べる
   * @param o java.lang.Object[]
   */
  private void copyResultProd(Vector v) {
    StringBuffer sb = new StringBuffer();
    for (int i=0, n=v.size(); i < n; i++) {
      Object[] o = SQLutil.getRow(v, i);
      sb.append((Integer)o[0]);
      sb.append('\t');
      sb.append((String)o[1]);
      sb.append('\t');
      sb.append((String)o[2]);
      sb.append('\t');
      switch (((Integer)o[3]).intValue()) {
      case 0:
      case 4:
	break;
      case 1:
	sb.append("廃番予定 ");
	break;
      case 2:
	sb.append("廃番 ");
	break;
	/*
	  case 4:
	  sb.append("韓国 ");
	  break;
	*/
      default:
      }
      sb.append('\n');
    }
    Clipboard cb = mv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(sb.toString());
    cb.setContents(ss, ss);
  }
  /**
   * クリップボードへのコピーを実行する
   */
  public void copyToClip() {
    if (resultProd) copyResultProd(resultData);
    else copyResultMat(resultData);
  }
  /**
   * プログラムを終了する。全てのウィンドウに対しクローズリクエストを発行し、クローズできない
   * ウィンドウがあれば終了しない
   * バックグランド処理中も終了しない
   */
  public void exit() {
    if (Decomposer.isBgProcessing()) {
      JOptionPane.showMessageDialog(mv,
				    "現在プログラム内部でデータを処理しています　\n" +
				    "これが済むまでプログラムは終了できません　",
				    "バックグランド処理の実行中です",
				    JOptionPane.WARNING_MESSAGE);
      return;
    }
    mv.saveStatus();
    if (f.requestClose() == false) return;
    f.exit();
    /*
      boolean allClosable = true;
      for (int i=0, n=FBlist.size(); i < n; i++) {
      FBrowseViewC fbc = (FBrowseViewC)FBlist.elementAt(i);
      if (!fbc.isClosable()) {
      allClosable = false;
      break;
      }
      }
      if (!allClosable) {
      JOptionPane.showMessageDialog(mv,
      "編集中のウィンドウを先に終了して下さい　",
      "編集中のウィンドウがあります",
      JOptionPane.WARNING_MESSAGE);
      return;
      }
      f.exit();
    */
  }
  /**
   * InclusionRosolverの検索結果をフォーマットする。検索結果は
   * pcode, series, name, percent, obsoleteの順で格納されており、
   * これをobsolete, percent, pcode, series, name
   * の順に並べる
   * @return java.lang.String
   * @param o java.lang.Object[]
   */
  private String formatResultMat(Object[] o) {
    StringBuffer sb = new StringBuffer(64);
    switch (((Integer)o[4]).intValue()) {
    case 0:
    case 4:
      break;
    case 1:
      sb.append("廃番予定 ");
      break;
    case 2:
      sb.append("廃番 ");
      break;
      /*
	case 4:
	sb.append("韓国 ");
	break;
      */
    default:
    }
    NumberFormat nf = NumberFormat.getPercentInstance();
    nf.setMinimumFractionDigits(3);
    sb.append(nf.format(((Double)o[3]).doubleValue() / 100.0));
    sb.append(' ');
    sb.append((Integer)o[0]);
    sb.append(' ');
    sb.append((String)o[1]);
    sb.append(' ');
    sb.append((String)o[2]);
    return sb.toString();
  }
  /**
   * ProductRosolverの検索結果をフォーマットする。検索結果は
   * pcode, series, name, obsoleteの順で格納されており、これをobsolete, pcode, series, name
   * の順に並べる
   * @return java.lang.String
   * @param o java.lang.Object[]
   */
  private String formatResultProd(Object[] o) {
    StringBuffer sb = new StringBuffer(56);
    switch (((Integer)o[3]).intValue()) {
    case 0:
    case 4:
      break;
    case 1:
      sb.append("廃番予定 ");
      break;
    case 2:
      sb.append("廃番 ");
      break;
      /*
	case 4:
	sb.append("韓国 ");
	break;
      */
    default:
    }
    sb.append((Integer)o[0]);
    sb.append(' ');
    sb.append((String)o[1]);
    sb.append(' ');
    sb.append((String)o[2]);
    return sb.toString();
  }
  /**
   * 処方ブラウザに渡すタイトルを作成する。タイトルはpcode, series, nameで構成される
   * @return java.lang.String
   * @param o java.lang.Object[]
   */
  private String formatTitle(Object[] o) {
    StringBuffer sb = new StringBuffer(56);
    sb.append((Integer)o[0]);
    sb.append(' ');
    sb.append((String)o[1]);
    sb.append(' ');
    sb.append((String)o[2]);
    return sb.toString();
  }
  /**
   * 与えられた製造コードを含むリンクグループに含まれる処方を返す。
   * 戻り値の各要素はObject[]で、順にlinkID, pcode, series, nameを含む
   * @return java.util.Vector
   * @param pcode int
   */
  public Vector getLinkedFormula(int pcode) {
    return flc.getLinkedFormula(pcode);
  }
  /**
   * @return formula.MatDeterminer
   */
  private MatDeterminer getMd() {
    if (md == null) {
      try {
	md = new MatDeterminer(qm, mv);
      } catch (Exception e) {e.printStackTrace();}
    }
    return md;
  }
  /**
   * @return formula.ui.MainView
   */
  public MainView getMV() {
    return mv;
  }
  /**
   * MainWinSのJListにモデルを設定し、
   * 製品シリーズをロードして表示させる。更に業務部への登録状況をチェックして表示する
   */
  private void init() {
    mv.getResultList().setModel(rlm);
    final JList seriesView = mv.getSeriesList();
    seriesView.setModel(slm);
    series.addClient(new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) return;
	seriesData = (Vector)o;
	slm.loaded();
	mv.setVisible(true);

//	Runnable createCRC = new Runnable() {
//	  public void run() {
//	    crc = new ConfRegC(qm, MainViewC.this);
//	  }
//	};
//	Thread t = new Thread(createCRC);
//	t.start();
      }
    }, 0);
    series.load();
  }
  /**
   * MatDeterminerで検索された資材コード、資材名、ステータスを記録し、
   * MainWinSの表示を更新し、資材による検索を実行する
   * @param canceled boolean
   * @param code int
   * @param name java.lang.String
   * @param status int
   */
  public void matDetermined(boolean canceled, int code, String name, int status) {
    if (canceled) return;
    determinedCode = code;
    determinedName = name;
    determinedStatus = status;
    Color c = null;
    switch (status) {
    case 0:
      c = Color.black;
      break;
    case 1:
      c = Color.magenta;
      break;
    case 2:
      c = Color.red;
      break;
    default:
      c = Color.black;
    }
    String ct = null;
    String nt = null;
    if (code < 0) {
      ct = "";
      nt = "該当なし";
    } else {
      ct = String.valueOf(determinedCode);
      nt = determinedName;
    }
    final Color fc = c;
    final String fct = ct;
    final String fnt = nt;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JTextField cf = mv.getCodeField();
	JTextField nf = mv.getNameField();
	cf.setForeground(fc);
	nf.setForeground(fc);
	cf.setText(fct);
	nf.setText(fnt);
      }
    });
    boolean fromNormal = (mv.getSearchMode() == 1) ? true : false;
    resultProd = false;
    Object[] selectedSeries = mv.getSeriesList().getSelectedValues();
    ir.setSeries(selectedSeries);
    ir.resolv(this, 1, determinedCode, (selectedSeries.length == 0), fromNormal);
  }
  /**
   * 検索結果を受け取り、MainWinSの表示を更新する
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) return;
    resultData = (Vector)o;
    rlm.loaded();
    //	mws.getResultList().revalidate();
    //	mws.getResultList().repaint();
  }
  /**
   * コード指定による検索を実行する。seriesのサイズが0の場合、全品種からの検索を行う
   * @param codeText java.lang.String
   * @param mode int
   */
  public void searchByCode(Object[] series, int code, int mode) {
    if (mode == 0) { // 製品で検索
      resultProd = true;
      pr.setSeries(series);
      pr.resolvByCode(this, 1, code);
    } else {
      getMd().searchByCode(this, code);
    }
  }
  /**
   * 名前指定による検索を実行する。seriesのサイズが0の場合、全品種からの検索を行う
   * 原料名から検索する時は原料名から原料コードを調べ、正しいコードが得られれば検索を行う
   * @param codeText java.lang.String
   * @param mode int
   */
  public void searchByName(Object[] series, String name, int mode) {
    if (mode == 0) { // 製品で検索
      resultProd = true;
      pr.setSeries(series);
      pr.resolvByName(this, 1, name, (series.length == 0));
    } else {
      getMd().setDialogLocator(mv.getNameField());
      getMd().searchByName(this, name);
    }
  }
  /**
   * 登録確認画面を表示する。
   */
  public void showCRC() {
    crc = new ConfReg();
  }
  /**
   * 検索結果リスト上で選択された処方のブラウザを表示する
   * @param selectedIndex int[]
   */
  public void showFBrowser(int[] selectedIndex) {
    for (int i=0; i < selectedIndex.length; i++) {
      int ix = selectedIndex[i];
      Object[] oa = SQLutil.getRow(resultData, ix);
      int pcode = ((Integer)oa[0]).intValue();
      String series = (String)oa[1];
      String name = (String)oa[2];
      FBrowseViewC fbvc = new FBrowseViewC(qm, pcode, null, series, name);
    }
  }
  /**
   * 処方リンク画面を表示する
   */
  public void showFLC() {
    if (flc == null) flc = FormulaLinkC.getInstance(qm);
    flc.show();
  }
  /**
   * 原料の一括更新を行うため、MatGloabalUpdateCを呼び出す
   */
  public void showMGU() {
    if (mguc == null) mguc = new MatGlobalUpdateC(qm);
    mguc.show();
  }
  /**
   * 新原料登録画面を表示する
   */
  public void showNMC() {
    if (nmc == null) nmc = new NewMatC(qm, this);
    nmc.show();
  }
  /**
   * 新製品登録画面を表示する
   */
  public void showNPC() {
    if (npc == null) npc = new NewProdC(qm, this, series);
    npc.show();
  }
}
/*
$Id: MainViewC.java,v 1.1 2008/10/17 01:15:40 wakui Exp $
*/
