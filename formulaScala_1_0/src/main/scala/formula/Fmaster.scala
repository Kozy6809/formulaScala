/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

/**
 * holbeinMasterM.mtbl製造処方マスタのエンティティクラス
 */
@Entity
@Table(name="mtbl製造処方マスタ")
@Access(AccessType.FIELD)
final class Fmaster(pk_ :FmasterPK, pcodeNew_ :Int, mcode_ :Int, percent_ :Double) {
  @EmbeddedId
  var pk:FmasterPK = pk_
  @Column(name = "製造コード")
  var pcodeNew:Int = pcodeNew_
  @Column(name = "処方コード")
  var mcode:Int = mcode_ // 技術データ
  @Column(name = "比率")
  var percent:Double = percent_ // 技術データ
  def this() = this(new FmasterPK,0,0,0.0)
  override def toString = pk.pcode+" "+pk.order+" "+pcodeNew+" "+mcode+" "+percent
  override def hashCode = pk.hashCode
  override def equals(o:Any) = o match {
    case f:Fmaster => f.pk equals pk
    case _ => false
  }
}

@Embeddable
final class FmasterPK(p:Int, o:Short) {
  @Column(name = "製造コード旧")
  var pcode:Int = p // 技術データ
  @Column(name = "処方順")
  var order:Short = o // 技術データ
  def this() = this(0,0)
  override def hashCode = 41 * (41 + pcode) + order
  override def equals(o:Any) = o match {
    case f:FmasterPK => f.pcode == pcode && f.order == order
    case _ => false
  }
}
