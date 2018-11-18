package formula;

import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.tree.*;
import formula.ui.*;
import dap.*;
import myutil.*;
/**
 * 処方リンクのコントローラ
 */
public class FormulaLinkC implements IQueryClient, IConsts, IWinControl {
  private static FormulaLinkC flc = null;
  private QueryManager qm;
  private FormulaLinkM flm = null;
  private FormulaLinkV flv = null;
  private FBrowseViewC[] fbs = null; // 処方が更新される処方ブラウザの配列
  private Object updateSrc = null; // 外部オブジェクトによる更新処理の通知用
  private boolean updateFailed = false; // 同上
  private boolean updating = false; // 更新処理を外部に依頼中であることを示す
  private Authenticator auth = null;
  private String person = null; // リンクの更新者
  private NewLinkDlg nld = null;
  private boolean ready = false;
  // topはuserObjectがnullでもisLeaf()はfalseを返すべきなので、FLTreeNodeにはできない
  private DefaultMutableTreeNode top = new DefaultMutableTreeNode();
  private DefaultTreeModel treeModel = new DefaultTreeModel(top);

  // 専用TreeNodeクラス
  private class FLTreeNode extends DefaultMutableTreeNode {
    FLTreeNode() {super();}
    FLTreeNode(Object o) {super(o);}
    // userObjectがNodeGroupならば、常にleafではない
    public boolean isLeaf() {
      Object o = getUserObject();
      return (o == null || o instanceof NodeData) ? true : false;
    }
  }
	
  // FormulaLinkMのデータをラップして、TreeNodeとの橋渡しをするクラス
  private class NodeData {
    private FLTreeNode tn = null;
    private Object[] item; // FormulaLinkMから来るデータ
    NodeData(Object[] item) {this.item = item;}
    void setTreeNode(FLTreeNode tn) {this.tn = tn;}
    Object[] getItem() {return item;}
    int getLinkID() {return ((Integer)item[0]).intValue();}
    int getPcode() {return ((Integer)item[1]).intValue();}
    public String toString() {
      return ((Integer)item[1]).toString() + " " + (String)item[2] + " " + (String)item[3];
    }
    String getSeries() {return (String)item[2];}
    String getName() {return (String)item[3];}
    boolean isLinked(int linkID) {return (getLinkID() == linkID) ? true : false;}
  }

  // リンクグループを表現するTreeNodeのクラス
  private class NodeGroup {
    private FLTreeNode tn = null;
    NodeGroup() {}
    void setTreeNode(FLTreeNode tn) {this.tn = tn;}
    public String toString() {
      if (tn.getChildCount() == 0) return "(empty)";
      String s = "";
      for (Enumeration e=tn.children(); e.hasMoreElements();) {
	FLTreeNode child = (FLTreeNode)e.nextElement();
	NodeData nd = (NodeData)child.getUserObject();
	s += " / " + nd.getSeries() + " " + nd.getName();
      }
      s += " /";
      return s;
    }
    int getLinkID() {
      if (tn.getChildCount() == 0) return -1;
      FLTreeNode n = (FLTreeNode)tn.getChildAt(0);
      return ((NodeData)(n.getUserObject())).getLinkID();
    }
  }
  /**
   * FormulaLinkC コンストラクター・コメント。
   */
  private FormulaLinkC(QueryManager qm) {
    super();
    this.qm = qm;
    flv = new FormulaLinkV(this);
    Main.addWin(this);
    flm = new FormulaLinkM(qm);
    flm.load(this, 0);
  }
  /**
   * リンクを新たに追加する。tpで指定されるパスがグループならばそのグループに、
   * リンクならそのリンクを含むグループに追加される
   *
   * リンクの追加によって処方の変更が生じるので、まず更新権限の設定を行う必要がある。
   * 次いで追加されるリンクと、追加する側とされる側のどちらに処方を合わせるかの指定をする
   * ダイアログを表示する
   *
   * 処方の更新とリンクの更新は同じトランザクションの中で行わなければならない。
   * まず処方の更新を行い、それが失敗した場合はリンクの更新は行わない
   * @param tp javax.swing.tree.TreePath
   */
  public void addLink(TreePath tp) {
    FLTreeNode tn = (FLTreeNode)tp.getLastPathComponent();
    final FLTreeNode parentNode = (tn.isLeaf()) ? (FLTreeNode)tn.getParent() : tn;
    final NodeGroup ng = (NodeGroup)parentNode.getUserObject();
	
    // 更新権限の設定
    if (auth == null) auth = new Authenticator(qm);
    if (!auth.authorize(flv)) return; // dialog canceled
    person = auth.getResult();

    // 追加するリンクを入力させる
    if (nld == null) nld = new NewLinkDlg(flv, this);
    nld.setPerson(person);
    nld.pack();
    nld.setLocationRelativeTo(flv);
    nld.setVisible(true);
    if (nld.isCanceled()) return;

    // 更新処理開始の前に、ビューをロックする
    flv.setLock(true);
    // 更新処理の開始オブジェクト。最初のメソッドが成功したら、順次次のメソッド
    // へと処理が受け渡されていく。それらのメソッドはここで生成されるスレッドで
    // 実行される
    final Object[] newItem = new Object[4];
    newItem[0] = null;
    newItem[1] = new Integer(nld.getPcode());
    newItem[2] = nld.getSeries();
    newItem[3] = nld.getName();
    Runnable transactor = new Runnable() {
      public void run() {
	transact(parentNode, newItem, nld.isMaster());
      }
    };
    Thread t = new Thread(transactor);
    t.start();
  }
  /**
   * 製造コードから品名を取り出し、NewLinkDlgにセットするサービスメソッド
   * @param pcode int
   */
  public void code2name(int pcode) {
    flm.searchName(new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) return;
	Vector v = (Vector)o;
	String series = null;
	String name = null;
	if (v.size() > 0) {
	  Object[] oa = SQLutil.getRow((Vector)o, 0);
	  series = (String)oa[0];
	  name = (String)oa[1];
	}
	final String fSeries = series;
	final String fName = name;
	SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    nld.setNames(fSeries, fName);
	  }
	});
      }
    }, 0, pcode);
  }
  /**
   * データをクリップボードにコピーする。個々のリンクグループは改行で区切る
   * 
   */
  public void copyToClip() {
    String s = "";
    for (Enumeration e = top.preorderEnumeration(); e.hasMoreElements(); ) {
      Object o = e.nextElement();
      if (o == top) continue;
      FLTreeNode tn = (FLTreeNode)o;
      if (tn.isLeaf()) {
	NodeData nd = (NodeData)tn.getUserObject();
	s += nd.toString() + "\n";
      } else {
	s += "\n";
      }
    }
    Clipboard cb = flv.getToolkit().getSystemClipboard();
    StringSelection ss = new StringSelection(s);
    cb.setContents(ss, ss);
  }
  /**
   * TreePathで指定されるノードの製品を削除する。ノードがリンクグループだった場合は
   * グループ全体を削除する
   * @param tp javax.swing.tree.TreePath
   */
  public void delLink(TreePath tp) {
    final FLTreeNode tn = (FLTreeNode)tp.getLastPathComponent();
    Object o = tn.getUserObject();

    IQueryClient client = new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) return;
	treeModel.removeNodeFromParent(tn);
      }
    };
    if (tn.isLeaf()) {
      NodeData nd = (NodeData)o;
      flm.delete(client, 1, nd.getItem());
    }else {
      NodeGroup ng = (NodeGroup)o;
      int linkID = ng.getLinkID();
      if (linkID < 0) { // no child is there
	treeModel.removeNodeFromParent(tn);
      } else flm.delGroup(client, 2, linkID);
    }
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return formula.FormulaLinkC
   * @param qm dap.QueryManager
   */
  public static FormulaLinkC getInstance(QueryManager qm) {
    if (flc == null) flc = new FormulaLinkC(qm);
    return flc;
  }
  /**
   * 与えられた製造コードを含むリンクグループに含まれる処方を返す。
   * 戻り値の各要素はObject[]で、順にlinkID, pcode, series, nameを含む
   * @return java.util.Vector
   * @param pcode int
   */
  public Vector getLinkedFormula(int pcode) {
    return flm.getLinkedFormula(pcode);
  }
  /**
   * 操作が可能になっていればtrueを返す
   * @return boolean
   */
  public boolean isReady() {
    return ready;
  }
  /**
   * 新しいリンクグループのノードを作成する
   */
  public void makeNewGroup() {
    NodeGroup ng = new NodeGroup();
    FLTreeNode tn = new FLTreeNode(ng);
    ng.setTreeNode(tn);
    treeModel.insertNodeInto(tn, top, 0);
    TreePath tp = new TreePath(treeModel.getPathToRoot(tn));
    flv.makePathVisible(tp);
  }
  /**
   * 与えられた製造コードがリンクツリー上に含まれていれば、それが表示されるようにする
   * 含まれていなければfalseを返す
   * @param pcode int
   */
  public boolean makeVisible(int pcode) {
    for (Enumeration e=top.depthFirstEnumeration(); e.hasMoreElements(); ) {
      Object o = e.nextElement();
      if(o == top) continue;
      FLTreeNode tn = (FLTreeNode)o;
      if (tn.isLeaf()) {
	NodeData nd = (NodeData)tn.getUserObject();
	if (nd.getPcode() == pcode) {
	  TreePath tp = new TreePath(treeModel.getPathToRoot(tn));
	  flv.addSelectionPath(tp);
	  flv.makePathVisible(tp);
	  show();
	  return true;
	}
      }
    }
    return false;
  }
  /**
   * mode == 0でFormulaLinkM.load()からの呼び出し
   * @param o java.lang.Object
   * @param mode int
   */
  public void queryCallBack(Object o, int mode) {
    if (mode == SQLERROR) return;
    switch (mode) {
    case 0:
      setNodes();
      ready = true;
      flv.pack();
      break;
    }
  }
  /**
   * 更新処理の実行中はビューがロックされている。この間はクローズリクエストを受付けない
   */
  public boolean requestClose() {
    if (flv.isLocked()) return false;
    flv.setVisible(false);
    return true;
  }
  /**
   * FormulaLinkMがロードしたデータをJTreeのノードにセットする
   */
  private void setNodes() {
    int l0 = -1;
    FLTreeNode n = null;
    NodeGroup ng = null;
    for (Enumeration e=flm.getAllEnum(); e.hasMoreElements(); ) {
      NodeData nd = new NodeData((Object[])e.nextElement());
      if (nd.getLinkID() != l0) {
	ng = new NodeGroup();
	n = new FLTreeNode(ng);
	ng.setTreeNode(n);
	top.add(n);
	l0 = nd.getLinkID();
      }
      FLTreeNode l = new FLTreeNode(nd);
      nd.setTreeNode(l);
      n.add(l);
    }
    flv.setNodes(treeModel);
  }
  /**
   * viewを表示する
   */
  public void show() {
    flv.setVisible(true);
  }
  /**
   */
  private void showSQLErrDlg() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(flv,
				      "データベースに書き込めませんでした　\n\n" +
				      "原因を確認の上やりなおしてください　",
				      "更新に失敗しました",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   */
  private void showUnableToAddDlg(final String name) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	JOptionPane.showMessageDialog(flv,
				      name + "が追加できません　\n" +
				      "既にいずれかのリンクグループに含まれている可能性があります　",
				      "リンク追加エラー",
				      JOptionPane.ERROR_MESSAGE);
      }
    });
  }
  /**
   * リンクへの追加に伴う処方の更新を指示する。pcodeで指定される処方がgroupに追加され、
   * isMasterが真ならgroupがpcodeの処方に揃い、偽ならpcodeがgroupの処方に揃う
   * @param group javax.swing.tree.FLTreeNode
   * @param master javax.swing.tree.FLTreeNode
   */
  private void transact(FLTreeNode group, Object[] item, boolean isMaster) {
    if (flm.isExist(((Integer)item[1]).intValue())) {
      showUnableToAddDlg((String)item[2] + " " + (String)item[3]);
      flv.setLock(false);
      return;
    }
    // トランザクションIDの取得
    int tID = qm.getTransactionID();
    int grpCnt = group.getChildCount();
    if (grpCnt > 0) { // groupにまだ処方が無かった場合には処方の更新は発生しない
      Object[][] newItem = new Object[1][];
      newItem[0] = item;
      Object[][] grpItem = new Object[grpCnt][];
      for (int i=0; i < grpCnt; i++) {
	FLTreeNode tn = (FLTreeNode)group.getChildAt(i);
	grpItem[i] = ((NodeData)tn.getUserObject()).getItem();
      }
      // isMasterの値に応じてgroupの処方または新規追加の処方を更新する
      boolean success = true;
      if (isMaster) success = updateFormula(tID, newItem, grpItem);
      else success = updateFormula(tID, grpItem, newItem);
      if (!success) {
	showSQLErrDlg();
	flv.setLock(false);
	return;
      }
    }
    // リンクテーブルの更新
    if (group.getChildCount() > 0) {
      int id = ((NodeData)(((FLTreeNode)group.getChildAt(0)).getUserObject())).getLinkID();
      item[0] = new Integer(id);
    } else item[0] = null;
    boolean success = flm.chainInsert(tID, item);
    if (!success) {
      showSQLErrDlg();
      flv.setLock(false);
      return;
    }

    // 全て正常に更新された。トランザクションを終了させる
    try {
      qm.commitAndWait(tID);
    } catch (java.sql.SQLException e) {}

    if (fbs != null) {
      // 更新された処方につき、分解処方も更新する
      for (int i=0; i < fbs.length; i++) {
	fbs[i].updateResolvf();
      }
      // 更新された処方ブラウザを表示し、印刷を強制する
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  for (int i=0; i < fbs.length; i++) {
	    //					fbs[i].forceToPrint(true);
	    fbs[i].show();
	  }
	}
      });
    }
	
    // JTreeの更新
    FLTreeNode newNode = new FLTreeNode(new NodeData(item));
    treeModel.insertNodeInto(newNode, group, 0);

    flv.setLock(false);
    if (fbs != null) {
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  JOptionPane.showMessageDialog(flv,
					"リンクへの追加によって処方が更新されました　\n" +
					"忘れずに連絡書を発行してください　",
					"処方リンク",
					JOptionPane.INFORMATION_MESSAGE);
	  fbs = null;
	}
      });
    }
  }
  /**
   * slave[]の製造コードで指示される製品の処方をmaster[0]の製造コードの製品に揃える
   * 全ての更新に成功したらtrue、さもなくばfalseを返す
   * isMasterが真ならgroupがpcodeの処方に揃い、偽ならpcodeがgroupの処方に揃う
   * 更新に失敗した場合、ロールバック処理はFormulaModelの中で行われる
   * @param group javax.swing.tree.FLTreeNode
   * @param master javax.swing.tree.FLTreeNode
   */
  private boolean updateFormula(int tID, final Object[][] master, Object[][] slave) {
    IFormulaModel fm = new FormulaModel(qm);
    final SpinLock sl = new SpinLock(0);
    final boolean[] isErr = new boolean[1];
    isErr[0] = false;
    fm.load(new IQueryClient() {
      public void queryCallBack(Object o, int mode) {
	if (mode == SQLERROR) isErr[0] = true;
	sl.set(1);
      }
    }, 0, ((Integer)master[0][1]).intValue(), null);
    sl.get(1);
    if (isErr[0]) return false;
    Date date = new Date();
    fbs = new FBrowseViewC[slave.length];
    for (int i=0; i < slave.length; i++) {
      int pcode = ((Integer)slave[i][1]).intValue();
      String series = (String)slave[i][2];
      String name = (String)slave[i][3];
      FormulaModel newFm = new FormulaModel(qm);
      newFm.setPcode(pcode);
      newFm.chkNewFormula();
      newFm.copyData(fm);
      newFm.setReason("別製品とリンクしたため");
      newFm.setDate(date);
      newFm.setPerson(person);
      fbs[i] = new FBrowseViewC(qm, newFm, pcode, series, name);
      if (!fbs[i].linkFormula(tID)) return false; // 更新失敗
    }
    return true;
  }
  /**
   * 外部のオブジェクトに依頼した更新処理を通知してもらうメソッド
   * @param pcode int
   */
  public void updateResult(Object updateSrc, boolean updateFailed) {
    this.updateSrc = updateSrc;
    this.updateFailed = updateFailed;
    updating = false;
  }
}
