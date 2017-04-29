package myutil;

/**
 * プログラム全体に渡って影響するようなエラーや例外を処理するためのインターフェイス
 */
public interface IGlobalErrorHandler {
  /**
   * このメソッドは VisualAge で作成されました。
   * @param source java.lang.Object
   * @param t java.lang.Throwable
   */
  void globalError(Object source, Throwable t);
}
