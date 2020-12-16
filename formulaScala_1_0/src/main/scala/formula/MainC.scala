package formula

import collection.JavaConversions._
import javax.swing.AbstractListModel
import formula.ui.MainView
import java.awt.Color
import java.text.NumberFormat
import java.awt.datatransfer.StringSelection

class MainC extends MatDetermin {
  protected val em = Env.fem
  private val mv = new MainView(this)
  protected val f = mv
  private var resultData: List[_] = _ // 製品で検索した場合はList[Pcode]、資材の場合はList[Array[AnyRef]]
  mv.setSeriesModel(new SeriesListModel)
  mv.pack
  mv.setVisible(true)

  private object Obsolete extends Enumeration {
    val O0 = Value("")
    val O1 = Value("廃番予定 ")
    val O2 = Value("廃番 ")
  }
  private val obsColor = Array(Color.black, Color.magenta, Color.red)
  private class SeriesListModel extends AbstractListModel[String] {
    private val data =
      em.createQuery("select distinct p.series from Pcode p order by p.series", classOf[String]).getResultList.toList
    override def getElementAt(ix: Int) = data(ix)
    override def getSize = data.size
  }
  private class ResultListModel(private var data: List[String]) extends AbstractListModel[String] {
    override def getElementAt(ix: Int) = if (data.size == 0) "該当なし" else data(ix)
    override def getSize = if (data.size == 0) 1 else data.size
  }
  /**
   * searchByCodeとsearchByNameの共通構造を抽象化する
   */
  private def searchBy(queryByProd: => List[_], determinMat: => Option[Mcode], setNoneText: => Unit,
    series: Array[AnyRef], mode: Int) = {
    resultData = if (mode == 0) queryByProd // 製品による検索
    else {
      val m = determinMat // 資材を確定させる
      m match {
        case None => setNoneText; List.empty
        case Some(determined) => { // 確定した資材による検索
          mv.getCodeField.setForeground(obsColor(determined.status))
          mv.getNameField.setForeground(obsColor(determined.status))
          mv.getCodeField.setText(determined.mcode.toString)
          mv.getNameField.setText(determined.mname)
          if (mode == 1) queryNormalByMat(series, determined.mcode)
          else queryResolvfByMat(series, determined.mcode)
        }
      }
    }
    mv.setResultModel(new ResultListModel(formatResult))
    em.clear()
  }
  /**
   * コード指定による検索を実行する。
   */
  def searchByCode(series: Array[AnyRef], code: Int, mode: Int) =
    searchBy(queryByPcode(code), determinByMcode(code), mv.getNameField.setText("該当なし"), series, mode)
  /**
   * 名前による検索を実行する
   */
  def searchByName(series: Array[AnyRef], name: String, mode: Int) {
    val rname = name.replace('*', '%').replace('?', '_')
    searchBy(queryByPname(series, rname), determinByMname(rname), mv.getCodeField.setText(""), series, mode)
  }
  /**
   * 検索結果を表示用に整形する
   */
  private def formatResult: List[String] = {
    val nf = NumberFormat.getPercentInstance
    nf.setMinimumFractionDigits(3)
    nf.setMaximumFractionDigits(3)
    def formatPcode(p: Pcode) = "" + Obsolete(p.obsolete) + p.pcode + " " + p.series + " " + p.name
    def formatPercent(p: Pcode, percent: Double) =
      Obsolete(p.obsolete) + nf.format(percent / 100) + " " + p.pcode + " " + p.series + " " + p.name
    resultData.map {
      case p: Pcode => formatPcode(p)
      case a: Array[AnyRef] => formatPercent(a(0).asInstanceOf[Pcode], a(1).asInstanceOf[Double])
    }
  }
  /**
   * 検索結果をクリップボードにコピーする
   */
  def copyToClip {
    val nf = NumberFormat.getInstance
    nf.setMinimumFractionDigits(3)
    nf.setMaximumFractionDigits(3)
    def formatPcode(p: Pcode) = "" + p.pcode + "\t" + p.series + "\t" + p.name + "\t" + Obsolete(p.obsolete)
    def formatPercent(p: Pcode, percent: Double) =
      p.pcode + "\t" + p.series + "\t" + p.name + "\t" + nf.format(percent) + "\t" + Obsolete(p.obsolete)
    val r = resultData.map {
      case p: Pcode => formatPcode(p)
      case a: Array[AnyRef] => formatPercent(a(0).asInstanceOf[Pcode], a(1).asInstanceOf[Double])
    }
    val cb = mv.getToolkit.getSystemClipboard
    val ss = new StringSelection(r.mkString("\n"))
    cb.setContents(ss, ss)
  }

  /**
   * 製品名による検索と確定した資材コードによる検索(通常処方及び分解処方)は、
   * クエリー文字列が違う以外はほぼ同一であり、この共通構造を抽象化する
   * @param T クエリーが返すエンティティクラスの型。PcodeかArray[AnyRef]
   * @param P クエリーパラメータの型。IntかString
   * @param queryFromAll 特定の品種が選ばれていない時の検索クエリー文字列
   * @param queryFromSeries 特定の品種が選ばれている時の検索クエリー文字列
   * @param c classOf[T]
   * @return 品種配列と検索パラメータを取り、検索結果を返す関数
   */
  private def genQuery[T, P](queryFromAll: String, queryFromSeries: String, c: Class[T]) = {
    (seriesArray: Array[AnyRef], p: P) =>
      {
        def fromAll = em.createQuery(queryFromAll, c).setParameter("p1", p).getResultList.toList
        def fromSeries(series: AnyRef) = em.createQuery(queryFromSeries, c)
          .setParameter("p1", series).setParameter("p2", p).getResultList.toList
        if (seriesArray.size == 0) fromAll
        else seriesArray.toList.flatMap(fromSeries)
      }
  }
  private val queryByPname = genQuery[Pcode, String](
    "select p from Pcode p where p.name like :p1",
    "select p from Pcode p where p.series = :p1 and p.name like :p2",
    classOf[Pcode])
  // 以下のクエリーでf.percentはFloatだが、sum(f.percent)はDoubleになる。
  // つまり通常処方検索と分解処方検索で戻り値の型が異なってしまう。
  // これを回避するため、f.percent+0.0として型をDoubleにしている。
  private val queryNormalByMat = genQuery[Array[AnyRef], Int](
    "select p, sum(f.percent) from Pcode p, Form1 f " +
      "where p.pcode = f.pk.pcode and f.mcode = :p1 group by p " +
      "order by sum(f.percent) desc, p.pcode",
    "select p, sum(f.percent) from Pcode p, Form1 f " +
      "where p.pcode = f.pk.pcode and p.series = :p1 and f.mcode = :p2 group by p " +
      "order by sum(f.percent) desc, p.pcode",
    classOf[Array[AnyRef]])
  private val queryResolvfByMat = genQuery[Array[AnyRef], Int](
    "select p, f.percent + 0.0 from Pcode p, Resolvf f where p.pcode = f.pk.pcode " +
      "and f.pk.mcode = :p1 order by f.percent desc, p.pcode",
    "select p, f.percent + 0.0 from Pcode p, Resolvf f where p.pcode = f.pk.pcode " +
      "and p.series = :p1 and f.pk.mcode = :p2 order by f.percent desc, p.pcode",
    classOf[Array[AnyRef]])
  private def queryByPcode(code: Int) =
    em.createQuery("select p from Pcode p where p.pcode = :pcode", classOf[Pcode])
      .setParameter("pcode", code).getResultList.toList

  def exit { println("exit invoked"); System.exit(0) }
  def showNMC {}
  def showNPC {}
  def showFLC {}
  def showCRC {}
  def showFBrowser(ix: Array[Int]) {
    if (resultData.size > 0) { // データサイズが0でも、1行目の"該当なし"が選択されてこのメソッドが呼ばれる可能性がある
      ix foreach {
        resultData(_) match {
          case p: Pcode => new FBrowseC(p)
          case a: Array[AnyRef] => new FBrowseC(a(0).asInstanceOf[Pcode])
        }
      }
    }
  }
}
