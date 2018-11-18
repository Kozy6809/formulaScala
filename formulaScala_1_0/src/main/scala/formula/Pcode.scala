/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="pcode")
@Access(AccessType.FIELD)
class Pcode {
  var obsolete:Short =_
  @Id
  var pcode:Int =_
  var series:String =_
  var name:String =_
  override def hashCode = pcode
  override def equals(o:Any) = o match {
    case p:Pcode => p.pcode == pcode
    case _ => false
  }

}
