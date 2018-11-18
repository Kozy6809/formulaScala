/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import java.sql.Timestamp

import javax.persistence._

@Entity
@Table(name="arc2")
@Access(AccessType.FIELD)
@IdClass(classOf[Arc2PK])
final class Arc2 extends Common2 {
  @Id
  var pcode: Int =_
  @Id
  var date: java.sql.Timestamp =_
  var sg: Float =_
  var person: String =_
  var comment: String =_
  var reason: String =_
  var confDate: Timestamp =_
  type T = Arc1
  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "common2")
  var common1: java.util.List[T] =_

  override def hashCode = 41 * (41 + pcode) + date.hashCode
  override def equals(o: Any)  = o match {
    case a: Arc2 => a.pcode == pcode && a.date == date
    case _ => false
  }
}

final class Arc2PK extends Serializable {
  var pcode:Int = _
  var date: Timestamp =_
  override def hashCode = 41 * (41 + pcode) + date.hashCode
  override def equals(o:Any) = o match {
    case a:Arc2PK => a.pcode == pcode && a.date == date
    case _ => false
  }
}
