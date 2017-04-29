package formula.ui;

import formula.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * 更新権限設定のダイアログ
 */
public class AuthenticationDialog extends JDialog {
  private JList candidates = null;
  private boolean canceled = true;
  /**
   * AuthenticationDialog コンストラクター・コメント。
   */
  public AuthenticationDialog() {
    super();
    init();
  }
  /**
   * AuthenticationDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   */
  public AuthenticationDialog(java.awt.Frame arg1) {
    super(arg1);
    init();
  }
  /**
   * AuthenticationDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   * @param arg2 java.lang.String
   */
  public AuthenticationDialog(java.awt.Frame arg1, String arg2) {
    super(arg1, arg2);
    init();
  }
  /**
   * AuthenticationDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   * @param arg2 java.lang.String
   * @param arg3 boolean
   */
  public AuthenticationDialog(java.awt.Frame arg1, String arg2, boolean arg3) {
    super(arg1, arg2, arg3);
    init();
  }
  /**
   * AuthenticationDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   * @param arg2 boolean
   */
  public AuthenticationDialog(java.awt.Frame arg1, boolean arg2) {
    super(arg1, arg2);
    init();
  }
  /**
   * @return javax.swing.JList
   */
  public JList getList() {
    return candidates;
  }
  private void init() {
    setTitle("更新権限の設定");
    Container cp = getContentPane();
    candidates = new JList();
    candidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane sp = new JScrollPane(candidates);
    cp.add(sp, BorderLayout.CENTER);
    JPanel p = new JPanel(new FlowLayout());
    cp.add(p, BorderLayout.SOUTH);
    JButton b = new JButton("決定(G)");
    b.setMnemonic('G');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (getList().getSelectedIndex() < 0) return;
	canceled = false;
	setVisible(false);
      }
    });
    p.add(b);
    b = new JButton("キャンセル(C)");
    b.setMnemonic('C');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	canceled = true;
	setVisible(false);
      }
    });
    p.add(b);
  }
  /**
   * @return boolean
   */
  public boolean isCanceled() {
    return canceled;
  }
}
