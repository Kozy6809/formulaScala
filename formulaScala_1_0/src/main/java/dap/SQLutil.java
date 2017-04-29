package dap;

import java.util.Vector;
/**
 * SQLのユーティリティ・メソッド集
 */
public class SQLutil {
  /**
   * SQLutil コンストラクター・コメント。
   */
  private SQLutil() {
    super();
  }
  /**
   * "*"と"?"をワイルドカードに使っている時、これを"%"と"_"に変換するメソッド
   * NOTE:
   * Accessはワイルドカードのエスケープに対応していないため、パーセント文字を含む
   * 文字列をまともに検索できないことに注意。逆に、エスケープに対応したdbmsを使用する場合は、
   * SQL文のlike述部にescapeを追加する必要がある。
   * @return java.lang.String
   * @param in java.lang.String
   */
  public static String convWildCard(String in) {
    StringBuffer sb = new StringBuffer(in);
    for (int i=sb.length()-1; i >= 0; i--) {
      char c = sb.charAt(i);
      if (c == '%' || c == '_') {
	// sb.insert(i, '\\');
      } else if (sb.charAt(i) == '?') {
	sb.setCharAt(i, '_');		
      } else if (sb.charAt(i) == '*') {
	sb.setCharAt(i, '%');
      }
    }
    return sb.toString();	
  }
  /**
   * クエリーの戻り値形式のデータ(Object[]を要素とするVector)
   * から単一のデータを取り出す
   * @return java.lang.Object
   * @param row int
   * @param col int
   */
  public static Object get(Vector rs, int row, int col) {
    return ((Object[])rs.elementAt(row))[col];
  }
  /**
   * クエリーの戻り値形式のデータ(Object[]を要素とするVector)
   * から一行のデータを取り出す
   * @return java.lang.Object
   * @param row int
   * @param col int
   */
  public static Object[] getRow(Vector rs, int row) {
    return (Object[])rs.elementAt(row);
  }
  /**
   * クエリーの戻り値形式のデータ(Object[]を要素とするVector)
   * に単一のデータをセットする
   * @return java.lang.Object
   * @param row int
   * @param col int
   */
  public static void set(Vector rs, int row, int col, Object o) {
    ((Object[])rs.elementAt(row))[col] = o;
  }
}
