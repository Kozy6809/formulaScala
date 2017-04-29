package dap;

/**
 * dbとデータをやりとりするデータモデルが実装すべきインターフェイス
 */
import java.util.*;
public interface ITransactor {
  /**
   * リスナー登録
   * @param tl ITransactorListener
   */
  void addListener(ITransactorListener tl);
  /**
 * リスナーを削除
 * @param tl dap.ITransactorListener
 */
  void removeListener(ITransactorListener tl);
  /**
 * データを渡し、やりとりを実行させる
 * @param v Vector
 */
  void retrieve(Vector v);
}
