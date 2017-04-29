package formula;

/**
 * 外部から与えられた条件にマッチする製品名と製造コードの組を保持する。
 * 受け入れる条件は 1)製造コード 2)製品名(ワイルドカード含む)のいずれかで、
 * 更に限定修飾子としてシリーズ名のリストを受付ける
 */
import java.util.*;
import dap.*;
public class ProductResolver implements IQueryClient, IConsts {
  private static int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private static PQuery allName2code = null; // 全品種の名前から製造コード
  private static PQuery name2code = null; // 名前から製造コード
  private static PQuery code2name = null; // 製造コードから名前
  private QueryManager qm;
  private Object[] seriesList = null;
  private Vector data = null;
  private boolean valid = false; // データが正しい検索結果かどうか示す
  private IQueryClient client = null;
  private int clientMode = 0;
  // シリーズ名を含む検索で全てのシリーズ名をカウントする
  private int seriesCount = 0;
  /**
   * ProductResolver コンストラクター・コメント。
   */
  public ProductResolver(QueryManager qm) {
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
    int i1 = allName2code.getReady();
    int i2 = name2code.getReady();
    int i3 = code2name.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0) ready = 1;
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
    case 3:
      Vector d = (Vector)o;
      for (int i=0, n=d.size(); i < n; i++) {
	data.addElement(d.elementAt(i));
      }
      if (--seriesCount == 0) {
	valid = true;
	client.queryCallBack(data, clientMode);
	break;
      }
    default :
    }
  }
  /**
   * 製造コードで検索を行い、結果をIQueryClientに通知する。
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
   * 製品名から検索を行う。fromAllSeriesがtrueなら全品種から、
   * さもなくばseriesListを参照してシリーズ毎の検索を繰り返す。
   * @return boolean
   * @param client dap.IQueryClient
   * @param mode int
   * @param name String
   * @param fromAllSeries boolean
   */
  public boolean
    resolvByName(IQueryClient client, int mode, String name, boolean fromAllSeries) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    String cName = SQLutil.convWildCard(name);
    if (fromAllSeries) {
      valid = false;
      Object[] p = {cName};
      allName2code.query(this, 2, p);
    } else {
      if (seriesList == null) return false;
      seriesCount = seriesList.length;
      data = new Vector();
      for (int i=0; i < seriesList.length; i++) {
	Object[] p = new Object[2];
	p[0] = (String)seriesList[i];
	p[1] = cName;
	name2code.query(this, 3, p);
      }
    }
    return true;
  }
  /**
   * シリーズ名リストを受け取る
   * @param series java.util.Vector
   */
  public void setSeries(Object[] series) {
    seriesList = series;
  }
  /**
   * SQL文をprepareする。結果は非同期で判明するので、このメソッドが複数回呼ばれても
   * Singletonパターンでただ一度だけprepareされるようにする。
   */
  private synchronized void startPrepare() {
    if (allName2code == null) {
      int[] INtypes = new int[1];
      INtypes[0] = STRING;
      allName2code = new PQuery
	(qm, "select pcode, series, name, obsolete from pcode where name like ?", INtypes);
      allName2code.prepare();
    }

    if (name2code == null) {	
      int[] INtypes = new int[2];
      INtypes[0] = STRING;
      INtypes[1] = STRING;
      name2code = new PQuery
	(qm, "select pcode, series, name, obsolete from pcode where series = ? and name like ?", INtypes);
      name2code.prepare();
    }

    if (code2name == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      code2name = new PQuery
	(qm, "select pcode, series, name, obsolete from pcode where pcode = ?", INtypes);
      code2name.prepare();
    }
  }
}
