package formula
import scala.collection.JavaConversions._
import formula.ui.MatDeterminDialog
import javax.persistence.EntityManager

/**
 * 資材コードまたは資材名から検索を実行し、資材を確定する。	MainCとFBrowseCの両方で使用する
 */
trait MatDetermin {
  protected val f: java.awt.Frame // ダイアログの親コンポーネント
  protected val em: EntityManager
  private def queryByMcode(code: Int) =
    em.createQuery("select m from Mcode m where m.mcode = :mcode", classOf[Mcode])
      .setParameter("mcode", code).getResultList
  private def queryByMname(name: String) =
    em.createQuery("select m from Mcode m where m.mname like :mname order by m.mname", classOf[Mcode])
      .setParameter("mname", name).getResultList
  protected def determinByMcode(code: Int) = {
    val r = queryByMcode(code)
    em.clear()
    r.size match {
      case 0 => None
      case 1 => Some(r(0))
    }
  }
  protected def determinByMname(name: String) = {
    val r = queryByMname(name)
    em.clear()
    r.size match {
      case 0 => None
      case 1 => Some(r(0))
      case _ => {
        val s = r.map(_.mname).toArray
        val md = new MatDeterminDialog(f)
        // TODO mdにデータモデルをセットする必要あり md.getList.setModel(...)
        md.pack()
        md.setVisible(true)
        val ix = md.selection
        if (ix < 0) None else Some(r(ix))
      }
    }

  }

}