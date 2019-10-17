package formula.ui;

import formula.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 更新権限設定のダイアログ
 */
public class AuthenticationDialog extends JDialog {
    private JFrame parent;
    private JList candidates = null;
    private boolean canceled = true;

    public AuthenticationDialog(JFrame parent, String[] names) {
        super(parent, true);
        this.parent = parent;
        candidates = new JList(names);
        init();
    }


    private void init() {
        setTitle("更新権限の設定");
        Container cp = getContentPane();
        candidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(candidates);
        cp.add(sp, BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout());
        cp.add(p, BorderLayout.SOUTH);
        JButton b = new JButton("決定(G)");
        b.setMnemonic('G');
        b.addActionListener(e -> {
            if (candidates.getSelectedIndex() < 0) return;
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
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public String selectedPerson() {
        if (canceled) return null;
        return (String)candidates.getSelectedValue();
    }

}
