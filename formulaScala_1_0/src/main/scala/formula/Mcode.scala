/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

@Entity
@Table(name="mcode")
@Access(AccessType.FIELD)
class Mcode {
  @Id
  var mcode:Int =_
  var mname:String = ""
  var status:Short =_
  var price:Float =_
  var date:java.sql.Timestamp =_
  override def hashCode = mcode
  override def equals(o:Any) = o match {
    case m:Mcode => m.mcode == mcode
    case _ => false
  }
}
