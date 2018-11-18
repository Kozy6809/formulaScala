package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import formula.*;
/**
 * 処方リンクのビュー
 */
public class FormulaLinkV extends JFrame {
  private FormulaLinkC flc;
  private JTree tree = new JTree();
  private boolean locked = false; // 更新処理の間ビューをロックするためのフラグ
  /**
   * FormulaLinkV コンストラクター・コメント。
   */
  public FormulaLinkV() {
    super("処方リンク");
    init();
  }
  /**
   * FormulaLinkV コンストラクター・コメント。
   */
  public FormulaLinkV(FormulaLinkC flc) {
    super("処方リンク");
    this.flc = flc;
    init();
  }
  /**
   * 指定された行が表示され、選択状態になるようにする
   * @param row int
   */
  public void addSelectionPath(TreePath path) {
    tree.addSelectionPath(path);
  }
  /**
   * ビューの初期化
   */
  private void init() {
    JMenuBar mb = new JMenuBar();
    setJMenuBar(mb);
    JMenu m = new JMenu("編集(E)");
    m.setMnemonic('E');
    mb.add(m);
    JMenuItem mi = new JMenuItem("新規リンクグループ(G)");
    mi.setMnemonic('G');
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (locked) return;
	flc.makeNewGroup();
      }
    });
    m.add(mi);
    mi = new JMenuItem("追加(A)");
    mi.setMnemonic('A');
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (locked) return;
	TreePath tp = tree.getSelectionPath();
	if (tp != null) flc.addLink(tp);
      }
    });
    m.add(mi);
    mi = new JMenuItem("削除(D)");
    mi.setMnemonic('D');
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (locked) return;
	TreePath tp = tree.getSelectionPath();
	if (tp != null) flc.delLink(tp);
      }
    });
    m.add(mi);
    mi = new JMenuItem("クリップボードにコピー(C)");
    mi.setMnemonic('C');
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	flc.copyToClip();
      }
    });
    m.add(mi);

    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    JScrollPane sp = new JScrollPane(tree);
    getContentPane().add(sp, BorderLayout.CENTER);
  }
  /**
   * @return boolean
   */
  public boolean isLocked() {
    return locked;
  }
  /**
   * 指定された行が表示されるようにする
   * @param row int
   */
  public void makePathVisible(TreePath path) {
    tree.scrollPathToVisible(path);
  }
  /**
   * ビューをロックする
   * @param b boolean
   */
  public void setLock(boolean b) {
    locked = b;
  }
  /**
   * JTreeにTreeModelをセットする
   * @param root TreeNode
   */
  public void setNodes(TreeModel tm) {
    tree.setModel(tm);
  }
}
