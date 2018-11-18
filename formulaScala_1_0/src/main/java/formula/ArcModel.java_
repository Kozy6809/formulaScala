package formula;

import dap.*;
import java.util.*;
import java.text.*;
/**
 * この型は VisualAge で作成されました。
 */
public class ArcModel implements IFormulaModel, IQueryClient, IConsts {
  private int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private QueryManager qm;
  private FBrowseViewC fbc = null;
  private int pcode;
  private Decomposer dcmp = null;
  private Vector normData = new Vector();
  private Vector decompData = null;
  private boolean decompDataValid = false;
  private Object[] data2 = new Object[5];
  private double price = -1.0;
  private IQueryClient client = null;
  private int clientMode = 0;
  private boolean editable = true;
  private boolean editing = false;
  // 新規処方であることを示す。この場合、処方更新時にForm1,2からデータの削除を行わない
  private boolean newFormula = false;

  private static PQuery getArc1 = null;
  private static PQuery getArc2 = null;
  /**
   * ArcModel コンストラクター・コメント。
   * @param qm dap.QueryManager
   */
  public ArcModel(dap.QueryManager qm) {
    this.qm = qm;
    dcmp = new Decomposer(qm);
    editable = false;
    startPrepare();
  }
  /**
   * 処方の更新を実行するが、コミットを行わない。処方リンクで複数処方を同時に更新する時に使用
   * @param client dap.IQueryClient
   * @param mode int
   */
  public boolean chainUpdate(int transactionID) {
    return false;
  }
  /**
   * このモデルのdb上の値がまだ新規処方で、form1のデータが存在しないかどうか確認し、
   * newFormulaフィールドに結果をセットする
   * 通常はload()メソッドによってnewFormulaはセットされるが、別のモデルからデータをコピー
   * する場合はload()メソッドが呼び出されないため、newFormulaを正しくセットするには
   * このメソッドを呼ぶ必要がある
   * このメソッドはAWTスレッドから呼び出してはならない
   * @return boolean
   */
  public boolean chkNewFormula() {
    return false;
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
   * 別のモデルからデータをコピーする。製造コードはコピーしない
   * @param fm formula.AbstractFormulaModel
   */
  public void copyData(IFormulaModel fm) {
    data2 = null;
    normData = null;
    decompData = null;
    decompDataValid = false;
    Object[] o = fm.getData2();
    if (o != null) data2 = (Object[])o.clone();
    Vector v = fm.getNormData();
    if (v != null) normData = (Vector)v.clone();
    v = fm.getDecompData();
    if (v != null) {
      decompData = (Vector)v.clone();
      decompDataValid = true;
    }
    editable = fm.isEditable();
  }
  /**
   * normDataから指定行を削除する
   * @param index int
   */
  public void deleteRow(int index) {
    if (!editable || index >= normData.size()) return;
    normData.removeElementAt(index);
  }
  /**
   * Object.equals()をオーバーライドし、製造コードと登録日付が等しい時trueを返す
   * @return boolean
   * @param o java.lang.Object
   */
  public boolean equals(Object o) {
    if (o == null) return false;
    ArcModel am = null;
    try {
      am = (ArcModel)o;
    } catch (ClassCastException e) {
      return false;
    }
    if (pcode == am.getPcode() && getDate().equals(am.getDate())) return true;
    else return false;
  }
  /**
   * normDataの指定行と下の行を交換する
   * @param index int
   */
  public void exchangeRow(int index) {
    if (!editable || index > normData.size()-2) return;
    Object o = normData.elementAt(index);
    normData.setElementAt(normData.elementAt(index+1), index);
    normData.setElementAt(o, index+1);
  }
  /**
   * データをクリップボードにコピーできるフォーマットにする
   * データの並びはpcode, series, name, sg, price, order, mcode, mname, percent, comment, 
   * person, date, reasonの順とする
   * priceの値は旧処方の場合は使用されないが、現行処方と書式を揃えるために、priceの位置に
   * "---"を挿入する
   */
  private String formatForCopy(String series, String name, Vector d) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    NumberFormat nf2 = NumberFormat.getInstance();
    nf2.setMinimumFractionDigits(2);
    nf2.setMaximumFractionDigits(2);
    StringBuffer sb = new StringBuffer();
    String p = String.valueOf(pcode);
    String date = DateFormat.getDateInstance().format((Date)data2[0]);
    String sg = nf2.format((Double)data2[1]);
    String person = (String)data2[2];
    if (person == null) person = "";
    String comment = (String)data2[3];
    if (comment == null) comment = "";
    String reason = (String)data2[4];
    if (reason == null) reason = "";
    for (int i=0, n=d.size(); i < n; i++) {
      Object[] o = SQLutil.getRow(d, i);
      String order = String.valueOf(i+1);
      String mcode = ((Integer)o[0]).toString();
      String mname = (String)o[1];
      String percent = nf.format((Double)o[2]);
      sb.append(p);
      sb.append('\t');
      sb.append(series);
      sb.append('\t');
      sb.append(name);
      sb.append('\t');
      sb.append(sg);
      sb.append('\t');
      sb.append("---");
      sb.append('\t');
      sb.append(order);
      sb.append('\t');
      sb.append(mcode);
      sb.append('\t');
      sb.append(mname);
      sb.append('\t');
      sb.append(percent);
      sb.append('\t');
      sb.append('"');
      sb.append(comment);
      sb.append('"');
      sb.append('\t');
      sb.append(person);
      sb.append('\t');
      sb.append(date);
      sb.append('\t');
      sb.append('"');
      sb.append(reason);
      sb.append('"');
      sb.append('\n');
    }
    return sb.toString();
  }
  /**
   * normDataやdecompDataを印刷向けのフォーマットにする
   * @return java.util.Vector
   * @param d java.util.Vector
   */
  private Vector formatForPrint(Vector d) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    Vector r = new Vector();
    for (int i=0, n=d.size(); i < n; i++) {
      Object[] o = SQLutil.getRow(d, i);
      String[] s = new String[3];
      s[0] = ((Integer)o[0]).toString();
      s[1] = (String)o[1];
      s[2] = nf.format((Double)o[2]);
      r.addElement(s);
    }
    String[] s = new String[]{"", "合計", nf.format(getTotal())};
    r.addElement(s);
    return r;
  }
  /**
   * @return java.lang.String
   */
  public String getComment() {
    return (String)data2[3];
  }
  /**
   * @return Object[]
   */
  public Object[] getData2() {
    return data2;
  }
  /**
   * @return java.util.Date
   */
  public Date getDate() {
    return (Date)data2[0];
  }
  /**
   * 分解処方を返す
   * @return java.util.Vector
   */
  public Vector getDecompData() {
    return decompData;
  }
  /**
   * decompDataのサイズを返す
   * @return int
   */
  public int getDecompDataSize() {
    return (decompData == null) ? 0 : decompData.size();
  }
  /**
   * decompDataとdata2の組み合わせのクリップボード向けフォーマットにして返す
   */
  public String getDecompForCopy(String series, String name) {
    return formatForCopy(series, name, decompData);
  }
  /**
   * decompDataを印刷向けにフォーマットする
   */
  public Vector getDecompForPrint() {
    return formatForPrint(decompData);
  }
  /**
   * 資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
   * @return int
   * @param row int
   */
  public int getDecompMatStatus(int row) {
    if (!decompDataValid) return 0;
    return ((Integer)SQLutil.get(decompData, row, 3)).intValue();
  }
  /**
   * decompDataから値を取り出す
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getDecompValueAt(int row, int column) {
    return SQLutil.get(decompData, row, column);
  }
  /**
   * 通常処方の値を取得する。これは処方リンク時のコピー用
   * @param data java.util.Vector
   */
  public Vector getNormData() {
    return normData;
  }
  /**
   * normDataのサイズを返す
   * @return int
   */
  public int getNormDataSize() {
    return (normData == null) ? 0 : normData.size();
  }
  /**
   * normDataとdata2の組み合わせのクリップボード向けフォーマットにして返す
   */
  public String getNormForCopy(String series, String name) {
    return formatForCopy(series, name, normData);
  }
  /**
   * normDataを印刷向けにフォーマットする
   */
  public Vector getNormForPrint() {
    return formatForPrint(normData);
  }
  /**
   * 資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
   * @return int
   * @param row int
   */
  public int getNormMatStatus(int row) {
    return ((Integer)SQLutil.get(normData, row, 3)).intValue();
  }
  /**
   * @return int
   */
  public int getPcode() {
    return pcode;
  }
  /**
   * @return java.lang.String
   */
  public String getPerson() {
    return (String)data2[2];
  }
  /**
   * 製品単価を返すが、ArcModelの場合は常に-1を返す。これとisPriceValid()が常にtrueを
   * 返すことにより、表示系に単価表示を行わないことを認識させる
   * @return double
   */
  public double getPrice() {
    return -1.0;
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getArc1.getReady();
    int i2 = getArc2.getReady();

    if (i1 < 0 || i2 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0) ready = 1;
    return ready;
  }
  /**
   * @return java.lang.String
   */
  public String getReason() {
    return (String)data2[4];
  }
  /**
   * @return double
   */
  public double getSG() {
    return ((Double)data2[1]).doubleValue();
  }
  /**
   * 通常処方データからパーセント値の合計を求める。分解処方が表示されている場合でも、
   * この値は分解処方の合計値と一致することが期待されている
   * @return double
   */
  public double getTotal() {
    double t = 0.0;
    for (int i=0, n=normData.size(); i < n; i++) {
      t += ((Double)SQLutil.get(normData, i, 2)).doubleValue();
    }
    return t;
  }
  /**
   * 合計値を文字列化して返す
   * @return java.lang.String
   */
  public String getTotalText() {
    double t = getTotal();
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(3);
    nf.setMaximumFractionDigits(3);
    return nf.format(t);
  }
  /**
   * normDataから値を取り出す
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getValueAt(int row, int column) {
    return SQLutil.get(normData, row, column);
  }
  /**
   * normDataの指定位置に新規の行を挿入する。指定されたインデックスの行は一つ下にずれる。
   * すなわち新しい行は指定行の前に挿入される
   * @param index int
   */
  public void insertRow(int index) {
    if (!editable || index >= normData.size()) return;
    normData.insertElementAt(new Object[] {new Integer(0), "", new Double(0.0), new Integer(0), new Integer(0)}, index);
  }
  /**
   * @return boolean
   */
  public boolean isDecompDataValid() {
    return decompDataValid;
  }
  /**
   * @return boolean
   */
  public boolean isEditable() {
    return editable;
  }
  /**
   * @return boolean
   */
  public boolean isEditing() {
    return editing;
  }
  /**
   * 現在のデータがデータベースの様々な制約に違反しないかチェックする
   * チェック項目は以下の通り
   * ・更新理由がnullまたは空文字列でないこと
   * ・全ての資材コードが0以下でないこと
   * @return boolean
   */
  public boolean isLegal() {
    String reason = (String)data2[4];
    if (reason == null || reason.length() == 0) return false;
    for (int i=0, n=normData.size(); i < n; i++) {
      if (((Integer)SQLutil.get(normData, i, 0)).intValue() <= 0) return false;
    }
    return true;
  }
  /**
   * @return boolean
   */
  public boolean isNewFormula() {
    System.out.println(newFormula);
    return newFormula;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  public boolean isPoison() {
    return false;
  }
  /**
   * このモデルが保持している製品単価が正しければtrueを返す。ArcModelの場合常にtrueを返すが、
   * 一方getPrice()は常に-1を返すため、この組み合わせで単価表示を行わないことを認識させる
   * 作成日 : (00-02-16 09:43:49)
   * @return boolean
   */
  public boolean isPriceValid() {
    return true;
  }
  /**
   * load メソッド・コメント。
   */
  public boolean load(IQueryClient client, int mode, int pcode, Date date) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    this.pcode = pcode;
    Object[] p = {new Integer(pcode), date};
    getArc1.query(this, 1, p);
    getArc2.query(this, 2, p);
    return true;
  }
  /**
   * loadDecomp メソッド・コメント。
   */
  public boolean loadDecomp(dap.IQueryClient client, int mode, int pcode) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    this.pcode = pcode;
    dcmp.calcDecomp(this, 3, pcode, normData);
    return true;
  }
  /**
   * 読み込んだ通常処方の行数が0だった場合、これは新規登録されてまだ処方が入力されていない
   * 製品であるので、代わりに初期データを生成する
   */
  protected Vector makeNullData() {
    Vector r = new Vector(6);
    for (int i=1; i < 7; i++) {
      Object[] o = new Object[] {
	new Integer(0), "", new Double(0.0), new Integer(0), new Integer(i)
      };
      r.addElement(o);
    }
    return r;
  }
  /**
   * modeが1でnormData、2でdata2、3でdecompData、4でpriceを扱うことを示す
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      //		valid = false;
      client.queryCallBack(o, mode);
      return;
    }
    switch (mode) {
    case 1:
      normData = (Vector)o;
      if (normData.size() == 0) {
	newFormula = true;
	normData = makeNullData();
      } else newFormula = false;
      break;
    case 2:
      data2 = SQLutil.getRow((Vector)o, 0);
      client.queryCallBack(o, clientMode);
      break;
    case 3:
      decompData = (Vector)o;
      decompDataValid = true;
      client.queryCallBack(o, clientMode);
      break;
    default :
    }
  }
  /**
   * @param comment java.lang.String
   */
  public void setComment(String comment) {
    if (!editable) return;
    data2[3] = comment;
  }
  /**
   * @param date java.util.Date
   */
  public void setDate(Date date) {
    if (!editable) return;
    data2[0] = date;
  }
  /**
   * 編集の開始時にtrueにセットし、終了時にfalseにセットする
   * @param b boolean
   */
  public void setEditing(boolean b) {
    if (!editable) return;
    editing = b;
  }
  /**
   * @param fbc formula.FBrowseViewC
   */
  public void setFBC(FBrowseViewC fbc) {
    this.fbc = fbc;
  }
  /**
   * 通常処方の値をセットする。これは処方リンク時のコピー用
   * @param data java.util.Vector
   */
  public void setNormData(Vector data) {
    normData = data;
  }
  /**
   * 製造コードをセットする。これは処方リンク時のコピー用
   * @param data java.util.Vector
   */
  public void setPcode(int pcode) {
    this.pcode = pcode;
  }
  /**
   * @param person java.lang.String
   */
  public void setPerson(String person) {
    if (!editable) return;
    data2[2] = person;
  }
  /**
   * @param reason java.lang.String
   */
  public void setReason(String reason) {
    if (!editable) return;
    data2[4] = reason;
  }
  /**
   * @param sg double
   */
  public void setSG(double sg) {
    if (!editable) return;
    data2[1] = new Double(sg);
  }
  /**
   * normDataに値を設定する
   * @param value java.lang.Object
   * @param row int
   * @param column int
   */
  public void setValueAt(Object value, int row, int column) {
    if (!editable || row >= normData.size() || column > 3) return;
    SQLutil.set(normData, row, column, value);
    decompDataValid = false;
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  protected synchronized void startPrepare() {
    if (getArc1 == null) {
      int[] INtypes = new int[2];
      INtypes[0] = INT;
      INtypes[1] = TIMESTAMP;
      String o;
      if (System.getProperty("postgre") == null) o = "order";
      else o = "order_";
      getArc1 = new PQuery
	(qm, "select f.mcode, m.mname, f.percent, m.status, f." + o + " from arc1 f, mcode m " +
	 "where f.mcode = m.mcode and f.pcode = ? and f.date = ? order by f." + o, INtypes);
      getArc1.prepare();
    }

    if (getArc2 == null) {
      int[] INtypes = new int[2];
      INtypes[0] = INT;
      INtypes[1] = TIMESTAMP;
      getArc2 = new PQuery
	(qm, "select date, sg, person, comment, reason from arc2 where pcode = ? and date = ?", INtypes);
      getArc2.prepare();
    }
  }
  /**
   * 処方の更新を実行する
   * @param client dap.IQueryClient
   * @param mode int
   */
  public boolean update(IQueryClient client, int mode) {
    return false;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  public boolean updateAndWait() {
    return false;
  }
  /**
   * 分解処方テーブルを更新する
   */
  public void updateResolvf() {
  }
}
