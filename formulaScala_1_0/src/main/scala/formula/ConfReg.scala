/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import java.text.DateFormat
import javax.persistence._
import java.awt.datatransfer.StringSelection
import java.sql.Date
import java.sql.Timestamp
import javax.swing.table.AbstractTableModel
import collection.JavaConversions._
import scala.swing.Swing.onEDT


/**
 * 管理部未登録の処方データを表示し、指定された処方を管理部データベースに登録する
 */
class ConfReg {
  private val fem = Env.fem
  private val hem = Env.hem
  private val q = 
    fem.createQuery("select " +
                    "new formula.ConfRegData(f.date,f.pcode,p.series,p.name,a.person,a.id) " +
                    "from Form2 f, Pcode p, Authorize a " +
                    "where f.pcode = p.pcode and f.person = a.person " +
                    "and f.confDate is NULL", classOf[ConfRegData])
//                    "and f.confDate is NULL order by f.date", classOf[ConfRegData])
                    
  private val data = q.getResultList
    
  private class CRTableModel extends AbstractTableModel {
    override def getRowCount = data.size
    override def getColumnCount = 5 // チェック、中間区分、登録日、品名、登録者
    override def getValueAt(row:Int, col:Int) = {
      val r = data.get(row)
      col match {
        case 0 => r.checked.asInstanceOf[AnyRef]
        case 1 => r.medium.asInstanceOf[AnyRef]
        case 2 => r.dateString
        case 3 => r.compName
        case 4 => r.person
      }
    }
    override def setValueAt(o:AnyRef, row:Int, col:Int) {
      val r = data.get(row)
      col match {
        case 0 => r.checked = o.asInstanceOf[Boolean]
        case 1 => r.medium = o.asInstanceOf[Boolean]
      }
    }
    override def isCellEditable(row:Int, col:Int) = col < 2
    override def getColumnClass(c:Int) = getValueAt(0, c).getClass
  }
	
  private val crtm = new CRTableModel()
  private val crv = new formula.ui.ConfRegV(this, crtm);
  crv.pack();
  onEDT {
    crtm.fireTableDataChanged()
    crv.setVisible(true)
  }

  /**
   * データをクリップボードにコピーする。登録日、製造コード、品種、品名、登録者
   */
  def copyToClip() {
    val s = data.mkString("", "\n","\n")
    val cb = crv.getToolkit.getSystemClipboard
    val ss = new StringSelection(s)
    cb.setContents(ss, ss)
  }

  def update() {
    val ft = fem.getTransaction
    val ht = hem.getTransaction
    ft.begin
    ht.begin
    val d = new Timestamp(System.currentTimeMillis)
    for (r <- data if r.checked) {
      updateHolbeinM(r, d)
      val f = fem.find(classOf[Form2], r.pcode)
      f.confDate = d
    }
    Env.commit(ht, crv)
    Env.commit(ft, crv)
    close()
  }
  private def updateHolbeinM(r:ConfRegData, d:Timestamp) {
    // pcodeに一致する処方データを取り出す
    val f =
      fem.createQuery("select f from Form2 f join fetch f.form1 where f.pcode = :pcode", classOf[Form2])
    .setParameter("pcode", r.pcode).getSingleResult
    // 文字列がnullか全て空白文字(全角スペースも含む)ならnullを返す。さもなくばs.trimを返す
    def nullSpace(s:String) = {
      if (s == null) null
      else if (s.replace('　', ' ').trim.length == 0) null
      else s.trim
    }
    f.comment = nullSpace(f.comment)
    // pcodeに一致するPmasterを取り出す。存在しなければ新しいPmasterをpersistし、
    // pcode,name,registerDateをセットする
    val pmaster = {
      val p = hem.find(classOf[Pmaster], r.pcode)
      if (p != null) p
      else {
        val newp = new Pmaster
        hem.persist(newp)
        val nm = 
          fem.createQuery("select p from Pcode p where p.pcode = :pcode", classOf[Pcode])        
        .setParameter("pcode", r.pcode).getSingleResult
        newp.pcode = r.pcode
        newp.name = nm.series +" "+ nm.name
        newp.registerDate = new Date(d.getTime)
        newp
      }
    }
    // pcodeに一致するCmasterを取り出す。存在しなければ新しいCmasterを作成する
    val cmaster = {
      val c = hem.find(classOf[Cmaster], r.pcode)
      if (c != null) c
      else new Cmaster
    }
    // 1 f.commentがnullでcmasterも新規なら何もしない
    // 2 f.commentがnullでcmasterが既存なら、cmaster.commentをnullにする
    // 3 f.commentがnullでなくcmasterが既存ならcmaster.commentをf.commentにする
    // 4 f.commentがnullでなくcmasterが新規ならcmaster.commentをf.commentにしてpersistする
    // cmasterが新規かどうかは、cmaster.pcode == 0で判断する
    if (cmaster.pcode != 0) { // 条件2と3はこれ1つにまとめられる
      cmaster.comment = f.comment
    }
    if (f.comment != null && cmaster.pcode == 0) { // 条件4
      cmaster.pcode = r.pcode
      cmaster.comment = f.comment
      hem.persist(cmaster)
    }
    // FLoatをDoubleに代入する場合に小数点以下3位までの丸め処理を行う
    def float2double(f:Float):Double = java.lang.Math.round(f*1000).toDouble/1000
    // Pmasterに処方データをセットする。sgをFloatからDoubleに変換する際に
    // 小数点以下3位までの丸め処理を行っている
    pmaster.sg = float2double(f.sg)
    pmaster.isMedium = if (r.medium) 1 else 0
    val a = fem.createQuery("select a from Authorize a where a.person = :person", classOf[Authorize])
    .setParameter("person", f.person).getSingleResult
    pmaster.publisherID = a.id
    pmaster.updateDate = d
    
    // 製造処方マスタからpcodeに一致する新製造コードを取り出す。無ければ0
    val pcodeNew = {
      val f = hem.find(classOf[Fmaster], new FmasterPK(r.pcode, 1))
      if (f == null) 0 else f.pcodeNew
    }
    // 製造処方マスタからpcodeに一致するデータを削除する
    hem.createQuery("delete from Fmaster f where f.pk.pcode = :pcode")
    .setParameter("pcode", r.pcode).executeUpdate
    // f.Form1からFmasterを生成する。percentをFloatからDoubleに変換する際に
    // 小数点以下3位までの丸め処理を行っている
    def form1toFmaster(f:Form1):Fmaster = 
      new Fmaster(new FmasterPK(f.pk.pcode, f.pk.order), pcodeNew, f.mcode,
                  float2double(f.percent))

    f.form1.map(form1toFmaster).foreach(hem.persist)
  }
  def close() {
    crv.dispose
    fem.close
    hem.close
  }
}

class ConfRegData(
  var checked:Boolean, //登録対象
  var medium: Boolean, // 中間品かどうか
  val date:Timestamp,
  val pcode:Int,
  val series:String,
  val name:String,
  val person:String,
  val empID:Int){
  def this(date:Timestamp,pcode:Int,series:String,name:String,person:String,empID:Int) =
    this(false,false,date,pcode,series,name,person,empID)
  def compName = pcode +" "+ series +" "+ name
  def dateString = DateFormat.getDateInstance.format(date)
  override def toString = dateString +"\t"+ pcode +"\t"+ series +"\t"+ name +"\t"+ person
}
