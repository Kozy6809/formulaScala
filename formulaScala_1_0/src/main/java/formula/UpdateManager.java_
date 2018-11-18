package formula;

import java.io.*;
import java.util.*;
import dap.*;
import myutil.*;
import myutil.Queue;
import formula.ui.*;
/**
 * 処方の更新を管理・統括するクラス。処方の更新は場合によっては大規模なトランザクション
 * になる可能性があるため、エラー中断・再開等の機能が必要になる。このクラスは以下の
 * 処理を実装する
 * 更新リクエストの受付(リンク更新、資材の一括更新)
 * 更新される処方のリストアップ
 * 処方の更新
 * 更新のロギング
 * エラー処理
 * エラーからの再開
 * 更新エラーの状態でプログラムが終了した場合、このクラスは再開に必要な情報をファイルに
 * 保存する。プログラムが開始する時には、エラーからの再開ができるようにこのクラスに処理を
 * させるべきである
 */
public class UpdateManager implements Runnable {
  private QueryManager qm;
  private Decomposer d;
  private FormulaLinkM flm;
  // 更新リクエストを入れるためのキュー
  // normQには更新データを格納したIFormulaModelが入る
  // decompQには分解処方を計算すべき処方の製造コードを格納したIntegerが入る
  private Queue normQ = new Queue(false); // 重複を許さないQueue
  private Queue decompQ = new Queue(false); // 同上
  private boolean quitRequested = false;
  private boolean quitGranted = false;
  private static String logName = "formula.log"; // ログファイル名。QueryManagerが
  // 使用するエラーログファイルと共用
  private static String resumeName = "formulaResume.dat"; // レジュームデータファイル名
  private LogManager lm = LogManager.getInstance();
  private static int retryCount = 0; // 更新に失敗した時のリトライ回数
  private boolean errored = false; // エラーが発生したら新たな更新リクエストは受付けない
  /**
   * UpdateManager コンストラクター・コメント。
   */
  public UpdateManager(QueryManager qm) {
    this.qm = qm;
    d = new Decomposer(qm);
    flm = new FormulaLinkM(qm);
  }
  /**
   * レジュームファイルが存在するならば、データを読みだしてキューにセットし、
   * 更新処理を再開する
   * レジュームファイルのフォーマットはsuspend()を参照
   */
  public synchronized void resume() {
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(resumeName));
    } catch (IOException e) {
      return; // レジュームファイルが存在しない
    }
    try {
      int n = ois.readInt();
      for (int i=0; i < n; i++) {
	int pcode = ois.readInt();
	Object[] data2 = (Object[])ois.readObject();
	Vector normData = (Vector)ois.readObject();
	FormulaModel fm = new FormulaModel(qm, pcode, data2, normData);
	normQ.put(fm);
      }
      n = ois.readInt();
      for (int i=0; i < n; i++) {
	int pcode = ois.readInt();
	decompQ.put(new Integer(pcode));
      }
      ois.close();
    } catch (Exception e) {
      lm.write(logName, "レジュームファイルが正しく読み込めませんでした");
      return;
    }
    try {
      new File(resumeName).delete();
    } catch (Exception e) {
      lm.write(logName, "レジュームファイルが削除できませんでした");
    }
	
    notifyAll(); // バックグランドスレッドを動かす
  }
  /**
   * 処方の更新をバックグランドのスレッドで実行するためのメソッド。キューからの取り出しを
   * peek()で行っているのは、エラーが発生した場合にエラーを起こしたものもキューに残しておいて
   * ファイルに書き出す必要があるため
   */
  public void run() {
    for (;;) {
      waitRequest();
      if (normQ.getSize() > 0) {
	boolean rc = updateNorm((IFormulaModel)normQ.peek());
	if (rc) normQ.get(); // 処理の終わったものをキューから取り除く
	else return; // エラー終了
      } else if (decompQ.getSize() > 0) {
	boolean rc = updateDecomp(((Integer)decompQ.get()).intValue());
	if (rc) decompQ.get(); // 処理の終わったものをキューから取り除く
	else return; // エラー終了
      }
		
      if (quitRequested && normQ.getSize() == 0 && decompQ.getSize() == 0) {
	quitGranted = true;
	return;
      }
      Thread.yield(); // 確実に低優先度で動作するように
    }
  }
  /**
   * エラーによる更新処理の中断。キュー内のリクエストをファイルに書き出す
   * 書き出されるファイルのフォーマットは次のようにする
   * 未処理の通常処方の個数(int)
   * 未処理の処方の製造コード(int)
   * FormulaModel.getData2() (Object[])
   * FormulaModel.getNormData() (Vector)
   * 上記2つを個数分繰り返し
   * 未処理の分解処方の個数(int)
   * 未処理の処方の製造コード(int)
   * 上記を個数分繰り返し
   */
  public synchronized void suspend() {
    errored = true;
    try {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resumeName));
      int n = normQ.getSize();
      oos.writeInt(n);
      for (int i=0; i < n; i++) {
	IFormulaModel fm = (IFormulaModel)normQ.get();
	oos.writeInt(fm.getPcode());
	oos.writeObject(fm.getData2());
	oos.writeObject(fm.getNormData());
      }
      n = decompQ.getSize();
      for (int i=0; i < n; i++) {
	oos.writeInt(((Integer)decompQ.get()).intValue());
      }
      oos.close();
    } catch (IOException e) {
      lm.write(logName, "レジュームファイルが作成できませんでした");
    }
  }
  /**
   * 与えられた分解処方を更新する。更新に失敗した場合はエラー中断処理を行う
   * @param pcode int
   */
  private boolean updateDecomp(int pcode) {
    Vector dependants = d.listDependants(new Integer(pcode));
    if (dependants == null) {
      lm.write(logName, pcode + "の分解に失敗しました。更新処理は中断されました");
      suspend();
    }
    for (int i=0, n=dependants.size(); i < n; i++) {
      Object o = ((Object[])dependants.elementAt(i))[0];
      decompQ.put(o);
    }
    int n = retryCount + 1;
    for (int i=0; i < n; i++) {
      boolean rc = d.updateSingle(pcode);
      if (rc) {
	lm.write(logName, pcode + "は正常に分解されました");
	return true;
      }
      lm.write(logName, pcode + "の分解エラーです");
    }
    lm.write(logName, pcode + "の分解に失敗しました。更新処理は中断されました");
    suspend();
    return false;
  }
  /**
   * 資材の一括更新を受付けるエントリ・メソッド。更新される処方のリストを返す。エラーが
   * 起きた場合はnullを返す。この辺はupdateSingleFormulaと同等
   * @return java.util.Vector
   * @param formulas int[] 更新される処方の製造コードのリスト
   * @param oldMat int 更新される資材の資材コード
   * @param newMat int 新しい資材の資材コード
   */
  public Vector updateMatGlobally(int[] formulas, int oldMat, int newMat) {
    // 製造コードのリストからFormulaModelを生成し、データをロードする
    Vector fms = new Vector();
    for (int i=0; i < formulas.length; i++) {
      FormulaModel fm = new FormulaModel(qm);
      boolean rc = fm.loadAndWait(formulas[i], null);
      if (!rc) return null;
      fms.addElement(fm);
    }
    return null;
  }
  /**
   * 与えられた処方を更新する。更新に失敗した場合はエラー中断処理を行う
   * @param fm formula.IFormulaModel
   */
  private boolean updateNorm(IFormulaModel fm) {
    int pcode = fm.getPcode();
    int n = retryCount + 1;
    for (int i=0; i < n; i++) {
      boolean rc = fm.updateAndWait();
      if (rc) {
	lm.write(logName, pcode + "は正常に更新されました");
	return true;
      }
      lm.write(logName, pcode + "の更新エラーです");
    }
    lm.write(logName, pcode + "の更新に失敗しました。更新処理は中断されました");
    suspend();
    return false;
  }
  /**
   * 単一処方の更新リクエストを受付けるエントリ・メソッド。リンク処方も更新される。
   * 戻り値として、リンク処方のリストを返す。もしリンクしていなければ、サイズが0行のリストが
   * 返ることになる。
   * エラーが発生した場合はnullが返る。その場合更新は行われない
   * リストの各行の並び順はlinkID, pcode, series, name
   * @param fm formula.IFormulaModel
   */
  public Vector updateSingleFormula(IFormulaModel fm) {
    int pcode = fm.getPcode();
    Vector linked = flm.getLinkedFormula(pcode);
    if (linked == null) return null;
    normQ.put(fm);
    Object[] data2 = fm.getData2();
    Vector normData = fm.getNormData();
    for (int i=0, n=linked.size(); i < n; i++) {
      int lPcode = ((Integer)SQLutil.get(linked, i, 1)).intValue();
      normQ.put(new FormulaModel(qm, lPcode, data2, normData));
    }
    synchronized (this) {
      notifyAll();
    }
    return linked;
  }
  /**
   * キューにリクエストが到着するのを待つ
   */
  private synchronized void waitRequest() {
    while (normQ.getSize() == 0 && decompQ.getSize() == 0) {
      try {
	wait();
      } catch (InterruptedException e) {}
    }
  }
}
