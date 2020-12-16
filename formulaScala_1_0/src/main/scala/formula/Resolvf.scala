/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

@Entity
@Table(name="resolvf")
@Access(AccessType.FIELD)
final class Resolvf {
  @EmbeddedId
  var pk:ResolvfPK = new ResolvfPK
  var percent:Float =_
  override def hashCode = pk.hashCode
  override def equals(o:Any) = o match {
    case r:Resolvf => r.pk equals pk
    case _ => false
  }
  def this(pcode:Int, mcode:Int, percent:Float) = {
    this()
    pk.pcode = pcode
    pk.mcode = mcode
    this.percent = percent
  }
}

@Embeddable
final class ResolvfPK {
  var pcode:Int =_
  var mcode:Int =_
  override def hashCode = 41 * (41 + pcode) + mcode
  override def equals(o:Any) = o match {
    case r:ResolvfPK => r.pcode == pcode && r.mcode == mcode
    case _ => false
  }

}