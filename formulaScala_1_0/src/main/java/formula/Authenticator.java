package formula;

import dap.*;
import formula.ui.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
/**
 * 処方更新権限の認証を行う
 */
public class Authenticator {
  private QueryManager qm;
  private Frame parent;
  private static Query getAuthPerson = null;
  private Vector authPerson = null;
  private String selectedPerson = null;
  private AuthenticationDialog ad = null;
  private class APListModel extends AbstractListModel<String> {
    public String getElementAt(int index) {
      if (authPerson == null) return null;
      return (String)SQLutil.get(authPerson, index, 0);
    }
    public int getSize() {
      return (authPerson == null) ? 0 : authPerson.size();
    }
    public void fireContentsChanged(Object source, int index0, int index1) {
      super.fireContentsChanged(source, index0, index1);
    }
  }
  APListModel aplm = new APListModel();
  /**
   * Authenticator コンストラクター・コメント。
   */
  public Authenticator(QueryManager qm) {
    super();
    this.qm = qm;
    getAuthPerson = new Query(qm, "select person from authorize order by kana");
    getAuthPerson.query(new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == IConsts.SQLERROR) return;
	authPerson = (Vector)o;
	aplm.fireContentsChanged(aplm, 0, authPerson.size());
      }
    }, 0);
  }
  /**
   * 認証を実行する。ダイアログがキャンセルされればfalse、さもなくばtrueを返す
   * @return boolean
   */
  public boolean  authorize(Frame parent) {
    ad = new AuthenticationDialog(parent, true);
    ad.getList().setModel(aplm);
    ad.pack();
    ad.setLocationRelativeTo(parent);
    ad.setVisible(true);
    if (!ad.isCanceled()) selectedPerson = (String)ad.getList().getSelectedValue();
    return !ad.isCanceled();
  }
  /**
   * @return java.lang.String
   */
  public String getResult() {
    return selectedPerson;
  }
}
