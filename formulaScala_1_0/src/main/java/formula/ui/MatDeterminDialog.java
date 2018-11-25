package formula.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MatDeterminDialog extends JDialog {
    private JList<String> candidates;
    private boolean canceled = true;

    public MatDeterminDialog() {
        super();
        init();
    }

    /**
     * MatDeterminDialog コンストラクター・コメント。
     *
     * @param owner java.awt.Frame
     */
    public MatDeterminDialog(java.awt.Frame owner) {
        super(owner);
        init();
    }

    /**
     * MatDeterminDialog コンストラクター・コメント。
     *
     * @param owner java.awt.Frame
     * @param title java.lang.String
     */
    public MatDeterminDialog(java.awt.Frame owner, String title) {
        super(owner, title);
        init();
    }

    /**
     * MatDeterminDialog コンストラクター・コメント。
     *
     * @param owner java.awt.Frame
     * @param title java.lang.String
     * @param modal boolean
     */
    public MatDeterminDialog(java.awt.Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        init();
    }

    /**
     * MatDeterminDialog コンストラクター・コメント。
     *
     * @param owner java.awt.Frame
     * @param modal boolean
     */
    public MatDeterminDialog(java.awt.Frame owner, boolean modal) {
        super(owner, modal);
        init();
    }

    /**
     * @return javax.swing.JList
     */
    public JList<String> getList() {
        return candidates;
    }

    private void init() {
        setTitle("複数の候補があります");
        Container cp = getContentPane();
        JLabel l = new JLabel("次のリストから選んで下さい", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        cp.add(l, BorderLayout.NORTH);
        candidates = new JList<String>();
        candidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(candidates);
        cp.add(sp, BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout());
        cp.add(p, BorderLayout.SOUTH);
        JButton b = new JButton("決定(G)");
        b.setMnemonic('G');
        b.addActionListener(e -> {
            canceled = false;
            setVisible(false);
        });
        p.add(b);
        b = new JButton("キャンセル(C)");
        b.setMnemonic('C');
        b.addActionListener(e -> {
            canceled = true;
            setVisible(false);
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
