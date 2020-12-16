package formula.ui;

/**
 * この クラス は SmartGuide によって生成されました。
 */

import java.awt.*;
import java.util.*;

public class PrintCanvas extends java.awt.Canvas {
    //  private int width = 516*90/100;
    //  private int height = 729*90/100;
    /*
      処方連絡書の印字がページからはみだす問題に対処するため、現在決め打ちしているページサイズの値を小さくすることにした。超quick hack
    */
    private int width = 516 * 90 / 100;
    private int height = 729 * 95 / 100;
    private int resolution = 360;
    private String regDate = null;
    private String person = null;
    private String sg = null;
    private String price = null;
    private String comment = null;
    private String reason = null;
    private Vector mr = null;
    private String pname = null; // pcode & series & name
    private boolean modeResolv = false;

    /**
     * PrintCanvas コンストラクター・コメント
     */
    public PrintCanvas() {
        super();
        setBackground(Color.white);
    }

    private Vector alignText(String text, FontMetrics fm, int width) {
        if (text == null) return null;
        Vector v = new Vector();
        for (; ; ) {
            int ix = text.indexOf('\n');
            if (ix < 0) {
                v.addElement(text);
                break;
            }
            v.addElement(text.substring(0, ix));
            text = text.substring(ix + 1);
        }
        v.addElement("");
        for (int i = 0; i < v.size() - 1; i++) {
            String fore = (String) (v.elementAt(i));
            String rear = "";
            if (fm.stringWidth(fore) > width) {
                for (; fm.stringWidth(fore) > width; ) {
                    rear = fore.substring(fore.length() - 1) + rear;
                    fore = fore.substring(0, fore.length() - 1);
                }
                v.setElementAt(fore, i);
                v.insertElementAt(rear, i + 1);
            }
        }
        return v;
    }

    private void drawText(Graphics g, String s, int width, int maxLine) {
        Font f = g.getFont();
        FontMetrics fm = getToolkit().getFontMetrics(f);
        int lineHeight = fm.getHeight();
        Vector v = alignText(s, fm, width);
        if (v == null) return;
        for (int i = 0; i < maxLine && i < v.size(); i++) {
            String aLine = (String) (v.elementAt(i));
            g.drawString(aLine, 0, lineHeight * i);
        }
    }

    public Dimension getPreferredSize() {
        //    return new Dimension(width, height+256);
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        if (modeResolv) paintResolv(g);
        else paintFormula(g);
    }

    /**
     * 長い行を単語の空白位置でほぼ二分割する。空白が無い場合は等分する
     *
     * @param l
     * @return 分割する文字位置
     */
    private int splitLine(String l) {
        int half = l.length() / 2;
        int n = 0;
        for (; n >= 0; ) {
            n = l.indexOf(' ', n + 1);
            System.out.println("92 " + n);
            if (n > half) break;
        }
        return (n > 0) ? n : half; // 空白が見つからなければhalfを返す
    }

    public void paintFormula(Graphics g) {
        int margin = width / 12;
        int ewidth = width - margin;
        g.translate(margin, 0);
        int x = 0;
        int y = 0;
        String title = "処方連絡書";
        String outDate = "印字日:" + java.text.DateFormat.getDateInstance().format(new Date());
        Font f = g.getFont();
        FontMetrics fm = getToolkit().getFontMetrics(f);
        int lineHeight = fm.getHeight();
        int sw = fm.stringWidth(title);
        g.drawString(title, (ewidth - sw) / 2, lineHeight * 2);
        sw = fm.stringWidth(outDate);
        g.drawString(outDate, (ewidth - sw), lineHeight * 2);
        sw = fm.stringWidth(pname);
        g.drawString(pname, (ewidth - sw) / 2, lineHeight * 4);
        g.drawString(person, 0, lineHeight * 6);
        g.drawString(regDate, 0, lineHeight * 7);
        g.drawString(sg, 0, lineHeight * 8);
        g.drawString(price, 0, lineHeight * 15);
        sw = fm.stringWidth(regDate) * 120 / 100;
        x = sw;
        y = lineHeight * 5;
        g.translate(x, y);

        // determine the maximum ewidth of material name
        sw = 0; // string width
        int swLimit = fm.stringWidth("8888888888888888888888");
        for (int i = 0; i < mr.size(); i++) {
            String[] sary = (String[]) mr.elementAt(i);
            int w = fm.stringWidth(sary[1]);
            if (w > sw) sw = w;
        }
        if (sw > swLimit) sw = swLimit;
        System.out.println("131 " + swLimit);
        System.out.println("132 " + sw);
        sw = sw * 110 / 100;
        int nw = fm.stringWidth("88") * 120 / 100; // number width
        int cw = fm.stringWidth("888888") * 120 / 100; // code width
        int pw = fm.stringWidth("188.888") * 120 / 100; // percent width
        int nSplitLine = 0; // 長いため分割した行の個数
        int no = 0; // 行番号
        for (int i = 0; i < mr.size(); i++, no++) {
            int splitLineAt = 0; // 分割位置
            String[] sary = (String[]) mr.elementAt(i);
            System.out.println("142 " + fm.stringWidth(sary[2]));
            if (fm.stringWidth(sary[1]) > sw) {
                nSplitLine++;
                splitLineAt = splitLine(sary[1]);
                System.out.println("146 " + splitLineAt);
            }
            int hScale = (splitLineAt == 0) ? 1 : 2; // 行高さスケーリング値
            int cf = i + nSplitLine - hScale + 1; // 行位置修正ファクター
            g.drawRect(0, lineHeight * cf, nw + cw + sw + pw + nw, lineHeight * hScale);
            if (no > 0 && no <= mr.size()) g.drawString(String.valueOf(no), nw / 10, lineHeight * (cf + 1));
            g.drawString(sary[0], nw + cw / 10, lineHeight * (cf + 1));
            if (splitLineAt == 0) {
                g.drawString(sary[1], nw + cw + sw / 40, lineHeight * (cf + 1));
            } else {
                g.drawString(sary[1].substring(0, splitLineAt), nw + cw + sw / 40, lineHeight * (cf + 1));
                g.drawString(sary[1].substring(splitLineAt), nw + cw + sw / 40, lineHeight * (cf + 2));
            }
            int percentewidth = fm.stringWidth(sary[2]);
            g.drawString(sary[2], nw + cw + sw + pw - percentewidth - pw / 10, lineHeight * (cf + 1));
        }
//    for (int i=1; i < mr.size()-1; i++) {
//      g.drawString(String.valueOf(i), nw/10, lineHeight*(i+1));
//    }
        g.drawLine(nw, 0, nw, lineHeight * (mr.size() + nSplitLine));
        g.drawLine(nw + cw, 0, nw + cw, lineHeight * (mr.size() + nSplitLine));
        g.drawLine(nw + cw + sw, 0, nw + cw + sw, lineHeight * (mr.size() + nSplitLine));
        g.drawLine(nw + cw + sw + pw, 0, nw + cw + sw + pw, lineHeight * (mr.size() + nSplitLine));
        g.drawString("※製品ラベルの変更が必要な原料は右端にチェック", 0, lineHeight * (mr.size() + nSplitLine + 1));
        g.translate(-x, -y);
        x = 0;
        y = lineHeight * 22;
        g.translate(x, y);
        g.drawString("特記事項", 0, 0);
        g.drawRect(0, 0, ewidth, lineHeight * 4);
        g.translate(0, lineHeight);
        drawText(g, comment, ewidth, 4);

        g.translate(0, lineHeight * 5);
        g.drawString("更新理由・旧処方品との混合の可否 … ( 可 ・ 不可 )", 0, 0);
        g.drawRect(0, 0, ewidth, lineHeight * 4);
        g.translate(0, lineHeight);
        drawText(g, reason, ewidth, 4);

        // draw manufacture box
        g.translate(0, lineHeight * 5);
        String[] factoryItem = {"容量(ml)", "最大ロット(dz)", "最小ロット(dz)", "機械1", "機械2",
                "機械3", "優先順", "伝票flag"};
        sw = 0;
        //    lineHeight = lineHeight * 130 / 100;
        for (int i = 0; i < factoryItem.length; i++) {
            int w = fm.stringWidth(factoryItem[i]);
            if (w > sw) sw = w;
            g.drawRect(0, lineHeight * i, ewidth / 3, lineHeight);
            g.drawString(factoryItem[i], 0, lineHeight * (i + 1));
        }
        g.drawLine(sw, 0, sw, lineHeight * factoryItem.length);
        // draw stamp box
        g.translate(ewidth / 3 + ewidth / 12, 0);
        g.drawRect(0, 0, lineHeight * 4, lineHeight * 8);
        g.drawLine(0, lineHeight * 2, lineHeight * 4, lineHeight * 2);
        g.drawLine(0, lineHeight * 4, lineHeight * 4, lineHeight * 4);
        g.drawLine(0, lineHeight * 6, lineHeight * 4, lineHeight * 6);
        g.drawLine(lineHeight * 2, 0, lineHeight * 2, lineHeight * 8);
        g.drawString("上長", 0, lineHeight * 15 / 10);
        g.drawString("部長", 0, lineHeight * 35 / 10);
        g.drawString("登録", 0, lineHeight * 55 / 10);
        g.drawString("製造", 0, lineHeight * 75 / 10);
        validate();

    }

    public void paintResolv(Graphics g) {
        int mergin = width / 12;
        int ewidth = width - mergin;
        g.translate(mergin, 0);
        int x = 0;
        int y = 0;
        String title = "分解処方";
        String outDate = "印字日:" + java.text.DateFormat.getDateInstance().format(new Date());
        Font f = g.getFont();
        FontMetrics fm = getToolkit().getFontMetrics(f);
        int lineHeight = fm.getHeight();
        int sw = fm.stringWidth(title);
        g.drawString(title, (ewidth - sw) / 2, lineHeight * 2);
        sw = fm.stringWidth(outDate);
        g.drawString(outDate, (ewidth - sw), lineHeight * 2);
        sw = fm.stringWidth(pname);
        g.drawString(pname, (ewidth - sw) / 2, lineHeight * 4);
    /*
      g.drawString(person, 0, lineHeight*6);
      g.drawString(regDate, 0, lineHeight*7);
      g.drawString(sg, 0, lineHeight*8);
      sw = fm.stringWidth(regDate)*120/100;
      x = sw;
      y = lineHeight*5;
      g.translate(x, y);
    */
        // determine the maximum ewidth of material name
        sw = 0;
        for (int i = 0; i < mr.size(); i++) {
            String[] sary = (String[]) mr.elementAt(i);
            int w = fm.stringWidth(sary[1]);
            if (w > sw) sw = w;
        }
        sw = sw * 110 / 100;
        int nw = fm.stringWidth("88") * 120 / 100;
        int cw = fm.stringWidth("888888") * 120 / 100;
        int pw = fm.stringWidth("188.888") * 120 / 100;
        g.translate((ewidth - sw - nw - cw - pw) / 2, lineHeight * 5);
        for (int i = 0; i < mr.size(); i++) {
            g.drawRect(0, lineHeight * i, sw + nw + cw + pw, lineHeight);
            String[] sary = (String[]) mr.elementAt(i);
            g.drawString(sary[0], nw + cw / 10, lineHeight * (i + 1));
            g.drawString(sary[1], nw + cw + sw / 40, lineHeight * (i + 1));
            int percentewidth = fm.stringWidth(sary[2]);
            g.drawString(sary[2], nw + cw + sw + pw - percentewidth - pw / 10, lineHeight * (i + 1));
        }
        for (int i = 1; i < mr.size() - 1; i++) {
            g.drawString(String.valueOf(i), nw / 10, lineHeight * (i + 1));
        }
        g.drawLine(nw, 0, nw, lineHeight * mr.size());
        g.drawLine(nw + cw, 0, nw + cw, lineHeight * mr.size());
        g.drawLine(nw + cw + sw, 0, nw + cw + sw, lineHeight * mr.size());
    }

    public void setComment(String c) {
        comment = (c == null) ? "" : c;
    }

    public void setHeight(int h) {
    /* 現状ではサイズをB5に固定している。だがプリンタの機種が変われば適切な値も変化する
       可能性もあることに注意
       height = h*9/10; // for mergin
    */
    }

    /**
     * このメソッドは SmartGuide によって作成されました。
     *
     * @param b boolean
     */
    public void setModeResolv(boolean b) {
        modeResolv = b;
    }

    public void setMr(Vector m) {
        mr = m;
        mr.insertElementAt(
                new String[]{"コード", "　　資　材　名　　", "比　率"}, 0);
    }

    public void setPerson(String p) {
        person = "登録者:" + ((p == null) ? "" : p);
    }

    public void setPname(String n) {
        pname = (n == null) ? "" : n;
    }

    public void setPrice(String s) {
        price = "g単価:" + ((s == null) ? "" : s);
    }

    public void setReason(String r) {
        reason = (r == null) ? "" : r;
    }

    public void setRegDate(String rd) {
        regDate = "登録日:" + ((rd == null) ? "" : rd);
    }

    public void setResolution(int r) {
        resolution = r;
    }

    public void setSg(String s) {
        sg = "比重:" + ((s == null) ? "" : s);
    }

    public void setWidth(int w) {
    /* 現状ではサイズをB5に固定している。だがプリンタの機種が変われば適切な値も変化する
       可能性もあることに注意
       width = w*9/10; // form mergin
    */
    }
}
