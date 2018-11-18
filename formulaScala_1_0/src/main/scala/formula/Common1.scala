package formula

/**
  Form1とArc1の親クラス
  */
abstract class Common1 {
  var pcode: Int
  var order: Short
  var date: java.sql.Timestamp
  var mcode: Int
  var percent: Float
  type T <: Common2
  var common2: T
  override def toString = pcode +" "+ date +" "+ order +" "+
    mcode +" "+ percent

}