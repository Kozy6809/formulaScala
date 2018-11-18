package formula

/**
  Form2とArc2の親クラス
  */
abstract class Common2 {
  var pcode: Int
  var date: java.sql.Timestamp
  var sg: Float
  var person: String
  var comment: String
  var reason: String
  var confDate: java.sql.Timestamp
  type T <: Common1
  var common1: java.util.List[T]

  override def toString() = pcode + " " + date + " " + sg + " " + person + " " +
    comment + " " + reason + " " + confDate

}