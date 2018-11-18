/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="form2")
@Access(AccessType.FIELD)
final class Form2 extends Common2 {
  @Id
  var pcode:Int =_
  var date :java.sql.Timestamp =_
  var sg:Float =_
  var person:String =_
  var comment:String =_
  var reason:String =_
  var confDate:java.sql.Timestamp =_
  type T = Form1
  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "common2")
  var common1:java.util.List[T] =_

  override def hashCode = pcode
  override def equals(o:Any) = o match {
    case f:Form2 => f.pcode == pcode
    case _ => false
  }
}
