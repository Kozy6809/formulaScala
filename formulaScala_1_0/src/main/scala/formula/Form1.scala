package formula
import javax.persistence._

@Entity
@Table(name="form1")
@Access(AccessType.FIELD)
@IdClass(classOf[Form1PK])
final class Form1 extends Common1 {
  @Id
  var pcode: Int =_
  @Id
  @Column(name = "[ORDER]")
  var order: Short =_
  var date:java.sql.Timestamp =_
  var mcode:Int =_
  @Column(name = "[PERCENT]")
  var percent:Float =_
  type T = Form2
  @JoinColumn(name = "PCODE", referencedColumnName = "PCODE", insertable = false, updatable = false)
  @ManyToOne(optional = false)
  var common2: T = _

  override def hashCode = 41 * pcode + order
  override def equals(o:Any) = o match {
    case f:Form1 => f.pcode == pcode && f.order == order
    case _ => false
  }
}

final class Form1PK extends Serializable {
  var pcode:Int =_
  var order:Short =_
  override def hashCode = 41 * (41 + pcode) + order
  override def equals(o:Any) = o match {
    case f:Form1PK => f.pcode == pcode && f.order == order
    case _ => false
  }
}
