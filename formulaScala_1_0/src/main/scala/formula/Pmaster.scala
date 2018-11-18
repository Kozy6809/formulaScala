/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="mtbl製造マスタ")
@Access(AccessType.FIELD)
/**
 * holbeinMasterM.mtbl製造マスタのエンティティクラス<p>
 */
final class Pmaster {
  @Id
  @Column(name = "製造コード旧")
  var pcode:Int =_ // 技術データ
  @Column(name = "製造コード")
  var pcodeNew:Int =_
  @Column(name = "製造名")
  var name:String =_ // 技術データ 品種名込みの色名
  @Column(name = "比重")
  var sg:Double =_ // 技術データ
  @Column(name = "中間区分")
  var isMedium:Short =_ // 技術データにすべきか?
  @Column(name = "処方発行者")
  var publisherID:Int =_ // 技術データ
  @Column(name = "登録年月日")
  var registerDate:java.sql.Date =_ // 新規の場合は記入する
  @Column(name = "更新日時")
  var updateDate:java.sql.Timestamp =_ // 技術データ
  @Column(name = "SERIES")
  var priceSeries:String =_
  @Column(name = "製品種別コード")
  var productKindCode:String =_
  @Column(name = "製造部署コード")
  var factoryCode:Short =_
  @Column(name = "同月区分")
  var sameMonth:Short =_
  @Column(name = "社員コード")
  var employeeID:Short =_
  @Column(name = "機械１")
  var m1:String =_
  @Column(name = "機械２")
  var m2:String =_
  @Column(name = "機械３")
  var m3:String =_
  @Column(name = "製造単位")
  var productionAmount:Double =_
  @Column(name = "最低単位")
  var minimumAmount:Double =_
  @Column(name = "優先順")
  var priority:Int =_
  @Column(name = "中間予備在庫量")
  var spareStockAmount:Int =_
  @Column(name = "伝票フラグ")
  var invoiceFlag:Short =_
  @Column(name = "注意フラグ")
  var cautionFlag:Short =_
  @Column(name = "標準時間")
  var stdTime:Double =_
  @Column(name = "標準ロール")
  var stdRoll:Int =_
  @Column(name = "標準回数")
  var stdPathTime:Short =_
  @Column(name = "登録区分")
  var registerSpec:Short =_
  @Column(name = "変更年月日")
  var alterDate:java.sql.Date =_
  @Column(name = "印刷日時")
  var printDate:java.sql.Timestamp =_
  @Column(name = "コンピュータ名")
  var computerName:String =_
  override def toString = pcode+" "+pcodeNew+" "+name+" "+sg+" "+priceSeries+" "+
  productKindCode+" "+factoryCode+" "+sameMonth+" "+employeeID+" "+
  m1+" "+m2+" "+m3+" "+productionAmount+" "+minimumAmount+" "+
  priority+" "+isMedium+" "+spareStockAmount+" "+invoiceFlag+" "+
  cautionFlag+" "+publisherID+" "+stdTime+" "+stdRoll+" "+
  stdPathTime+" "+registerSpec+" "+registerDate+" "+alterDate+" "+
  updateDate+" "+printDate+" "+computerName
  override def hashCode = pcode
  override def equals(o:Any) = o match {
    case p:Pmaster => p.pcode == pcode
    case _ => false
  }
}
