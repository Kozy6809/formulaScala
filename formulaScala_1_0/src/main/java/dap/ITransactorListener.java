package dap;

/**
 * ITransactorのリスナーが実装するインターフェイス
 */
public interface ITransactorListener {
  /**
   * dbとのデータのやりとりの終了時に呼び出されるメソッド
   * @param t dap.ITransactor
   * @param result java.lang.Object
   */
  void notify(ITransactor t, Object result);
}
