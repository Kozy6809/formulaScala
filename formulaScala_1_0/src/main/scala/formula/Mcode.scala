/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="mcode")
@Access(AccessType.FIELD)
final class Mcode {
  @Id
  var mcode: Int =_
  var mname: String =_
  var status: Int =_
  var price: Float =_
  var date : java.sql.Timestamp =_

  override def toString() = mcode +" "+ mname +" "+ status +" "+ price +" "+ date
  override def hashCode = mcode
  override def equals(o:Any) = o match {
    case f: Mcode => f.mcode == mcode
    case _ => false
  }
}
