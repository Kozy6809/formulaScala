/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula
import javax.persistence._

@Entity
@Table(name="flink")
@Access(AccessType.FIELD)
final class Flink {
  var linkID: Int =_
  @Id
  var pcode: Int =_

  override def hashCode = linkID
  override def equals(o:Any) = o match {
    case f: Flink => f.linkID == linkID
    case _ => false
  }

}
