package formula.ui;

import formula.*;
import java.awt.*;
/**
 * 起動時のスプラッシュ画面。ついでに最近の更新点を表示する
 * 作成日 : (00-02-17 15:21:46)
 * @author: 
 */
public class Splash extends Frame {
  private static String updates = "2020/3/26 処方連絡書にラベル仕様変更のチェック欄を追加しました";

  /**
   * Splash コンストラクター・コメント。
   */
  public Splash() {
    super();
    init();
  }
  /**
   * Splash コンストラクター・コメント。
   * @param title java.lang.String
   */
  public Splash(String title) {
    super(title);
  }
  /**
   * 表示する画面の初期化
   * 作成日 : (00-02-17 15:24:09)
   */
  private void init() {
    setLayout(new BorderLayout());
    Label l = new Label("サーバーに接続しています…");
    l.setAlignment(Label.CENTER);
    l.setFont(new Font("dialog", 0, 18));
    add(l, "South");
    l = new Label(updates);
    l.setAlignment(Label.CENTER);
    l.setFont(new Font("dialog", 0, 18));
    add(l, "Center");
    setSize(570, 330);
    setTitle("処方データベースを起動しています");
  }
}
