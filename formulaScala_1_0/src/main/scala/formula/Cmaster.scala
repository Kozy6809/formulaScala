/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

@Entity
@Table(name="mtbl製造備考マスタ")
@Access(AccessType.FIELD)
/**
 * holbeinMasterM.mtbl製造備考マスタのエンティティクラス。
 * 元表にはプライマリキーが設定されていない
 */
final class Cmaster {
  @Column(name = "製造コード")
  var pcodeNew:Int =_
  @Id
  @Column(name = "製造コード旧")
  var pcode:Int =_
  @Column(name = "備考")
  var comment:String =_
  override def toString = pcodeNew +" "+ pcode +" "+ comment
  override def hashCode = pcode
  override def equals(o:Any) = o match {
    case p:Cmaster => p.pcode == pcode
    case _ => false
  }
}
