/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

@Entity
@Table(name="form1")
@Access(AccessType.FIELD)
final class Form1 {
  @EmbeddedId
  var pk:Form1PK = new Form1PK
  var date:java.sql.Timestamp =_
  var mcode:Int =_
  var percent:Float =_
  @JoinColumn(name = "PCODE", referencedColumnName = "PCODE", insertable = false, updatable = false)
  @ManyToOne(optional = false)
  var form2:Form2 = _

  override def toString = pk.pcode +" "+ date +" "+ pk.order +" "+
  mcode +" "+ percent
  override def hashCode = pk.hashCode
  override def equals(o:Any) = o match {
    case f:Form1 => f.pk equals pk
    case _ => false
  }
}

@Embeddable
final class Form1PK(p:Int, o:Short) {
  var pcode:Int = p
  var order:Short = o
  def this() = this(0,0)
  override def hashCode = 41 * (41 + pcode) + order
  override def equals(o:Any) = o match {
    case f:Form1PK => f.pcode == pcode && f.order == order
    case _ => false
  }
}
