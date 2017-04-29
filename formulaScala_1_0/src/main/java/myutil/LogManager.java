package myutil;

import java.io.*;
import java.util.*;
/**
 * ログファイルへの書き出しを統括するクラス
 * このクラスのインスタンスはgetInstanceメソッドで取得する
 * ログを書き込むにはログファイル名とテキストをwrite()メソッドに渡す
 * write()メソッドは書き込みごとにバッファをフラッシュする
 * ログテキストには先頭に時刻が付加される
 * exit()メソッドでLogManagerは処理を終了し、全てのログファイルをクローズする
 */
public class LogManager {
  private static LogManager me = null;
  private Hashtable logs = new Hashtable();
  /**
   * LogManager コンストラクター・コメント。
   */
  private LogManager() {
    super();
  }
  /**
   * 処理を終了する。全てのログファイルをクローズする
   */
  public void exit() {
    for(Enumeration e = logs.elements(); e.hasMoreElements(); ) {
      try {
	((FileWriter)e.nextElement()).close();
      } catch (IOException ee) {}
    }
    logs = null;
    me = null;
  }
  /**
   * @return myutil.LogManager
   */
  public synchronized static LogManager getInstance() {
    if (me == null) me = new LogManager();
    return me;
  }
  /**
   * 指定されたファイルにメッセージを書き出す
   * エラーが発生した場合はfalseを返す
   * @param fName java.lang.String ファイル名
   * @param mes java.lang.String メッセージ
   */
  public synchronized boolean write(String fName, String mes) {
    FileWriter fw = (FileWriter)logs.get(fName);
    try {
      if (fw == null) {
	fw = new FileWriter(fName, true);
	logs.put(fName, fw);
      }
      fw.write(new Date() + "\t" + mes + "\n");
      fw.flush();
      return true;
    } catch (IOException e) {}
    return false;
  }
}
