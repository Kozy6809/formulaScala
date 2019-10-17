package formula

import collection.JavaConversions._
import javax.swing.table.AbstractTableModel
import java.text.DateFormat
import java.text.NumberFormat
import javax.swing.JOptionPane
import formula.ui._

class FBrowseC(private val p: Pcode) extends MatDetermin {
  protected val em = Env.fem
  private val form2 = em.createQuery("select f from Form2 f join fetch f.common1 where f.pcode = :pcode", classOf[Form2])
    .setParameter("pcode", p.pcode).getSingleResult
  private val q = em.createQuery("select r from Resolvf r where r.pcode = :pcode order by r.mcode", classOf[Resolvf])
  private val r = q.setParameter("pcode", p.pcode).getResultList.toList
  private var ffv = form1ToFormView(form2.common1.toList)
  private var rfv = resolvfToFormView(r)
  private var showingfv = ffv
  private var editing = false
  private val fbv = new FBrowseView(this, p.pcode + " " + p.series + " " + p.name)
  protected val f = fbv // MatDeterminの抽象フィールドを具象化
  private val nf = NumberFormat.getInstance()
  nf.setMinimumFractionDigits(3)
  nf.setMaximumFractionDigits(3)

  setForm2toView()
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
    l.map(r => new FormView(r.mcode, determinByMcode(r.mcode).get, r.percent))

  private class FormTableModel(private val l: List[FormView], private val editable: Boolean)
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
        case None => {
          l(r).m.mname = "該当なし";
          l(r).m.status = 2
        }
        case Some(res) => {
          l(r).m = res;
          l(r).mcode = res.mcode
        }
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

  /**
    * Form2で保持されているデータをviewにセットする
    */
  private def setForm2toView(): Unit = {
    fbv.getPerson.setText(form2.person)
    fbv.getComment.setText(form2.comment)
    fbv.getReason.setText(form2.reason)
    fbv.getDate.setText(DateFormat.getDateInstance.format(form2.date))
    fbv.getSG().setText(nf.format(form2.sg))
    fbv.getPrice().setText(nf.format(price))
  }

  def matStatus(row: Int) = showingfv(row).m.status // not used

  private def total = showingfv.map(_.percent).sum

  private def price = {
    def accumPrice(t: Double, fv: FormView) = t + fv.m.price * fv.percent

    if (showingfv exists {
      _.m.price <= 0
    }) -1.0
    else showingfv.foldLeft(0.0)(accumPrice(_, _)) / total
  }

  /**
    * 分解処方を計算する。<p>
    * 資材コードが500000から600000の範囲にあるものをresolvf表を参照して分解する<p>
    * 比率が0のものを除去することによって、"----------------"などのイリーガルな原料を排除している<p>
    * 原料に自分自身を含む場合、それを除いた残りで分解処方を計算し、最後に比率を全量の比率に換算する
    */
  private def resolv(lfv: List[FormView]) = {
    val decomp = lfv.filter(fv => {
      fv.percent > 0.0 && fv.mcode != p.pcode
    }).flatMap { fv =>
      if (fv.mcode >= 500000 && fv.mcode < 600000)
        resolvfToFormView(q.setParameter("pcode", fv.mcode).getResultList.toList)
          .map(rfv => {
            rfv.percent = fv.percent * rfv.percent / 100;
            rfv
          })
      else List(fv)
    }
    val subTotal = decomp.map(_.percent).sum

    // 分解処方を資材コードごとにまとめ、一つのコードに複数項目があればその比率を加算して一つにまとめる
    def sumPercent(a: FormView, b: FormView) = new FormView(a.mcode, a.m, a.percent + b.percent)

    decomp.groupBy(_.mcode).values.map(_.reduceLeft(sumPercent(_, _))).toList.sortBy(_.mcode)
      .map(fv => {
        fv.percent = fv.percent / subTotal * total;
        fv
      })
  }

  /**
    * 更新される処方に依存する処方の分解処方を計算する
    * 循環参照がある場合、循環しているものの比率が100%でなければ、分解を繰り返すうちに比率はどんどん小さくなる。
    * そのため比率が0.001%よりも小さければ分解を打ち切るようにすれば循環参照があっても問題ない。
    * 実際の打ち切りの閾値は、計算精度を考慮して0.0001%にする
    */
  private def resolvForUpdate(lf: List[Form1]) = {
    def form1ToResolvf(f: Form1) = new Resolvf(f.pcode, f.mcode, f.percent)

    type LR = List[Resolvf]

    def qForm1(pcode: Int) =
      em.createQuery("select f from Form1 f where f.pk.pcode = :pcode", classOf[Form1])
        .setParameter("pcode", pcode).getResultList.toList.map(form1ToResolvf)

    def mplyPercent(percent: Float, lr: LR) = lr.map(r => {
      r.percent = r.percent * percent / 100;
      r
    })

    def decomp(accum: LR, target: LR): LR = {
      val (composit: LR, primitiv: LR) =
        target.filter(_.percent > 0.0001).partition(r => r.mcode >= 500000 && r.mcode < 600000)
      if (composit.size > 0)
        decomp(accum ++ primitiv, composit.flatMap(r => mplyPercent(r.percent, qForm1(r.mcode))))
      else accum ++ primitiv
    }

    def sumPercent(a: Resolvf, b: Resolvf) = new Resolvf(a.pcode, a.mcode, a.percent + b.percent)

    decomp(List.empty, lf.map(form1ToResolvf)).groupBy(_.mcode).values
      .map(_.reduceLeft(sumPercent(_, _))).toList.sortBy(_.mcode)
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

  def copyToClip(dispNorm: Boolean): Unit = ???

  /**
    * 更新権限を設定し、更新を開始する
    */
  def authorize {
    val authorities = em.createQuery("select a from Authorize a", classOf[Authorize]).getResultList.toList
    val names = authorities.map(_.person).toArray
    val ad = new AuthenticationDialog(fbv, names)
    val person = ad.selectedPerson
    if (person != null) {
      form2.reason = ""
      form2.person = person
      setForm2toView()
      fbv.setEditable(true)
      editing = true
      showNorm
    }
  }

  def update {
    //    if (editing) {
    //      fbv.getForm2Data(form2)
    //      if (form2.reason == "" || ffv.exists(_.m.mname == "該当なし")) {
    //        JOptionPane.showMessageDialog(fbv,
    //				    "カラムに不正な値が入っているか、　\n" +
    //				    "または空白にできないカラムが空白になっています　\n\n" +
    //				    "更新理由は記入しましたか？　\n" +
    //				    "更新理由は空白にできません　",
    //				    "値が不正です",
    //				    JOptionPane.WARNING_MESSAGE)
    //      } else {
    //        updateFormula
    //        dependants(p.pcode).foreach(updateResolvf(_))
    //        editing = false
    //        fbv.setEditable(false)
    //        showNorm
    //      }
    //    }
  }

  private def updateFormula: Unit = ???

  private def updateResolvf(pcode: Int): Unit = ???

  def requestClose: Unit = ???

  def showHistory: Unit = ???

  def showLinkList: Unit = ???

  def showPoisonList: Unit = ???

  def showNorm: Unit = {
    showingfv = ffv
    fbv.getTable.setModel(new FormTableModel(ffv, editing))
    fbv.getTotal.setText(nf.format(total))
    fbv.getPrice.setText(nf.format(price))
    fbv.selectShowNormMenu()
  }

  def showDecomp: Unit = {
    showingfv = rfv
    fbv.getTable.setModel(new FormTableModel(showingfv, false))
    fbv.getTotal.setText(nf.format(total))
    fbv.getPrice.setText(nf.format(price))
  }

  def recalcToxicity(): Unit = ???

  def recalcPoison(): Unit = ???

  def print(dispNorm: Boolean): Unit = ???

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

  /**
    * 分解処方の資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
    */
  def getDecompMatStatus(row: Int): Int = rfv(row).m.status

  /**
    * 通常処方の資材のステータスによって表示の色を変えるために、ステータスを読みだせるようにする
    */
  def getNormMatStatus(row: Int): Int = ffv(row).m.status

}

