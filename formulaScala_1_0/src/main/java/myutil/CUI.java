package myutil;

import java.io.*;
import java.util.*;
/**
 * CUIにおいてユーザーと対話するためのクラス。標準出力にメッセージを表示し、
 * それに対する応答を受け取るスタティックメソッドを実装する
 */
public class CUI {
  static BufferedReader br;
  static {
    br = new BufferedReader(new InputStreamReader(System.in));
  }

  /**
   * promptを表示し、応答文字列を返す
   * @return 応答文字列。エラーがあればnull
   * @param prompt 表示されるプロンプト
   */
  public static String getRes(String prompt) {
    System.out.print(prompt);
    String s = null;
    try {
      s = br.readLine();
      return s;
    } catch (IOException e) {
      System.err.println("can't read user response!");
      return null;
    }
  }

  /**
   * promptを表示し、応答をトークンに分割して返す。区切り文字はStringTokenizerのデフォルト値
   * @return トークンの並び。エラーがあればnull
   * @param prompt 表示されるプロンプト
   */
  public static List getMultiRes(String prompt) {
    String s = getRes(prompt);
    if (s == null) return null;
    StringTokenizer st = new StringTokenizer(s);
    List l = new ArrayList();
    while (st.hasMoreTokens()) {
      l.add(st.nextElement());
    }
    return l;
  }
}
