/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import java.awt.Component
import javax.persistence._
import javax.swing.JOptionPane


/**
 * アプリケーション実行環境を支持する。具体的にはJPA環境
 */
object Env {
  val femf = Persistence.createEntityManagerFactory("formulaPU")
  val hemf = Persistence.createEntityManagerFactory("holbeinmPU")
  def fem = femf.createEntityManager()
  def hem = hemf.createEntityManager()

  def setup(){}
  /**
   * コミットを実行し、ロールバック例外が発生した場合はエラーメッセージを表示する
   */
  def commit(t:EntityTransaction, c:Component) {
    try {
      t.commit
    } catch {
      case e:RollbackException => {
          JOptionPane.showMessageDialog(c, "コミットできませんでした")
          e.printStackTrace
      }
    }
  }
}
