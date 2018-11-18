/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="toxMcode")
@Access(AccessType.FIELD)
final class ToxMcode {
  @EmbeddedId
  var pk: ToxMcodePK =_
  var percent: Float =_

  override def toString() = pk.mcode +" "+ pk.toxNo +" "+ percent
  override def hashCode = pk.hashCode
  override def equals(o:Any) = o match {
    case f: ToxMcode => f.pk == pk
    case _ => false
  }
}

@Embeddable
final class ToxMcodePK {
  var mcode: Int =_
  var toxNo: String =_

  override def hashCode = 41 * mcode + toxNo.hashCode
  override def equals(o:Any) = o match {
    case a: ToxMcodePK => a.mcode == mcode && a.toxNo == toxNo
    case _ => false
  }
}
