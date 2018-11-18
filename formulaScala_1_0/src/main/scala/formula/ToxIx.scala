/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="toxix")
@Access(AccessType.FIELD)
final class ToxIx {
  @Id
  var toxNo: String =_
  var chemical: String =_
  var threshold: Float =_

  override def toString() = toxNo +" "+ chemical +" "+ threshold
  override def hashCode = toxNo.hashCode
  override def equals(o:Any) = o match {
    case f: ToxIx => f.toxNo == toxNo
    case _ => false
  }
}
