package formula.java;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import formula.ui.*;
import dap.*;
/**
 * 資材名と資材コードをマッチさせる
 * 資材名で検索をかける場合、複数が該当するならばダイアログを表示してユーザーに選ばせる
 */
public class MatDeterminer implements IQueryClient, IConsts {
  private JFrame parent;
  private MatDeterminDialog mdd = null;
  private QueryManager qm;
  private MaterialResolver mr;

  private Vector resultData = null;
  private class ResultListModel extends AbstractListModel<Object> {
    public Object getElementAt(int index) {
      if (resultData == null) return null;
      String mcode = ((Integer)SQLutil.get(resultData, index, 0)).toString();
      String mname = (String)SQLutil.get(resultData, index, 1);
      return mcode + " " + mname;
    }
    public int getSize() {
      return (resultData == null) ? 0 : resultData.size();
    }
    public void fireContentsChanged(Object source, int index0, int index1) {
      super.fireContentsChanged(source, index0, index1);
    }
  }
  private ResultListModel rlm = new ResultListModel();

  private MatDeterminListener mdl = null;
  private Component dialogLocator = null;
  /**
   * MatDeterminer コンストラクター・コメント。
   */
  public MatDeterminer(QueryManager qm, JFrame parent) {
    super();
    this.qm = qm;
    this.parent = parent;
    mr = new MaterialResolver(qm);
  }
  /**
   */
  private MatDeterminDialog getMdd() {
    if (mdd == null) {
      try {
	mdd = new MatDeterminDialog(parent, "複数の候補があります", true);
	mdd.getList().setModel(rlm);
      } catch (Exception e) {e.printStackTrace();}
    }
    return mdd;
  }
  /**
   * MaterialResolverから検索結果を受け取る。
   * 結果が0行ならクライアントにcode=-1,name=null,status=-1を返す
   * 結果が1行ならそれを返す
   * 結果が複数ならダイアログを表示し、一つを選択させる
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
    boolean canceled = false;
    int index = 0;
    if (mode == SQLERROR) return;
    resultData = (Vector)o;
    switch (resultData.size()) {
    case 0:
      mdl.matDetermined(false, -1, null, -1);
      return;
    case 1:
      break;
    default:
      rlm.fireContentsChanged(rlm, 0, resultData.size()-1);
      try {
	SwingUtilities.invokeAndWait(new Runnable() {
	  public void run() {
	    getMdd().pack();
	    if (dialogLocator != null) getMdd().setLocationRelativeTo(dialogLocator);
	    getMdd().setVisible(true);
	  }
	});
      } catch (Exception e) {e.printStackTrace();}
      index = getMdd().getList().getSelectedIndex();
      canceled = getMdd().isCanceled();
      break;
    }
    if (canceled || index < 0) return;
    Object[] oa = SQLutil.getRow(resultData, index);
    mdl.matDetermined(canceled, ((Integer)oa[0]).intValue(), (String)oa[1], ((Integer)oa[2]).intValue());
  }
  /**
   * 資材コードによる検索を実行する
   * @param client formula.MatDeterminListener
   * @param code int
   */
  public void searchByCode(MatDeterminListener client, int code) {
    mdl = client;
    mr.resolvByCode(this, 1, code);
  }
  /**
   * 資材名による検索を実行する
   * @param client formula.MatDeterminListener
   * @param code int
   */
  public void searchByName(MatDeterminListener client, String name) {
    mdl = client;
    mr.resolvByName(this, 2, name);
  }
  /**
   * ダイアログの表示位置を指定するコンポーネントをセットする
   * @param locator java.awt.Component
   */
  public void setDialogLocator(Component locator) {
    dialogLocator = locator;
  }
}
