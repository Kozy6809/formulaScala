/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="resolvf")
@Access(AccessType.FIELD)
@IdClass(classOf[ResolvfPK])
final class Resolvf(pcode_ : Int, mcode_ : Int, percent_ : Float) {
  @Id
  var pcode: Int = pcode_
  @Id
  var mcode: Int = mcode_
  @Column(name = "[PERCENT]")
  var percent: Float = percent_

  def this() = this(0, 0, 0.0f)
  override def toString() = pcode +" "+ mcode +" "+ percent
  override def hashCode = 41 * (41 + pcode) + mcode
  override def equals(o:Any) = o match {
    case a: Resolvf => a.pcode == pcode && a.mcode == mcode
    case _ => false
  }
}

final class ResolvfPK extends Serializable {
  var pcode: Int =_
  var mcode: Int =_
  override def hashCode = 41 * (41 + pcode) + mcode
  override def equals(o:Any) = o match {
    case a: ResolvfPK => a.pcode == pcode && a.mcode == mcode
    case _ => false
  }
}
