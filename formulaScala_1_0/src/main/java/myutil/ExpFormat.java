package myutil;

/**
 * 数値を指数形式でフォーマットする。有効数字が指定できる。
 */
public class ExpFormat {
  static double log10 = Math.log(10);
  /**
   * ExpFormat コンストラクター・コメント。
   */
  private ExpFormat() {
    super();
  }
  /**
   * 数値を指数形式でフォーマットする
   * @return java.lang.String
   * @param in double
   * @param eff int 有効数字
   */
  public static String format(double in, int eff) {
    if (in == 0.0) return "0";
    double d = Math.ceil(Math.log(in) / log10);
    double t = in * Math.pow(10, eff-d);
    long l = Math.round(t);
    return "." + l + "E" + Math.round(d);
  }
}
