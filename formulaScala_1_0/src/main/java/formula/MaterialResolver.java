package formula;

/**
 * 外部から与えられた条件にマッチする資材名と資材コードの組を保持する。
 * 受け入れる条件は 1)資材コード 2)資材名(ワイルドカード含む)のいずれか
 */
import java.util.*;
import dap.*;
public class MaterialResolver implements IQueryClient, IConsts {
  private static int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private static PQuery name2code = null; // 名前から資材コード
  private static PQuery code2name = null; // 資材コードから名前
  private QueryManager qm;
  private Vector data = null;
  private boolean valid = false; // データが正しい検索結果かどうか示す
  private IQueryClient client = null;
  private int clientMode = 0;
  /**
   * ProductResolver コンストラクター・コメント。
   */
  public MaterialResolver(QueryManager qm) {
    super();
    this.qm = qm;
    startPrepare();
  }
  /**
   * このオブジェクトの準備(即ち全てのprepareの完了)ができているかチェックし、
   * 未了ならば完了を待ってtrueを返す。もしエラーになればfalseを返す。
   * @return boolean
   */
  public boolean chkReady() {
    for (; getReady() == 0;) {
      Thread.yield();
    }
    return (getReady() > 0) ? true : false;
  }
  /**
   * prepareが全て終了しているかチェック。
   * @return int
   */
  public int getReady() {
    if (ready != 0) return ready;
    int i1 = name2code.getReady();
    int i2 = code2name.getReady();

    if (i1 < 0 || i2 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0) ready = 1;
    return ready;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  public boolean isValid() {
    return valid;
  }
  /**
   * queryCallBack メソッド・コメント。
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) {
      valid = false;
      client.queryCallBack(o, mode);
      return;
    }
    switch (mode) {
    case 1:
    case 2:
      data = (Vector)o;
      valid = true;
      client.queryCallBack(o, clientMode);
      break;
    default :
    }
  }
  /**
   * 資材コードで検索を行い、結果をIQueryClientに通知する。
   * 検索が実行できない場合はfalseを返す。
   * @return boolean
   * @param code int
   */
  public boolean resolvByCode(IQueryClient client, int mode, int code) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    valid = false;
    Object[] p = {new Integer(code)};
    code2name.query(this, 1, p);
    return true;
  }
  /**
   * 資材名から検索を行う。
   * @return boolean
   * @param client dap.IQueryClient
   * @param mode int
   * @param name String
   */
  public boolean resolvByName(IQueryClient client, int mode, String name) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    String cName = SQLutil.convWildCard(name);
    valid = false;
    Object[] p = {cName};
    name2code.query(this, 2, p);
    return true;
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (name2code == null) {
      int[] INtypes = new int[1];
      INtypes[0] = STRING;
      name2code = new PQuery
	(qm, "select mcode, mname, status from mcode where mname like ? order by mcode", INtypes);
      name2code.prepare();
    }

    if (code2name == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      code2name = new PQuery
	(qm, "select mcode, mname,status from mcode where mcode = ? order by mcode", INtypes);
      code2name.prepare();
    }
  }
}
