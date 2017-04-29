package formula;

/**
 * ホルベイン製品のシリーズ名を保持するクラス。登録された複数のクライアントに対し、
 * シリーズ名がロードされる度に通知を行う
 */
import java.util.*;
import dap.*;
public class Series implements IQueryClient {
  private static Query q= null;
  private Vector v = null;
  private boolean valid = false;
  private QueryManager qm = null;
  private Vector clients = new Vector();
  /**
   * @param qm dap.QueryManager
   */
  public Series(QueryManager qm) {
    super();
    this.qm = qm;
    q = new Query(qm, "select distinct series from pcode");
  }
  /**
   * クライアントを登録する
   * @param client dap.IQueryClient
   * @param mode int
   */
  public synchronized void addClient(IQueryClient client, int mode) {
    clients.addElement(new Object[] {client, new Integer(mode)});
  }
  /**
   * 既にロードされたデータがあればそれを、無ければnullを返す
   * @return java.util.Vector
   */
  public Vector getResult() {
    return isValid() ? v : null;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  public boolean isValid() {
    return valid;
  }
  /**
   * dbからデータをロードする
   */
  public void load() {
    valid = false;
    q.query(this, 0);
  }
  /**
   * データの読み込みを指示したクライアントがいれば通知を行う
   * @param o java.lang.Object
   * @param callBackMode int
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == 0) {
      v = (Vector)o;
      valid = true;
    }
    for (int i=0, n=clients.size(); i < n; i++) {
      Object[] client = SQLutil.getRow(clients, i);
      ((IQueryClient)client[0]).queryCallBack(o, 
					      (mode < 0) ? mode : ((Integer)client[1]).intValue());
    }
  }
  /**
   * クライアントを削除する
   */
  public synchronized void removeClient(IQueryClient client) {
    for (int i=0, n=clients.size(); i < n; i++) {
      IQueryClient c = (IQueryClient)SQLutil.get(clients, i, 0);
      if (c == client) {
	clients.removeElementAt(i);
	break;
      }
    }
  }
}
