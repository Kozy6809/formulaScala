package formula
import collection.JavaConversions._
import formula.ui.FBrowseV
import javax.swing.table.AbstractTableModel
import java.text.NumberFormat
import javax.swing.JOptionPane
import javax.persistence._


class FBrowseC(private val p: Pcode) extends MatDetermin {
  protected val em: javax.persistence.EntityManager = Env.fem
  private val form2 = em.createQuery("select f from Form2 f join fetch f.form1 where f.pcode = :pcode", classOf[Form2])
    .setParameter("pcode", p.pcode).getSingleResult
  private val q = em.createQuery("select r from Resolvf r where r.pk.pcode = :pcode order by r.pk.mcode", classOf[Resolvf])
  private val r = q.setParameter("pcode", p.pcode).getResultList.toList
  private var ffv = form1ToFormView(form2.form1.toList)
  private var rfv = resolvfToFormView(r)
  private var showingfv = ffv
  private var editing = false
  private val fbv = new FBrowseV(this, p.pcode + " " + p.series + " " + p.name)
  protected val f = fbv

  fbv.setForm2Data(form2)
  showNorm
  fbv.pack()
  fbv.setVisible(true)

  /**
   * 処方表示テーブル用のデータ型
   */
  private class FormView(var mcode: Int, var m: Mcode, var percent: Float)

  private def form1ToFormView(l: List[Form1]): List[FormView] =
    l.map(f => new FormView(f.mcode, determinByMcode(f.mcode).get, f.percent))

  private def resolvfToFormView(l: List[Resolvf]): List[FormView] =
    l.map(r => new FormView(r.pk.mcode, determinByMcode(r.pk.mcode).get, r.percent))

  class FormTableModel(private val l: List[FormView], private val editable: Boolean)
    extends AbstractTableModel {
    def getRowCount = l.size
    def getColumnCount = 3
    def getValueAt(r: Int, c: Int) = c match {
      case 0 => new Integer(l(r).mcode)
      case 1 => l(r).m.mname
      case 2 => new java.lang.Double(l(r).percent)
    }
    override def setValueAt(v: AnyRef, r: Int, c: Int) {
      def processMcode(o: Option[Mcode]) = o match {
        case None => { l(r).m.mname = "該当なし"; l(r).m.status = 2 }
        case Some(res) => { l(r).m = res; l(r).mcode = res.mcode }
      }
      c match {
        case 0 => val vint = v.asInstanceOf[Int]; l(r).mcode = vint; processMcode(determinByMcode(vint))
        case 1 => l(r).mcode = 0; processMcode(determinByMname(v.asInstanceOf[String].replace('*', '%').replace('?', '_')))
        case 2 => l(r).percent = v.asInstanceOf[Double].toFloat
      }
      updateState
    }
    override def isCellEditable(r: Int, c: Int) = editable
  }

  def matStatus(row: Int) = showingfv(row).m.status
  private def total = showingfv.map(_.percent).sum
  private def price = {
    def accumPrice(t: Double, fv: FormView) = t + fv.m.price * fv.percent
    if (rfv exists { _.m.price <= 0 }) -1.0
    else rfv.foldLeft(0.0)(accumPrice(_, _)) / total
  }
  /**
   * 分解処方を計算する。<p>
   * 資材コードが500000から600000の範囲にあるものをresolvf表を参照して分解する<p>
   * 比率が0のものを除去することによって、"----------------"などのイリーガルな原料を排除している<p>
   * 原料に自分自身を含む場合、それを除いた残りで分解処方を計算し、最後に比率を全量の比率に換算する
   */
  private def resolv(lfv: List[FormView]) = {
    val decomp = lfv.filter(fv => { fv.percent > 0.0 && fv.mcode != p.pcode }).flatMap { fv =>
      if (fv.mcode >= 500000 && fv.mcode < 600000)
        resolvfToFormView(q.setParameter("pcode", fv.mcode).getResultList.toList)
          .map(rfv => { rfv.percent = fv.percent * rfv.percent / 100; rfv })
      else List(fv)
    }
    val subTotal = decomp.map(_.percent).sum
    // 分解処方を資材コードごとにまとめ、一つのコードに複数項目があればその比率を加算して一つにまとめる
    def sumPercent(a: FormView, b: FormView) = new FormView(a.mcode, a.m, a.percent + b.percent)
    decomp.groupBy(_.mcode).values.map(_.reduceLeft(sumPercent(_, _))).toList.sortBy(_.mcode)
      .map(fv => { fv.percent = fv.percent / subTotal * total; fv })
  }
  /**
   * 更新される処方に依存する処方の分解処方を計算する
   * 循環参照がある場合、循環しているものの比率が100%でなければ、分解を繰り返すうちに比率はどんどん小さくなる。
   * そのため比率が0.001%よりも小さければ分解を打ち切るようにすれば循環参照があっても問題ない。
   * 実際の打ち切りの閾値は、計算精度を考慮して0.0001%にする
   */
  private def resolvForUpdate(lf: List[Form1]) = {
    def form1ToResolvf(f: Form1) = new Resolvf(f.pk.pcode, f.mcode, f.percent)
    type LR = List[Resolvf]
    def qForm1(pcode: Int) =
      em.createQuery("select f from Form1 f where f.pk.pcode = :pcode", classOf[Form1])
        .setParameter("pcode", pcode).getResultList.toList.map(form1ToResolvf)
    def mplyPercent(percent: Float, lr: LR) = lr.map(r => { r.percent = r.percent * percent / 100; r })
    def decomp(accum: LR, target: LR): LR = {
      val (comp: LR, prim: LR) =
        target.filter(_.percent > 0.0001).partition(r => r.pk.mcode >= 500000 && r.pk.mcode < 600000)
      if (comp.size > 0)
        decomp(accum ++ prim, comp.flatMap(r => mplyPercent(r.percent, qForm1(r.pk.mcode))))
      else accum ++ prim
    }
    def sumPercent(a: Resolvf, b: Resolvf) = new Resolvf(a.pk.pcode, a.pk.mcode, a.percent + b.percent)
    decomp(List.empty, lf.map(form1ToResolvf)).groupBy(_.pk.mcode).values
      .map(_.reduceLeft(sumPercent(_, _))).toList.sortBy(_.pk.mcode)
  }
  /**
   * 更新される処方に依存する処方をリストアップする
   * 更新される製品の製造コードを原料に含んでいる処方を順次検索し、検索されたListを接続していく
   * 循環参照がある可能性があるので、検索したものが既に検索されたListに含まれていたら、それ以上検索しないようにする
   */
  private def dependants(pcode: Int) = {
    val qDependants =
      em.createQuery("select distinct f.pcode from Form1 f where f.mcode = :pcode", classOf[Int])
    var list = List.empty[Int]
    def dependants_(l: List[Int]): List[Int] = {
      l.flatMap { pcode =>
        if (list.contains(pcode)) List.empty[Int]
        else {
          val r = qDependants.setParameter("pcode", pcode).getResultList.toList
          list = list ++ r
          if (r.size > 0) dependants_(r) else r
        }
      }
    }
    dependants_(List(pcode))
  }
  /**
   * 原料種類や比率が更新されたら、分解処方と比率合計も更新する
   */
  private def updateState {
    rfv = resolv(ffv)
    showNorm
  }
  def copyToClip {}
  def authorize {
    form2.reason = ""
    form2.person = "" // TODO 更新権限で設定された登録者が入る
    fbv.setForm2Data(form2)
    fbv.setEditable(true)
    editing = true
    showNorm
  }
  def update {
    if (editing) {
      fbv.getForm2Data(form2)
      if (form2.reason == "" || ffv.exists(_.m.mname == "該当なし")) {
        JOptionPane.showMessageDialog(fbv,
				    "カラムに不正な値が入っているか、　\n" +
				    "または空白にできないカラムが空白になっています　\n\n" +
				    "更新理由は記入しましたか？　\n" +
				    "更新理由は空白にできません　",
				    "値が不正です",
				    JOptionPane.WARNING_MESSAGE)
      } else {
        updateFormula
        dependants(p.pcode).foreach(updateResolvf(_))
        editing = false
        fbv.setEditable(false)
        showNorm
      }
    }
  }
  private def updateFormula {}
  private def updateResolvf(pcode:Int) {}
  def requestClose {}
  def showHistory {}
  def showLinkList {}
  def showPoisonList {}
  def showNorm {
    showingfv = ffv
    fbv.setModel(new FormTableModel(ffv, editing))
    fbv.setTotal(total)
    fbv.setPrice(price)
    fbv.selectShowNormMenu()
  }
  def showDecomp {
    showingfv = rfv
    fbv.setModel(new FormTableModel(rfv, false))
    fbv.setTotal(total)
    fbv.setPrice(price)
  }
  def recalcToxicity {}
  def print {}
  def insertRow(row: Int) {
    val (f, r) = ffv.splitAt(row)
    ffv = (f :+ new FormView(0, new Mcode, 0.0f)) ++ r
    showNorm
  }
  def deleteRow(row: Int) {
    val (f, r) = ffv.splitAt(row)
    ffv = f ++ r.tail
    updateState
  }
  def exchangeRow(row: Int) {
    if (row < ffv.size - 1) ffv = ffv.patch(row, List(ffv(row + 1), ffv(row)), 2)
    showNorm
  }
  def isEditing = editing
}

