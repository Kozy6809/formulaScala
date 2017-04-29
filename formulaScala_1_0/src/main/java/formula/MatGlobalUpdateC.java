package formula;

import java.util.*;
import dap.*;
import formula.ui.*;

/**
 * 原料の一括更新処理を行う。
 */
public class MatGlobalUpdateC {
  private QueryManager qm;
  private Vector formList = null; // 更新される処方のリスト
  private MatGlobalUpdateV mguv = null;
  private int org = 0; // 更新前の資材コード
  private int alt = 0; // 更新後の資材コード
  private String orgName = null; // 更新前の資材記号
  private String altName = null; // 更新後の資材記号
  /**
   * MatGlobalUpdateC コンストラクター・コメント。
   */
  public MatGlobalUpdateC(QueryManager qm) {
    super();
    this.qm = qm;
    mguv = new MatGlobalUpdateV(this);
  }
  /**
   */
  public void show() {
    if (mguv != null) mguv.setVisible(true);
  }
}
