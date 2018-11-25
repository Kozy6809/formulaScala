package formula

import java.sql.Timestamp
import javax.persistence._

@Entity
@Table(name="arc1")
@Access(AccessType.FIELD)
@IdClass(classOf[Arc1PK])
final class Arc1 extends Common1 {
  @Id
  var pcode: Int =_
  @Id
  var order: Short =_
  @Id
  var date: java.sql.Timestamp =_
  var mcode: Int =_
  @Column(name = "[PERCENT]")
  var percent: Float =_
  type T = Arc2
  @JoinColumns(Array(
    new JoinColumn(name = "PCODE", referencedColumnName = "PCODE", insertable = false, updatable = false),
    new JoinColumn(name = "DATE", referencedColumnName = "DATE", insertable = false, updatable = false)
  ))
  @ManyToOne(optional = false)
  var common2: T = _

  override def hashCode = 41 * (41 * (41 + pcode) + date.hashCode) + order
  override def equals(o:Any) = o match {
    case a: Arc1 => a.pcode == pcode && a.order == order && a.date == date
    case _ => false
  }
}

final class Arc1PK extends Serializable {
  var pcode: Int =_
  var date: Timestamp =_
  var order: Short =_
  override def hashCode = 41 * (41 * (41 + pcode) + date.hashCode) + order
  override def equals(o:Any) = o match {
    case a: Arc1PK => a.pcode == pcode && a.order == order && a.date == date
    case _ => false
  }
}
