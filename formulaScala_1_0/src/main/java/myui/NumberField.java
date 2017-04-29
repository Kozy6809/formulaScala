package myui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
/**
 * 数値データのみが入力できるJTextField。主に正の整数を入力することを想定している
 */
public class NumberField extends JTextField {
  private boolean allowDouble = false;
  private boolean allowMinus = false;
  private boolean dotted = false; // 小数点が入力済み
  private boolean minused = false; // マイナス記号が入力済み
	
  protected class NumberDocument extends PlainDocument {

    public void insertString(int offs, String str, AttributeSet a) 
      throws BadLocationException {

      char[] s = str.toCharArray();
      char[] r = new char[s.length];
      int i = 0;
      int j = 0;

      if (allowMinus && s[0] == '-' && offs == 0 && !minused) {
	minused = true;
	r[0] = '-';
	i++;
	j++;
      }
      for (; i < r.length; i++) {
	if (s[i] == '-') continue;
	if (s[i] == '.') {
	  if (allowDouble && !dotted) {
	    dotted = true;
	    r[j++] = s[i];
	  }
	  continue;
	}
	if (Character.isDigit(s[i])) r[j++] = s[i];
      }
      super.insertString(offs, new String(r, 0, j), a);
    }

    public void remove(int offs, int len) throws BadLocationException {
      String s = getText(offs, len);
      if (s.indexOf('-') >= 0) minused = false;
      if (s.indexOf('.') >= 0) dotted = false;
      super.remove(offs, len);
    }
  }
  /**
   * NumberField コンストラクター・コメント。
   */
  public NumberField() {
    super();
  }
  /**
   * NumberField コンストラクター・コメント。
   * @param columns int
   */
  public NumberField(int columns) {
    super(columns);
  }
  /**
   * NumberField コンストラクター・コメント。
   * @param text java.lang.String
   */
  public NumberField(String text) {
    super(text);
  }
  /**
   * NumberField コンストラクター・コメント。
   * @param text java.lang.String
   * @param columns int
   */
  public NumberField(String text, int columns) {
    super(text, columns);
  }
  /**
   * NumberField コンストラクター・コメント。
   * @param doc javax.swing.text.Document
   * @param text java.lang.String
   * @param columns int
   */
  public NumberField(Document doc, String text, int columns) {
    super(doc, text, columns);
  }
  /**
   * @return com.sun.java.swing.text.Document
   */
  protected Document createDefaultModel() {
    return new NumberDocument();
  }
  /**
     実数値の入力を許容する
     @param b boolean
   */
  public void setAllowDouble(boolean b) {
    allowDouble = b;
  }
  /**
     負数の入力を許容する
     @param b boolean
   */
  public void setAllowMinus(boolean b) {
    allowMinus = b;
  }

    /**
       long値を返す。値が実数の場合は、最も近いlong値を返す
    */
    public long getLongValue() {
	return Math.round(getDoubleValue());
    }

    /**
       実数値を返す
    */
    public double getDoubleValue() {
	double d = 0.0;
	try {
	    d = Double.parseDouble(getText());
	} catch (NumberFormatException e) {e.printStackTrace();}
	return d;
    }
}
