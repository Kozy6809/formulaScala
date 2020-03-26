package formula;

import dap.*;
import java.util.*;
import java.text.*;
import java.sql.SQLException;
import myutil.*;
/**
 * この型は VisualAge で作成されました。
 */
public class FormulaModel implements IFormulaModel, IQueryClient, IConsts {
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
  private boolean validPrice = false;
  private IQueryClient client = null;
  private int clientMode = 0;
  private boolean editable = true;
  private boolean editing = false;
  // 新規処方であることを示す。この場合、処方更新時にForm1,2からデータの削除を行わない
  private boolean newFormula = false;
  // 原料単価を保持するVector。処方更新時に製品単価を再計算するために用いる
  private Vector prices = new Vector();
  private boolean validPrices = false;
  private boolean priceDesabled = false; // setPrices()で単価の初期設定が
  // 間に合わなかった時にtrueになる

  private boolean poison = false; // 毒性の有無
  private static PQuery getForm1 = null;
  private static PQuery getForm2 = null;
  private static PQuery chkPrice = null;
  private static PQuery getPrice = null;
  private static PQuery getMatPrice = null;
  private static PQuery getPoison = null;
  private static PUpdate insArc1 = null;
  private static PUpdate insArc2 = null;
  private static PUpdate delForm1 = null;
  private static PUpdate delForm2 = null;
  private static PUpdate setForm1 = null;
  private static PUpdate setForm2 = null;
  /**
   * FormulaModel コンストラクター・コメント。
   * @param qm dap.QueryManager
   */
  public FormulaModel(QueryManager qm) {
    this.qm = qm;
    dcmp = new Decomposer(qm);
    startPrepare();
  }
  /**
   * 既知の処方データからFormulaModelを構成するためのコンストラクタ
   * @param qm dap.QueryManager
   */
  public FormulaModel(QueryManager qm, int pcode, Object[] data2, Vector normData) {
    this.qm = qm;
    this.pcode = pcode;
    this.data2 = data2;
    this.normData = normData;
    dcmp = new Decomposer(qm);
    startPrepare();
  }
  /**
   * 処方の更新を実行するが、コミットを行わない。処方リンクで複数処方を同時に更新する時に使用
   * コミットされないため、分解処方の再計算も行わない。処方分解は複数処方の更新が
   * 正常に終了した後でまとめてリクエストする。update()と比較せよ
   * エラー時のロールバックはここで実行する
   * トランザクションIDにtIDを使用する
   * このメソッドはバックグランドで実行される必要がある
   * @param client dap.IQueryClient
   * @param mode int
   */
  public boolean chainUpdate(int tID) {
    if (!chkReady()) return false;
    try {
      Object[] p = {new Integer(pcode)};
      if (!newFormula) { // 新規処方の場合、form2のデータのみが存在する
	insArc1.updateAndWait(p, tID);
	insArc2.updateAndWait(p, tID);
	delForm1.updateAndWait(p, tID);
      }
      delForm2.updateAndWait(p, tID);
      for (int i=0, n=normData.size(); i < n; i++) {
	Object[] row = SQLutil.getRow(normData, i);
	Object[] out = {p[0], data2[0], new Integer(i+1), row[0], row[2]};
	setForm1.updateAndWait(out, tID);
      }
      Object[] out = {p[0], data2[0], data2[1], data2[2], data2[3], data2[4]};
      setForm2.updateAndWait(out, tID);
    } catch (SQLException e) {
      try {
	qm.rollbackAndWait(tID);
      } catch (SQLException ee) {}
      return false;
    }
    return true;
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
    Object[] p = new Object[1];
    p[0] = new Integer(pcode);
    try {
      Vector r = getForm1.queryAndWait(p);
      newFormula = (r.size() == 0) ? true : false;
    } catch (SQLException e) {}
    return newFormula;
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
    price = fm.getPrice();
    poison = fm.isPoison();
  }
  /**
   * normDataから指定行を削除する
   * @param index int
   */
  public void deleteRow(int index) {
    if (!editable || index >= normData.size()) return;
    normData.removeElementAt(index);
    if (!priceDesabled) {
      if (validPrices) {
	prices.removeElementAt(index);
	recalcPrice();
      } else priceDesabled = true;
    }
  }
  /**
   * Object.equals()をオーバーライドし、製造コードが等しい時trueを返す
   * @return boolean
   * @param o java.lang.Object
   */
  public boolean equals(Object o) {
    if (o == null) return false;
    try {
      if (pcode == ((FormulaModel)o).getPcode()) return true;
    } catch (ClassCastException e) {}
    return false;
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

    if (!priceDesabled) {
      if (validPrices) {
	o = prices.elementAt(index);
	prices.setElementAt(prices.elementAt(index+1), index);
	prices.setElementAt(o, index+1);
      } else priceDesabled = true;
    }
  }
  /**
   * データをクリップボードにコピーできるフォーマットにする
   * データの並びはpcode, series, name, sg, percent, order, mcode, mname, percent, comment, 
   * person, date, reasonの順とする
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
    String sg = nf.format((Double)data2[1]);
    String person = (String)data2[2];
    if (person == null) person = "";
    String comment = (String)data2[3];
    if (comment == null) comment = "";
    String reason = (String)data2[4];
    if (reason == null) reason = "";
    String priceText = nf.format(price);
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
      sb.append(priceText);
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
   * 与えられた資材コードからgetMatPriceを使用して単価を取り出す。
   * 与えられたコードが中間品だったら、getPriceを使用して中間品の製品単価を取り出す。
   * このメソッドはバックグランドで使用すること
   * @return java.lang.Object
   * @param mcode java.lang.Integer
   */
  private Object getAPrice(Integer mcode) {
    if (!chkReady()) return null;
    Object[] p = new Object[1];
    Vector r = null;
    try {
      p[0] = mcode;
      if (mcode.intValue() < 500000) { // magic number!!
	r = getMatPrice.queryAndWait(p);
      } else {
	r = getPrice.queryAndWait(p);
      }
    } catch (SQLException e) {
      return null;
    }
    return ((Object[])r.elementAt(0))[0];
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
   * 製品単価を返す。このオブジェクトが保持している原料単価が不正なために製品単価が
   * 計算できない場合は-1を返す
   * @return double
   */
  public double getPrice() {
    return (validPrice) ? price : -1.0;
  }
  /**
   * getReady メソッド・コメント。
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = getForm1.getReady();
    int i2 = getForm2.getReady();
    int i3 = insArc1.getReady();
    int i4 = insArc2.getReady();
    int i5 = delForm1.getReady();
    int i6 = delForm2.getReady();
    int i7 = setForm1.getReady();
    int i8 = setForm2.getReady();
    int i9 = getPrice.getReady();
    int i10 = getMatPrice.getReady();
    int i11 = getPoison.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0 || i4 < 0 || i5 < 0 || i6 < 0 || i7 < 0 || i8 < 0 || i9 < 0 || i10 < 0 || i11 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0 && i4 > 0 && i5 > 0 && i6 > 0 && i7 > 0 && i8 > 0 && i9 > 0 && i10 > 0 && i11 > 0) ready = 1;
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
    if (!priceDesabled) {
      if (validPrices) {
	prices.insertElementAt(null, index);
      } else priceDesabled = true;
    }
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
   * @return boolean
   */
  public boolean isPoison() {
    return poison;
  }
  /**
   * このモデルが保持している原料単価が不正でなければtrueを返す。
   * 作成日 : (00-02-15 15:51:35)
   * @return boolean
   */
  public boolean isPriceValid() {
    return validPrice;
  }
  /**
   * load メソッド・コメント。
   */
  public boolean load(IQueryClient client, int mode, int pcode, Date date) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    this.pcode = pcode;
    Object[] p = {new Integer(pcode)};
    getForm1.query(this, 1, p);
    getForm2.query(this, 2, p);
    chkPrice.query(this, 6, p);
    getPrice.query(this, 4, p);
    getPoison.query(this, 5, p);
    return true;
  }
  /**
   * loadAndWait メソッド・コメント。
   */
  public boolean loadAndWait(int pcode, Date date) {
    if (!chkReady()) return false;
    this.pcode = pcode;
    Object[] p = {new Integer(pcode)};
    try {
      normData = getForm1.queryAndWait(p);
      if (normData.size() == 0) {
	newFormula = true;
	normData = makeNullData();
      } else newFormula = false;
      data2 = SQLutil.getRow(getForm2.queryAndWait(p), 0);
      Vector v = chkPrice.queryAndWait(p);
      validPrice = (v.size() > 0) ? false : true;
      price = ((Double)SQLutil.get(getPrice.queryAndWait(p), 0, 0)).doubleValue();
      if (getPoison.queryAndWait(p).size() > 0) poison = true;
      return true;
    } catch (SQLException e) {
      return false;
    }
  }
  /**
   * 製造コードから分解処方データを検索してロードする。現行処方を検索する場合dateはnullになる
   * 検索できない場合はfalseを返す
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   */
  public boolean loadDecomp(IQueryClient client, int mode, int pcode) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    this.pcode = pcode;
    if (editing) {
      dcmp.calcDecomp(this, 3, pcode, normData);
    } else {
      dcmp.load(this, 3, pcode, normData);
    }
    return true;
  }
  /**
   * 読み込んだ通常処方の行数が0だった場合、これは新規登録されてまだ処方が入力されていない
   * 製品であるので、代わりに初期データを生成する
   */
  private Vector makeNullData() {
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
   * modeが1でnormData、2でdata2、3でdecompData、4でprice、5でpoison、6でchkPrice
   * を扱うことを示す
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
      break;
    case 3:
      decompData = (Vector)o;
      decompDataValid = true;
      client.queryCallBack(o, clientMode);
      break;
    case 4:
      Vector v = (Vector)o;
      if (v.size() > 0) price = ((Double)SQLutil.get(v, 0, 0)).doubleValue();
      break;
    case 5:
      v = (Vector)o;
      if (v.size() > 0) poison = true;
      client.queryCallBack(o, clientMode);
      break;
    case 6:
      v = (Vector)o;
      validPrice = (v.size() > 0) ? false : true;
      break;
    default :
    }
  }
  /**
   * 製品単価の再計算を実行する
   */
  private void recalcPrice() {
    double sum = 0.0;
    for (int i=0, n=prices.size(); i < n; i++) {
      double percent = ((Double)getValueAt(i, 2)).doubleValue();
      Double aPrice = (Double)prices.elementAt(i);
      double d = (aPrice == null) ? 0.0 : aPrice.doubleValue();
      sum += percent * d / 100.0;
    }
    price = sum;
    if (fbc != null) fbc.setPrice(sum);
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
    if (b) setPrices();
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
   * 編集開始時に、原料単価のリストをセットする
   */
  private void setPrices() {
    if (!chkReady()) return;
    validPrices = false;
    validPrice = true;
    Runnable r = new Runnable() {
      public void run() {
	for (int i=0, n=getNormDataSize(); i < n; i++) {
	  Integer mcode = (Integer)getValueAt(i, 0);
	  Object o = getAPrice(mcode);
	  if (o == null) return;
	  if (((Double)o).doubleValue() <= 0.0) validPrice = false;
	  prices.addElement(o);
	}
	validPrices = true;
      }
    };
    Thread t = new Thread(r);
    t.start();
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
  public void setValueAt(Object value, final int row, int column) {
    if (!editable || row >= normData.size() || column > 3) return;
    SQLutil.set(normData, row, column, value);
    decompDataValid = false;
    if (!priceDesabled) {
      if (validPrices) {
	if (column == 2) recalcPrice();
	if (column == 0) {
	  final Integer mcode = (Integer)value;
	  Runnable r = new Runnable() {
	    public void run() {
	      Object o = getAPrice(mcode);
	      prices.setElementAt(o, row);
	      validPrice = true;
	      for (int i=0, n=prices.size(); i < n; i++) {
		if (((Double)prices.elementAt(i)).doubleValue() <= 0.0) {
		  validPrice = false;
		  break;
		}
	      }
	      recalcPrice();
	    }
	  };
	  Thread t = new Thread(r);
	  t.start();
	}
      } else priceDesabled = true;
    }
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (getForm1 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      String o;
      if (System.getProperty("postgre") == null) o = "order";
      else o = "order_";
      System.out.println(o);
      getForm1 = new PQuery
	(qm, "select f.mcode, m.mname, f.percent, m.status, f." + o + " from form1 f, mcode m " +
	 "where f.mcode = m.mcode and f.pcode = ? order by f." + o, INtypes);
      getForm1.prepare();
    }

    if (getForm2 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getForm2 = new PQuery
	(qm, "select date, sg, person, comment, reason from form2 where pcode = ?", INtypes);
      getForm2.prepare();
    }
	
    if (chkPrice == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      chkPrice = new PQuery
	(qm, "select pcode from resolvf r, mcode m where r.mcode = m.mcode " +
	 "and m.price <= 0.0 group by pcode having pcode = ?", INtypes);
      chkPrice.prepare();
    }
	
    if (getPrice == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getPrice = new PQuery
	(qm, "select sum(m.price * percent) / 100 from resolvf r, mcode m " +
	 "where r.pcode = ? and m.mcode = r.mcode", INtypes);
      getPrice.prepare();
    }
	
    if (getMatPrice == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getMatPrice = new PQuery
	(qm, "select price from mcode m where m.mcode = ?", INtypes);
      getMatPrice.prepare();
    }
	
    if (getPoison == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      getPoison = new PQuery
	(qm, "select m.toxNo, sum(m.percent * r.percent / 100.0), i.threshold " +
	 "from resolvf r, toxMcode m, toxIx i " +
	 "where r.pcode = ? and r.mcode = m.mcode and i.toxNo = m.toxNo " +
	 "group by m.toxNo, i.threshold " +
	 "having sum(m.percent * r.percent / 100.0) > i.threshold " +
	 "order by m.toxNo", INtypes);
      getPoison.prepare();
    }
	
    if (insArc1 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      String o;
      if (System.getProperty("postgre") == null) o = "order";
      else o = "order_";
      insArc1 = new PUpdate
	(qm, "insert into arc1 select pcode, date, " + o + ", mcode, percent, op_ing " +
	 "from form1 where pcode = ?", INtypes);
      insArc1.prepare();
    }
	
    if (insArc2 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      insArc2 = new PUpdate
	(qm, "insert into arc2 select pcode, date, sg, person, comment, reason " +
	 "from form2 where pcode = ?", INtypes);
      insArc2.prepare();
    }
	
    if (delForm1 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delForm1 = new PUpdate(qm, "delete from form1 where pcode = ?", INtypes);
      delForm1.prepare();
    }
	
    if (delForm2 == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      delForm2 = new PUpdate(qm, "delete from form2 where pcode = ?", INtypes);
      delForm2.prepare();
    }
	
    if (setForm1 == null) {
      int[] INtypes = new int[5];
      INtypes[0] = INT;
      INtypes[1] = TIMESTAMP;
      INtypes[2] = INT;
      INtypes[3] = INT;
      INtypes[4] = FLOAT;
      setForm1 = new PUpdate(qm, "insert into form1 values(?, ?, ?, ?, ?, null)", INtypes);
      setForm1.prepare();
    }

    if (setForm2 == null) {
      int[] INtypes = new int[6];
      INtypes[0] = INT;
      INtypes[1] = TIMESTAMP;
      INtypes[2] = FLOAT;
      INtypes[3] = STRING;
      INtypes[4] = STRING;
      INtypes[5] = STRING;
      setForm2 = new PUpdate(qm, "insert into form2 values(?, ?, ?, ?, ?, ?, null)", INtypes);
      setForm2.prepare();
    }	
  }
  /**
   * 処方の更新を実行する
   */
  public boolean update(final IQueryClient client0, final int mode0) {
    if (!chkReady()) return false;
    final int tID = qm.getTransactionID();
    Runnable updater = new Runnable() {
      public void run() {
	try {
	  Object[] p = {new Integer(pcode)};
	  if (!newFormula) { // 新規処方の場合、form2のデータのみが存在する
	    insArc1.updateAndWait(p, tID);
	    insArc2.updateAndWait(p, tID);
	    delForm1.updateAndWait(p, tID);
	  }
	  delForm2.updateAndWait(p, tID);
	  for (int i=0, n=normData.size(); i < n; i++) {
	    Object[] row = SQLutil.getRow(normData, i);
	    Object[] out = {p[0], data2[0], new Integer(i+1), row[0], row[2]};
	    setForm1.updateAndWait(out, tID);
	  }
	  Object[] out = {p[0], data2[0], data2[1], data2[2], data2[3], data2[4]};
	  setForm2.updateAndWait(out, tID);
	  qm.commitAndWait(tID);
	  client0.queryCallBack(null, mode0);
	  dcmp.updateResolvf(pcode);
	} catch (SQLException e) {
	  try {
	    qm.rollbackAndWait(tID);
	  } catch (SQLException ee) {}
	  client0.queryCallBack(e, SQLERROR);
	}
      }
    };
    Thread t = new Thread(updater);
    t.start();
    return true;
  }
  /**
   * 処方の更新を実行する。正常に更新できればtrueを返す
   */
  public boolean updateAndWait() {
    if (!chkReady()) return false;
    int tID = qm.getTransactionID();
    try {
      Object[] p = {new Integer(pcode)};
      if (!newFormula) { // 新規処方の場合、form2のデータのみが存在する
	insArc1.updateAndWait(p, tID);
	insArc2.updateAndWait(p, tID);
	delForm1.updateAndWait(p, tID);
      }
      delForm2.updateAndWait(p, tID);
      for (int i=0, n=normData.size(); i < n; i++) {
	Object[] row = SQLutil.getRow(normData, i);
	Object[] out = {p[0], data2[0], new Integer(i+1), row[0], row[2]};
	setForm1.updateAndWait(out, tID);
      }
      Object[] out = {p[0], data2[0], data2[1], data2[2], data2[3], data2[4]};
      setForm2.updateAndWait(out, tID);
      qm.commitAndWait(tID);
      return true;
    } catch (SQLException e) {
      try {
	qm.rollbackAndWait(tID);
      } catch (SQLException ee) {}
    }
    return false;
  }
  /**
   * 分解処方テーブルを更新する
   */
  public void updateResolvf() {
    dcmp.updateResolvf(pcode);
  }
}
