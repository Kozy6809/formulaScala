package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
/**
 * 印刷のプレビュー画面
 */
public class PrintPreview extends JFrame {
  private JScrollPane sp = null;
  private PrintCanvas pc;
  /**
   * PrintPreview コンストラクター・コメント。
   */
  public PrintPreview(PrintCanvas pc) {
    super();
    this.pc = pc;
    sp = new JScrollPane(pc);
    getContentPane().add(sp);
  }
  public void print() {
    Properties printPrp = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream("print.prp");
      printPrp.load(fis);
      fis.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }	
    PrintJob pjob = getToolkit().getPrintJob(this, "処方印刷", printPrp);
    if (pjob != null) {
      if (printPrp != null) {
	try {
	  FileOutputStream fos = new FileOutputStream("print.prp");
	  printPrp.store(fos, "print property");
	  fos.close();
	} catch (IOException e) {e.printStackTrace();}	
      }	
      Dimension d = pjob.getPageDimension();
      pc.setWidth(d.width);
      pc.setHeight(d.height);
      pc.setResolution(pjob.getPageResolution());
      Graphics pg = pjob.getGraphics();
      if (pg != null) {
	pc.printAll(pg);
	pg.dispose(); // flush page
      }
      pjob.end();
    }
  }
}
