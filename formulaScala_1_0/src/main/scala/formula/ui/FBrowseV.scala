package formula.ui

import formula.{FBrowseC, Form2}

import javax.swing.JFrame

/**
 * Scalaバージョンの処方ブラウザビュー。まだFBrowseCのコンパイルを通すための最低限のモック状態
 * @param c
 * @param title
 */
class FBrowseV(val c: FBrowseC, val t: String) extends JFrame {
  def selectShowNormMenu(): Unit = ???

  def setPrice(price: Double) = ???

  def setTotal(total: Float) = ???

  type Ftm = FBrowseC#FormTableModel
  def setModel(model: Ftm) = ???

  def getForm2Data(form2: Form2) = ???

  def setEditable(bool: Boolean) = ???

  def setForm2Data(form2: Form2) = ???

}
