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


/** Playing with sub types
  * Created by CAB on 07.11.2016.
  */

object SubType extends App {
  println("==== SubType ====")
  //Bricks
  trait D
  trait F[T]
  trait P[T] extends F[T]{
    def k(s:  S[T]): Unit = {}}
  trait S[T] extends F[T]{
    def l(p:  P[T]): Unit = {}}
  //Blocks
  trait B1 extends B{
    //Public
    val p = new P[Double]{}
    //Hidden
    val v1 = 10
    val u1 = ""
    private val up1 = ""
    protected val up2 = ""
    def f1(i: Int): Unit = {}}
  trait B2 extends B{
    //Public
    val s = new S[Double]{}
    def sp(p: P[Double]): Unit = {}
    def gp: P[Double] = ???
    //Hidden
    def sph(p: P[Double], v: Int): Unit = {}
    def gph: (P[Double], Int) = ???
    val v2 = 10
    def f2(i: Int): Unit = {}}
  //Sketch
  class Sk {
    val b1 = new B1{}
//    val b2 = new B2{
//      //DSL
//      sp(b1.p)
//      //Hidden
//      val v3 = 10
//      def f3(i: Int): Unit = {}
//    }
//    b1.p.k(b2.s)
//    b2.s.l(b1.p)
  }
  //Run
  val sk = new Sk
  //Base
  trait B {



    //Internal
    val iv: Int = 0
    def im(v: Int): Int = 0
    //Check
    println("\n####### " + getClass.getTypeName)

    val dsa = runtimeMirror(getClass.getClassLoader).classSymbol(getClass).toType.decls.filter{ d ⇒
      println(d)
      println(d.isConstructor)
      println(d.isImplementationArtifact)
      println(d.isSpecialized)
      println(d.isStatic)
      println(d.isTerm)



      true

    }

    println("+++++++ dsa: " + dsa)

    private val ds = typeOf[B].decls

    println("+++++++ ds: " + ds)


    //
//    private val bms = typeOf[B].members.map(_.fullName).toSet
//    println("+++++++ bms: " + bms)
//    private val ms1 = runtimeMirror(getClass.getClassLoader).classSymbol(getClass).toType.members.filter{ m ⇒
//      ! bms.contains(m.fullName)}
//    println("+++++++ ms1: " + ms1)
//
//    private val ms2 = ms1.filter{ m ⇒
//      def cht(s: Symbol): Boolean = s.typeSignature match {
//        case tpe if tpe <:< typeOf[F[_]] ⇒ true
//        case NullaryMethodType(tpe) if tpe <:< typeOf[F[_]] ⇒ true
//        case MethodType(Nil, tpe) if tpe <:< typeOf[F[_]] ⇒ true
//        case _ ⇒ false}
//      ! m.overrides.exists(s ⇒ cht(s))}
//    println("+++++++ ms2: " + ms2)
//
//    private val ms3 = ms2.filter{ m ⇒
//      !(m.isPrivate || m.isProtected || m.isConstructor || m.isSynthetic)}
//    println("+++++++ ms3: " + ms3)
//    println("+++++++ ms3: " + ms3.size)
//
//    val bms = classOf[B].getDeclaredMethods.toList.map(_.getName)
//    val bfs = classOf[B].getDeclaredFields.toList.map(_.getName)
//
//    println("=== bms:\n" + bms.mkString("\n"))
////    println("bfs:\n" + bfs.mkString("\n"))
//
//    private val ms = getClass.getDeclaredMethods.toList
////    private val fs = getClass.getDeclaredFields.toList
//
//    println("=== ms:\n" + ms.mkString("\n"))
////    println("fs:\n" + fs.mkString("\n"))
//
//    private val ms1 = ms.filter{ m ⇒
//
//      ! bms.contains(m.getName)
//
//
//
//    }
//
//    println("=== ms1:\n" + ms1.mkString("\n"))
//
//
//
//    private val ms2 = ms1.filterNot{ m ⇒
//      println("+++++: " + m)
//
//      val pts = m.getParameterTypes
//
//      val fss = pts.nonEmpty && m.getParameterTypes.forall{ a ⇒ classOf[F[_]].isAssignableFrom(a)}
//
//      println("----: " + fss)
//
//
////        val fss = m.getParameterTypes.forall{ a ⇒
////
////        println("----: " + a)
////
////        println("$: " + classOf[F[_]].isAssignableFrom(a))
////
////
////
//////        ! classOf[F[_]].isAssignableFrom(a)
////
////        classOf[F[_]].isAssignableFrom(a)
////
////      }
//
////      m.getParameterTypes.toList.forall(a ⇒ classOf[F[_]].isAssignableFrom(a)) ||
//
//
//      fss || classOf[F[_]].isAssignableFrom(m.getReturnType) ||
//      Modifier.isPrivate(m.getModifiers) || Modifier.isProtected(m.getModifiers)
//
//      //      ! (m.isSynthetic || Modifier.isPrivate(m.getModifiers)|| Modifier.isProtected(m.getModifiers))
//
//
//
//    }
//
//    println("=== ms2:\n" + ms2.mkString("\n"))
//    val ms = typeOf[B].members
//
//
//    println("ms: " + ms)
//
//
//    val fs = getClass.getMethods.toList
//
//    fs.foreach{ f ⇒ if(f.getName == "p"){
//
//
//      println(f)
//
//
//    }
//
//
//
//
//    }
//
//
//
//    val msa = runtimeMirror(getClass.getClassLoader).classSymbol(getClass).toType.members
//
//
//
//    msa.foreach{ m ⇒ if(m.name.toString == "p"){
//
//
//      def cht(s: Symbol): Boolean = s.typeSignature match {
//        case tpe if tpe <:< typeOf[F[_]] ⇒ true
//        case NullaryMethodType(tpe) if tpe <:< typeOf[F[_]] ⇒ true
//        case MethodType(Nil, tpe) if tpe <:< typeOf[F[_]] ⇒ true
//        case _ ⇒ false}
//
//
//      println("========== " + m)
//
//      println(m.name)
//      println(m.fullName)
//      println(m.typeSignature)
//      println(m.info)
//      println(m.alternatives.map(_.typeSignature))
//      println(m.overrides.map(s ⇒ cht(s)))
//      println(m.typeSignature <:< typeOf[P[Double]])
//
//
//
//
//    }}
//
//    println("msa: " + msa)
  }
//  trait T
//  trait S extends T
//  trait R extends S
//  trait P{
//    val ms = runtimeMirror(getClass.getClassLoader).classSymbol(getClass).toType.members.filter{ m ⇒
//      val isT = m.typeSignature match {
//        case tpe if tpe <:< typeOf[T] ⇒ true
//        case NullaryMethodType(tpe) if tpe <:< typeOf[T] ⇒ true
//        case MethodType(Nil, tpe) if tpe <:< typeOf[T] ⇒ true
//        case _ ⇒ false}
//      isT && !(m.isPrivate ||  m.isProtected)}
//    println(ms)}
//  trait A extends P{
//    val a: Int = 0
//    private val t: T = new T{}
//  }
//  trait B extends A{
//    val b: Double = 0
//    protected val s: S = new S{}
//  }
//  trait C extends B {
//    val c: String = ""
//    val r = new R{}
//    var v: R = new R{}
//    def gr: R = r
//    def sr(n: R): Unit = {}
//
//  }
//  class Z extends C
//  //
//  val z = new Z
//  z.validate()





}
