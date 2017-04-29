package dap;

/**
 * クエリーの結果を受け取るオブジェクトが実装すべき
 * コールバック関数を指定するインターフェース
 */
public interface IQueryClient {
  /**
   * SQLの実行結果を通知するコールバックメソッド
   * mode - このメソッドを実装したオブジェクトがあらかじめ指定した動作モード。ただし
   * エラーが発生した場合はIConsts.SQLERRORが返る。そのため必ずチェックする要あり
   *
   * o - 実行したSQL文に応じてVectorやintが返る
   */
  void queryCallBack(Object o, int mode);
}
