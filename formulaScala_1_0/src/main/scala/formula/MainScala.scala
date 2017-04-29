/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formula

import javax.persistence._

/**
 * 製造伝票を閲覧するためのURL一覧を生成する
 */
object MainScala {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    val emf = Persistence.createEntityManagerFactory("formulaPU")
    val em = emf.createEntityManager
//    val q = em.createQuery("select new ivview.Cmaster(c.ccode, c.cname) from ConvertQC c")
//    val r = q.getResultList
//    println(r)
//    val c = em.find(classOf[ConvertQC], 500013)
//    println(c)
//    val q = em.createNamedQuery("findByNative")
//    val r = q.setParameter(1, "CL").getResultList
//    println(r)
//    val q = em.createNamedQuery("findByCcode")
//    val r = q.setParameter("ccode", "CL").getResultList
//    println(r)
//    val c = em.find(classOf[Cmaster], 500013)
//    println(c)
//    val q = em.createQuery("select f from Fmaster f where f.pk.pcode = 500013")
//    println(q.getResultList)
//    val c = em.find(classOf[Fmaster], new FmasterPK(500013,1))
//    println(c)
    val c = em.find(classOf[Form2], 500013)
    println(c)
  }

}

// case class Cmaster(var ccode:String, var cname:String) // 色名マスタ
