package dap;

/**
 * JdbcOdbcDriverのバグを回避するため、適切なタイミングでガベージコレクションを行う
 * 適切なタイミングとは、トランザクションが実行中でない時に新たなトランザクションが開始
 * されるタイミング
 * DapまたはDapDirectのbeginTransaction(), dapCommit(), dapRollback()がこのクラスを使用する
 */
public class SyncGc {
  private static int trnsCnt = 0; // 実行中のトランザクションの個数
  /**
   * SyncGc コンストラクター・コメント。
   */
  private SyncGc() {
    super();
  }
  /**
   * トランザクションが開始される時に呼ばれる。既に開始されているトランザクションが無ければ
   * ガベージコレクションを行い、カウンタを1増やす
   */
  public static synchronized void begin() {
    if (trnsCnt == 0) System.gc();
    trnsCnt++;
  }
  /**
   * トランザクションの終了時に呼ばれ、カウンタを1減らす。トランザクションが異常終了した場合
   * でもこのメソッドは呼ばれなければならない
   */
  public static synchronized void end() {
    trnsCnt--;
  }
}
