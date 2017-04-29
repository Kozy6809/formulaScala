package myutil;

/**
 * スレッドを同期させるためのスピンロック
 */
public class SpinLock {
  private int i = 0;
  /**
   * SpinLock コンストラクター・コメント。
   */
  public SpinLock() {
    super();
  }
  /**
   * SpinLock コンストラクター・コメント。
   */
  public SpinLock(int i) {
    super();
    this.i = i;
  }
  /**
   * オブジェクト内部の値が引数に一致するまでブロックする。
   * @return int
   */
  public synchronized void get(int i) {
    while (this.i != i) {
      try {
	wait();
      } catch (InterruptedException e) {}
    }
  }
  /**
   * オブジェクトの値をiとする
   * @param i int
   */
  public synchronized void set(int i) {
    this.i = i;
    notifyAll();
  }
}
