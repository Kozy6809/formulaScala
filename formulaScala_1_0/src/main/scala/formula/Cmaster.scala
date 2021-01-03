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
  @Id
  @Column(name = "製造コード旧")
  var pcode:Int =_
  @Column(name = "備考")
  var comment:String =_
  @Column(name = "備考1")
  var comment1:String =_
  @Column(name = "備考2")
  var comment2:String =_
  @Column(name = "備考3")
  var comment3:String =_
  @Column(name = "備考4")
  var comment4:String =_
  @Column(name = "備考5")
  var comment5:String =_

  override def toString = pcode +" "+ comment +" "+ comment1 +" "+ comment2 +" "+ comment3 +" "+ comment4 +" "+ comment5
  override def hashCode = pcode
  override def equals(o:Any) = o match {
    case p:Cmaster => p.pcode == pcode
    case _ => false
  }
}
