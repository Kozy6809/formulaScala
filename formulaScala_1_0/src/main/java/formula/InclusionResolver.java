package formula;

/**
 * 指定された資材コードの資材を含む製品名と製造コード、含有比率の組を保持する。
 * 検索条件は通常処方に対してと分解処方に対しての両方を扱う
 * 更に限定修飾子としてシリーズ名のリストを受付ける
 */
import java.util.*;
import dap.*;
public class InclusionResolver implements IQueryClient, IConsts {
  private static int ready = 0; // prepare完了で1、未了で0、失敗で-1
  private static PQuery allNormal = null; // 全品種の通常処方を検索
  private static PQuery seriesNormal = null; // シリーズ限定で通常処方を検索
  private static PQuery allSolved = null; // 全品種の分解処方を検索
  private static PQuery seriesSolved = null; // シリーズ検定で分解処方を検索
  private QueryManager qm;
  private Object[] seriesList = null;
  private Vector data = null;
  private boolean valid = false; // データが正しい検索結果かどうか示す
  private IQueryClient client = null;
  private int clientMode = 0;
  // シリーズ名を含む検索で全てのシリーズ名をカウントする
  private int seriesCount = 0;
  public InclusionResolver(QueryManager qm) {
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
    for (; getReady() == 0; ) {
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
    int i1 = allNormal.getReady();
    int i2 = seriesNormal.getReady();
    int i3 = allSolved.getReady();
    int i4 = seriesSolved.getReady();

    if (i1 < 0 || i2 < 0 || i3 < 0 || i4 < 0) {
      ready = -1;
      return ready;
    }
    if (i1 > 0 && i2 > 0 && i3 > 0 && i4 > 0) ready = 1;
    return ready;
  }
  /**
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
      data = (Vector)o;
      valid = true;
      client.queryCallBack(o, clientMode);
      break;
    case 2:
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
   * 検索を行う。fromAllSeriesがtrueなら全品種から、
   * さもなくばseriesListを参照してシリーズ毎の検索を繰り返す。
   * fromNormalがtrueなら通常処方、さもなくば分解処方を検索する
   * @return boolean
   * @param client dap.IQueryClient
   * @param mode int
   * @param code int
   * @param fromAllSeries boolean
   * @param fromNormal boolean
   */
  public boolean
    resolv(IQueryClient client, int mode, int code, boolean fromAllSeries, boolean fromNormal) {
    if (!chkReady()) return false;
    this.client = client;
    this.clientMode = mode;
    if (fromAllSeries) {
      valid = false;
      Object[] p = {new Integer(code)};
      PQuery q = (fromNormal) ? allNormal : allSolved;
      q.query(this, 1, p);
    } else {
      if (seriesList == null) return false;
      seriesCount = seriesList.length;
      PQuery q = (fromNormal) ? seriesNormal : seriesSolved;
      data = new Vector();
      for (int i=0; i < seriesList.length; i++) {
	Object[] p = new Object[2];
	p[0] = (String)seriesList[i];
	p[1] = new Integer(code);
	q.query(this, 2, p);
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
    if (allNormal == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      allNormal = new PQuery
	(qm, "select p.pcode, series, name, sum(percent), obsolete from pcode p, form1 f " +
	 "where p.pcode = f.pcode and mcode = ? " +
	 "group by p.pcode, series, name, obsolete order by sum(percent) desc", INtypes);
      allNormal.prepare();
    }

    if (seriesNormal == null) {
      int[] INtypes = new int[2];
      INtypes[0] = STRING;
      INtypes[1] = INT;
      seriesNormal = new PQuery
	(qm, "select p.pcode, series, name, sum(percent), obsolete from pcode p, form1 f " +
	 "where p.pcode = f.pcode and series = ? and mcode = ? " +
	 "group by p.pcode, series, name, obsolete order by sum(percent) desc", INtypes);
      seriesNormal.prepare();
    }

    if (allSolved == null) {
      int[] INtypes = new int[1];
      INtypes[0] = INT;
      allSolved = new PQuery
	(qm, "select p.pcode, series, name, percent, obsolete from pcode p, resolvf f " +
	 "where p.pcode = f.pcode and mcode = ? " +
	 "order by percent desc", INtypes);
      allSolved.prepare();
    }

    if (seriesSolved == null) {
      int[] INtypes = new int[2];
      INtypes[0] = STRING;
      INtypes[1] = INT;
      seriesSolved = new PQuery
	(qm, "select p.pcode, series, name, percent, obsolete from pcode p, resolvf f " +
	 "where p.pcode = f.pcode and series = ? and mcode = ? " +
	 "order by percent desc", INtypes);
      seriesSolved.prepare();
    }
  }
}
