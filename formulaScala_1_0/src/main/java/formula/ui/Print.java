package formula.ui;

import java.awt.print.*;
import java.util.Locale;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class Print {
	private Printable p;

	public Print(Printable p) {
		this.p = p;
	}

	/**
	 * J2Dベースの印刷を実施する。ダイアログを表示してマニュアルで印刷設定を行うようになる
	 * @throws PrinterException
	 */
	public void printJ2D() throws PrinterException {
		//PrinterJobの取得
		PrinterJob pj = PrinterJob.getPrinterJob();

		//Printable, Pageableの設定
		pj.setPrintable(p);

		//印刷ダイアログの表示と印刷
		if (pj.printDialog()){
			pj.print();
		}
	}

	/**
	 * JPSベースの印刷。ダイアログを出さずにいきなり印刷する。プリンタによっては
	 * うまくいかない可能性もあるのでは?
	 * @throws PrintException
	 */
	public void print() throws PrintException {
		//印刷データの提供形式
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

		//印刷要求属性
		PrintRequestAttributeSet requestAttributeSet = 
				new HashPrintRequestAttributeSet();
		requestAttributeSet.add(new PageRanges("1"));
		requestAttributeSet.add(MediaSizeName.JIS_B5);  //用紙B5
		requestAttributeSet.add(
				new JobName("処方印刷", Locale.JAPANESE));  //ジョブ名

		//既定で選択される出力先
		PrintService defaultService 
		= PrintServiceLookup.lookupDefaultPrintService();
		//印刷ジョブの生成
		DocPrintJob job = defaultService.createPrintJob();

		//印刷ドキュメントの生成
		SimpleDoc doc = new SimpleDoc(p, flavor, null);
		//ジョブに印刷を依頼する
		job.print(doc, requestAttributeSet);
	}
}
