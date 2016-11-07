/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.playing.reflection
import scala.reflect.runtime.universe._


/**  Playing with sub types
  * Created by CAB on 07.11.2016.
  */

object SubType extends App {
  println("==== SubType ====")
  //Classes
  trait T
  trait S extends T
  trait R extends S
  trait P{
    val ms = runtimeMirror(getClass.getClassLoader).classSymbol(getClass).toType.members.filter{ m ⇒
      val isT = m.typeSignature match {
        case tpe if tpe <:< typeOf[T] ⇒ true
        case NullaryMethodType(tpe) if tpe <:< typeOf[T] ⇒ true
        case MethodType(Nil, tpe) if tpe <:< typeOf[T] ⇒ true
        case _ ⇒ false}
      isT && !(m.isPrivate ||  m.isProtected)}
    println(ms)}
  trait A extends P{
    val a: Int = 0
    private val t: T = new T{}
  }
  trait B extends A{
    val b: Double = 0
    protected val s: S = new S{}
  }
  trait C extends B {
    val c: String = ""
    val r = new R{}
    var v: R = new R{}
    def gr: R = r
    def sr(n: R): Unit = {}

  }
  class Z extends C
  //
  val z = new Z
//  z.validate()

}
