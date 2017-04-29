/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

@Entity
@Table(name="authorize")
@Access(AccessType.FIELD)
final class Authorize {
  var person:String =_
  var kana:String =_
  var password:String =_
  @Id
  var id:Int =_
  
  override def hashCode = id
  override def equals(o:Any) = o match {
    case a:Authorize => a.id == id
    case _ => false
  }

}
