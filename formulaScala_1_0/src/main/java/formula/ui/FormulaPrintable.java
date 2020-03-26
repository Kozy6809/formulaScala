package formula.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class FormulaPrintable implements Printable {
    private int width = 516 * 90 / 100;
    private int height = 729 * 95 / 100;
    private String regDate = null;
    private String person = null;
    private String sg = null;
    private String price = null;
    private String comment = null;
    private String reason = null;
    private List mr = null;
    private String pname = null; // pcode & series & name
    private boolean modeResolv = false;

    public int print(Graphics g, PageFormat pf, int pageIndex)
            throws PrinterException {
        // JDK7以降、デフォルトのフォントではラテン文字と日本語の混在する文字列が、文字種の切り替わりで印刷位置が
        // 左端に戻って重ね書きされてしまうというバグが発生している。論理フォントではMonospacedでこの問題を回避
        // できるが見栄えが今ひとつなので、以下の物理フォントを指定している。移植性が損なわれていることに注意。
        g.setFont(new Font("ＭＳ Ｐゴシック", Font.PLAIN, 13));
        if (pageIndex == 0) {
            //余白を加味して平行移動
            g.translate((int) pf.getImageableX(), (int) pf.getImageableY());
            width = (int) pf.getWidth() * 9 / 10;
            height = (int) pf.getHeight();
            if (modeResolv) paintResolv(g);
            else paintFormula(g);

            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    private List<String> alignText(String text, FontMetrics fm, int width) {
        if (text == null) return null;
        List<String> v = new LinkedList<String>();
        for (; ; ) {
            int ix = text.indexOf('\n');
            if (ix < 0) {
                v.add(text);
                break;
            }
            v.add(text.substring(0, ix));
            text = text.substring(ix + 1);
        }
        v.add("");
        for (int i = 0; i < v.size() - 1; i++) {
            String fore = (String) (v.get(i));
            String rear = "";
            if (fm.stringWidth(fore) > width) {
                for (; fm.stringWidth(fore) > width; ) {
                    rear = fore.substring(fore.length() - 1) + rear;
                    fore = fore.substring(0, fore.length() - 1);
                }
                v.set(i, fore);
                v.add(i + 1, rear);
            }
        }
        return v;
    }

    private void drawText(Graphics g, String s, int width, int maxLine) {
        Font f = g.getFont();
        FontMetrics fm = g.getFontMetrics(f);
        int lineHeight = fm.getHeight();
        List<String> v = alignText(s, fm, width);
        if (v == null) return;
        for (int i = 0; i < maxLine && i < v.size(); i++) {
            String aLine = (String) (v.get(i));
            g.drawString(aLine, 0, lineHeight * i);
        }
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
            if (n > half) break;
        }
        return (n > 0) ? n : half; // 空白が見つからなければhalfを返す
    }

    public void paintFormula(Graphics g) {
        int mergin = width / 12;
        int ewidth = width - mergin;
        g.translate(mergin, 0);
        int x = 0;
        int y = 0;
        String title = "処方連絡書";
        String outDate = "印字日:" + java.text.DateFormat.getDateInstance().format(new Date());
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight() * 11 / 10;
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
            String[] sary = (String[]) mr.get(i);
            int w = fm.stringWidth(sary[1]);
            if (w > sw) sw = w;
        }
        if (sw > swLimit) sw = swLimit;
        sw = sw * 110 / 100;
        int nw = fm.stringWidth("88") * 120 / 100; // number width
        int cw = fm.stringWidth("888888") * 120 / 100; // code width
        int pw = fm.stringWidth("188.888") * 120 / 100; // percent width
        int nSplitLine = 0; // 長いため分割した行の個数
        int no = 0; // 行番号

        g.drawString("※", nw + cw + sw + pw + nw / 10, lineHeight);

        for (int i = 0; i < mr.size(); i++, no++) {
            int splitLineAt = 0; // 分割位置
            String[] sary = (String[]) mr.get(i);
            if (fm.stringWidth(sary[1]) > sw) {
                nSplitLine++;
                splitLineAt = splitLine(sary[1]);
            }
            int hScale = (splitLineAt == 0) ? 1 : 2; // 行高さスケーリング値
            int cf = i + nSplitLine - hScale + 1; // 行位置修正ファクター
            g.drawRect(0, lineHeight * cf, nw + cw + sw + pw + nw, lineHeight * hScale);
            if (no > 0 && no < mr.size() - 1) {
				g.drawString(String.valueOf(no), nw / 10, lineHeight * (cf + 1));
			}
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
        FontMetrics fm = g.getFontMetrics(f);
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
            String[] sary = (String[]) mr.get(i);
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
            String[] sary = (String[]) mr.get(i);
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

    public void setModeResolv(boolean b) {
        modeResolv = b;
    }

    public void setMr(List m) {
        mr = m;
        mr.add(0, new String[]{"コード", "　　資　材　名　　", "比　率"});
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
    }

    public void setSg(String s) {
        sg = "比重:" + ((s == null) ? "" : s);
    }
}
