package myui;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
/**
   日付表示用レンダラ。Date.toString()の返す文字列がそのままではすこぶる洋風なため、より受け入れられる形式にする。<p>
 */
public class DateCellRenderer extends DefaultTableCellRenderer {
    private DateFormat df;
    /**
       表示形式を"年/月/日"の形に指定する
    */
    public static final int DATE = 0;

    /**
       表示形式を"年/月/日 時/分/秒"の形に指定する
    */
    public static final int DATETIME = 1;

    public DateCellRenderer(int style) {
	super();
	switch (style) {
	case DATE:
	    df = DateFormat.getDateInstance();
	    break;
	case DATETIME:
	    df = DateFormat.getDateTimeInstance();
	    break;
	default:
	    df = DateFormat.getDateTimeInstance();
	}
    }

    /**
       値をしかるべき形式の文字列に変換してセットする。与えられた値の型がDateでない場合は、その型のtoString()が使用される
    */
    public void setValue(Object value) {
	if (value == null) {
	    setText("");
	    return;
	}
	if (value instanceof java.util.Date) {
	    setText(df.format((java.util.Date)value));
	} else setText(value.toString());
    }
}
