package formula.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MatDeterminDialog extends JDialog {
	private JList<String> candidates;
	
	public MatDeterminDialog(java.awt.Frame f, String[] data) {
		super(f, true);
		setTitle("複数の候補があります");
		Container cp = getContentPane();
		JLabel l = new JLabel("次のリストから選んで下さい", SwingConstants.CENTER);
		l.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		cp.add(l, BorderLayout.NORTH);
		candidates = new JList<String>(data);
		candidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(candidates);
		cp.add(sp, BorderLayout.CENTER);
		JPanel p = new JPanel(new FlowLayout());
		cp.add(p, BorderLayout.SOUTH);
		JButton b = new JButton("決定(G)");
		b.setMnemonic('G');
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					setVisible(false);
			}
		});
		p.add(b);
		b = new JButton("キャンセル(C)");
		b.setMnemonic('C');
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				candidates.clearSelection();
				setVisible(false);
			}
		});
		p.add(b);
	}
	public int selection() {
		return candidates.getSelectedIndex();
	}
}