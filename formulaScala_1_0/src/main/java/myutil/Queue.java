package myutil;

import java.util.*;
/**
 * 汎用のFIFO。入出力はObjectになる。
 * Queueが空の時にデータを取り出そうとするとnullが返る。データとして格納されたnull
 * かどうか判定するにはgetSize()メソッドを使う
 * 重複したデータの格納を許さないようにすることもできる。デフォルトでは許す。この機能を
 * 有効にするには、格納されるオブジェクトのeqauls()メソッドが適切な値を返す必要がある
 */
public class Queue implements java.io.Serializable {
  private Vector data = new Vector();
  private boolean allowDupValue = true;
  /**
   * Queue コンストラクター・コメント。
   */
  public Queue() {
    this(true);
  }
  /**
   * Queue コンストラクター・コメント。
   */
  public Queue(boolean allowDupValue) {
    super();
    this.allowDupValue = allowDupValue;
  }
  /**
   * データを取り出す。空の場合はnullを返す
   * @return java.lang.Object
   */
  public synchronized Object get() {
    if (data.size() == 0) return null;
    Object o = data.firstElement();
    data.removeElementAt(0);
    return o;
  }
  /**
   * @return int
   */
  public synchronized int getSize() {
    return data.size();
  }
  /**
   * データを覗き見する。空の場合はnullを返す
   * @return java.lang.Object
   */
  public synchronized Object peek() {
    if (data.size() == 0) return null;
    Object o = data.firstElement();
    return o;
  }
  /**
   * データを格納する
   * @param o java.lang.Object
   */
  public synchronized void put(Object o) {
    if (!allowDupValue && data.indexOf(o) > 0) return;
    data.addElement(o);
  }
  /**
   * @param b boolean
   */
  public void setAllowDupValue(boolean b) {
    allowDupValue = b;
  }
}
