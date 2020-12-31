package formula.ui

import formula.MainC
import myui.NumberField

import javax.swing.{JFrame, JTextField}

/**
 * Scalaバージョンのメインビュー。まだMainCのコンパイルを通すための最低限のモック状態
 * @param mc
 */
class MainV(val mc: MainC) extends JFrame {
  def setResultModel(model: MainC#ResultListModel) = ???

  def setSeriesModel(model: MainC#SeriesListModel) = ???

  def getCodeField: NumberField = ???

  def getNameField: JTextField = ???

}
