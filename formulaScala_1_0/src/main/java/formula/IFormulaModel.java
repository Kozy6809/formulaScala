package formula;

import java.util.*;
import dap.*;
/**
 * 旧システムの処方データモデルが実装するインターフェース。新システムでは使わない<p>
$Id: IFormulaModel.java,v 1.1 2008/10/17 01:15:40 wakui Exp $
 */
public interface IFormulaModel {
  /**
   * 処方の更新を実行するが、コミットを行わない。処方リンクで複数処方を同時に更新する時に使用
   */
  public boolean chainUpdate(int transactionID);
  /**
   * このモデルのdb上の値がまだ新規処方で、form1のデータが存在しないかどうか確認し、
   * newFormulaフィールドに結果をセットする
   * 通常はload()メソッドによってnewFormulaはセットされるが、別のモデルからデータをコピー
   * する場合はload()メソッドが呼び出されないため、newFormulaを正しくセットするには
   * このメソッドを呼ぶ必要がある
   * このメソッドはAWTスレッドから呼び出してはならない
   * @return boolean
   */
  public boolean chkNewFormula();
  /**
   * このオブジェクトの準備(即ち全てのprepareの完了)ができているかチェックし、
   * 未了ならば完了を待ってtrueを返す。もしエラーになればfalseを返す。
   * @return boolean
   */
  public boolean chkReady();
  /**
   * 別のモデルからデータをコピーする。製造コードはコピーしない
   * @param fm formula.AbstractFormulaModel
   */
  public void copyData(IFormulaModel fm);
  /**
   * normDataから指定行を削除する
   * @param index int
   */
  public void deleteRow(int index);
  /**
   * normDataの指定行と下の行を交換する
   * @param index int
   */
  public void exchangeRow(int index);
  /**
   * @return java.lang.String
   */
  public String getComment();
  /**
   * @return Object[]
   */
  public Object[] getData2();
  /**
   * @return java.util.Date
   */
  public Date getDate();
  /**
   * 分解処方を返す
   * @return java.util.Vector
   */
  public Vector getDecompData();
  /**
   * decompDataのサイズを返す
   * @return int
   */
  public int getDecompDataSize();
  /**
   * decompDataとdata2の組み合わせのクリップボード向けフォーマットにして返す
   */
  public String getDecompForCopy(String series, String name);
  /**
   * decompDataを印刷向けにフォーマットする
   */
  public Vector getDecompForPrint();
  /**
   * 資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
   * @return int
   * @param row int
   */
  public int getDecompMatStatus(int row);
  /**
   * decompDataから値を取り出す
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getDecompValueAt(int row, int column);
  /**
   * 通常処方の値を取得する。これは処方リンク時のコピー用
   */
  public Vector getNormData();
  /**
   * normDataのサイズを返す
   * @return int
   */
  public int getNormDataSize();
  /**
   * normDataとdata2の組み合わせのクリップボード向けフォーマットにして返す
   */
  public String getNormForCopy(String series, String name);
  /**
   * normDataを印刷向けにフォーマットする
   */
  public Vector getNormForPrint();
  /**
   * 資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
   * @return int
   * @param row int
   */
  public int getNormMatStatus(int row);
  /**
   * @return int
   */
  public int getPcode();
  /**
   * @return java.lang.String
   */
  public String getPerson();
  /**
   * @return double
   */
  public double getPrice();
  /**
   * prepareが全て終了しているかチェック。
   * @return int
   */
  abstract public int getReady();
  /**
   * @return java.lang.String
   */
  public String getReason();
  /**
   * @return double
   */
  public double getSG();
  /**
   * 通常処方データからパーセント値の合計を求める。分解処方が表示されている場合でも、
   * この値は分解処方の合計値と一致することが期待されている
   * @return double
   */
  public double getTotal();
  /**
   * 合計値を文字列化して返す
   * @return java.lang.String
   */
  public String getTotalText();
  /**
   * normDataから値を取り出す
   * @return java.lang.Object
   * @param row int
   * @param column int
   */
  public Object getValueAt(int row, int column);
  /**
   * normDataの指定位置に新規の行を挿入する。指定されたインデックスの行は一つ下にずれる。
   * すなわち新しい行は指定行の前に挿入される
   * @param index int
   */
  public void insertRow(int index);
  /**
   * @return boolean
   */
  public boolean isDecompDataValid();
  /**
   * @return boolean
   */
  public boolean isEditable();
  /**
   * @return boolean
   */
  public boolean isEditing();
  /**
   * 現在のデータがデータベースの様々な制約に違反しないかチェックする
   * チェック項目は以下の通り
   * ・更新理由がnullまたは空文字列でないこと
   * ・全ての資材コードが0以下でないこと
   * @return boolean
   */
  public boolean isLegal();
  /**
   * @return boolean
   */
  public boolean isNewFormula();
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  boolean isPoison();
  /**
   * このモデルが保持する製品単価が正しいか調べるためのメソッド。
   * 作成日 : (00-02-14 14:14:31)
   * @return boolean
   */
  boolean isPriceValid();
  /**
   * 製造コードからデータを検索してロードする。現行処方を検索する場合dateはnullになる
   * 検索できない場合はfalseを返す
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   * @param date java.util.Date
   */
  abstract public boolean load(IQueryClient client, int mode, int pcode, Date date);
  /**
   * 製造コードから分解処方データを検索してロードする。現行処方を検索する場合dateはnullになる
   * 検索できない場合はfalseを返す
   * @param client dap.IQueryClient
   * @param mode int
   * @param pcode int
   */
  abstract public boolean loadDecomp(IQueryClient client, int mode, int pcode);
  /**
   * @param comment java.lang.String
   */
  public void setComment(String comment);
  /**
   * @param date java.util.Date
   */
  public void setDate(Date date);
  /**
   * 編集の開始時にtrueにセットし、終了時にfalseにセットする
   * @param b boolean
   */
  public void setEditing(boolean b);
  /**
   * @param fbc formula.FBrowseViewC
   */
  public void setFBC(FBrowseViewC fbc);
  /**
   * 通常処方の値をセットする。これは処方リンク時のコピー用
   * @param data java.util.Vector
   */
  public void setNormData(Vector data);
  /**
   * 製造コードをセットする。これは処方リンク時のコピー用
   */
  public void setPcode(int pcode);
  /**
   * @param person java.lang.String
   */
  public void setPerson(String person);
  /**
   * @param reason java.lang.String
   */
  public void setReason(String reason);
  /**
   * @param sg double
   */
  public void setSG(double sg);
  /**
   * normDataに値を設定する
   * @param value java.lang.Object
   * @param row int
   * @param column int
   */
  public void setValueAt(Object value, int row, int column);
  /**
   * 処方の更新を実行する
   * @param client dap.IQueryClient
   * @param mode int
   */
  public boolean update(IQueryClient client, int mode);
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  boolean updateAndWait();
  /**
   * 分解処方テーブルを更新する
   */
  public void updateResolvf();
}
