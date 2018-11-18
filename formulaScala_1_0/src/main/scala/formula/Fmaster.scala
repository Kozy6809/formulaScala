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
final class Fmaster(_pk: FmasterPK, _pcodeNew: Int, _mcode: Int, _percent: Double) {
  @EmbeddedId
  var pk:FmasterPK = _pk
  @Column(name = "製造コード")
  var pcodeNew: Int = _pcodeNew
  @Column(name = "処方コード")
  var mcode: Int = _mcode // 技術データ
  @Column(name = "比率")
  var percent: Double = _percent // 技術データ
  def this() = this(null, 0, 0, 0.0)
  override def toString = pk.pcode+" "+pk.order+" "+pcodeNew+" "+mcode+" "+percent
  override def hashCode = pk.hashCode
  override def equals(o: Any) = o match {
    case f: Fmaster => f.pk equals pk
    case _ => false
  }
}

@Embeddable
final class FmasterPK(_pcode: Int, _order: Short) {
  @Column(name = "製造コード旧")
  var pcode:Int = _pcode // 技術データ
  @Column(name = "処方順")
  var order: Short = _order // 技術データ
  def this() = this(0, 0)
  override def hashCode = 41 * pcode + order
  override def equals(o: Any) = o match {
    case f: FmasterPK => f.pcode == pcode && f.order == order
    case _ => false
  }
}
