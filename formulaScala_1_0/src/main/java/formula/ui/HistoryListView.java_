package formula.ui;

import formula.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * 履歴リストを表示する
 */
public class HistoryListView extends JFrame {
  private HistoryListC hlc;
  private JTable table = null;
  /**
   * HistoryListView コンストラクター・コメント。
   * @param arg1 java.lang.String
   */
  public HistoryListView(HistoryListC hlc, String title) {
    super(title + "のあゆみ");
    this.hlc = hlc;
    init();
  }
  /**
   * @return javax.swing.JTable
   */
  public JTable getTable() {
    return table;
  }
  /**
   * 
   */
  private void init() {
    TableColumnModel tcm = new DefaultTableColumnModel();
    TableColumn tc;
    tc = new TableColumn(0, 96);
    tc.setHeaderValue("日付");
    tcm.addColumn(tc);
    tc = new TableColumn(1, 64);
    tc.setHeaderValue("登録者");
    tcm.addColumn(tc);
    tc = new TableColumn(2, 256);
    tc.setHeaderValue("更新理由");
    tcm.addColumn(tc);
	
    table = new JTable(new AbstractTableModel() {
      public int getColumnCount() {return 3;}
      public int getRowCount() {return 1;}
      public Object getValueAt(int row, int col) {
	switch (col) {
	  case 0:
	  return new Date();
	  case 1:
	  return "person";
	  case 2:
	  return "etaoin\nshrdlu\nqwerty";
	  default:
	  return null;
	}
      }
    }, tcm);
				
		
    table.setRowSelectionAllowed(true);
    table.setColumnSelectionAllowed(false);
    table.setCellSelectionEnabled(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    //	table.setPreferredScrollableViewportSize(new Dimension(1024, 32));
    JScrollPane sp = new JScrollPane(table);
    getContentPane().add(sp, BorderLayout.CENTER);

    JPanel p = new JPanel(new FlowLayout());
    getContentPane().add(p, BorderLayout.SOUTH);
    JButton b = new JButton("選択した処方を表示(S)");
    b.setMnemonic('S');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	hlc.showBrowsers(table.getSelectedRows());
      }
    });
    p.add(b);
  }
}
