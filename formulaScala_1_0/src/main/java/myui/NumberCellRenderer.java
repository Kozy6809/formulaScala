package myui;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * 数値表示用レンダラー。DoubleかIntegerが与えられた場合、書式を整形して
 * 表示する
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
    private NumberFormat nf = NumberFormat.getInstance();
    /**
     * NumberCellRenderer コンストラクター・コメント。
     */
    public NumberCellRenderer() {
	super();
	setHorizontalAlignment(SwingConstants.RIGHT);
    }
    /**
       小数点以下の桁数を設定する
     */
    public void setFractionDigits(int fd) {
	nf.setMinimumFractionDigits(fd);
	nf.setMaximumFractionDigits(fd);
    }
    /**
     * 
     */
    public void setValue(Object value) {
	if (value == null) {
	    setText("");
	    return;
	}
	if (value instanceof Integer) {
	    setText(((Integer)value).toString());
	} else if (value instanceof Double) {
	    setText(nf.format(((Double)value).doubleValue()));
	} else setText(value.toString());
    }
}
