package myutil;

/**
 * ウィンドウのコントローラが実装するインターフェイス。次の目的に使用する
 * ウィンドウがクローズできるか照会する
 */
public interface IWinControl {
  /**
   * ウィンドウのクローズをリクエストする。クローズできる場合はウィンドウを
   * クローズし、trueを返す。さもなくばfalseを返す
   * @return boolean
   */
  boolean requestClose();
}
