package formula
import scala.collection.JavaConversions._
import formula.ui.MatDeterminDialog
import javax.persistence._
import javax.swing.AbstractListModel

/**
 * 資材コードまたは資材名から検索を実行し、資材を確定する。	MainCとFBrowseCの両方で使用する
 */
trait MatDetermin {
  protected val f: java.awt.Frame // ダイアログの親コンポーネント
  protected val em: EntityManager

  private def queryByMcode(code: Int): java.util.List[Mcode]  =
    em.createQuery("select m from Mcode m where m.mcode = :mcode", classOf[Mcode])
      .setParameter("mcode", code).getResultList.toList

  private def queryByMname(name: String): java.util.List[Mcode] =
    em.createQuery("select m from Mcode m where m.mname like :mname order by m.mname", classOf[Mcode])
      .setParameter("mname", name).getResultList.toList

  private class ResultListModel(data: Array[Mcode]) extends AbstractListModel[String] {
    def getElementAt(index: Int): String = {
      if (data == null) null
      else {
        val arow = data(index)
        arow.mcode +" "+ arow.mname
      }
    }

    def getSize: Int = if (data == null) 0 else data.size
  }

  protected def determinByMcode(code: Int) = {
    val r = queryByMcode(code)
    em.clear()
    r.size match {
      case 0 => None
      case 1 => Some(r(0))
    }
  }
  protected def determinByMname(name: String): Option[Mcode] = {
    val r = queryByMname(name)
    em.clear()
    r.size match {
      case 0 => None
      case 1 => Some(r(0).asInstanceOf[Mcode])
      case _ => {
        val md = new MatDeterminDialog(f, true)
        md.getList.setModel(new ResultListModel(r.map(_.asInstanceOf[Mcode]).toArray))
        md.pack()
        md.setVisible(true)
        val ix = md.getList.getSelectedIndex
        if (ix < 0) None else Some(r(ix))
      }
    }

  }

}