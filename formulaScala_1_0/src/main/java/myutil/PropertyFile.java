package myutil;

import java.io.*;
import java.util.*;

/**
 * プロパティをファイルから入出力する場合のユーティリティ・クラス。出力については現在未実装
 */
public class PropertyFile {
  /**
   * 指定されたファイルからプロパティを読み取り、Propertiesオブジェクトを返す
   * @param filename ファイル名
   * @return 読み出されたプロパティ。エラーが発生した場合はnull
   */
  public static Properties read(String filename) {
    Properties prp = new Properties();
    try {
      FileInputStream fis = new FileInputStream(filename);
      prp.load(fis);
      fis.close();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return prp;
  }
}
