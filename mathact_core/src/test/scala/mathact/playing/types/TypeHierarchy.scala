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

package mathact.playing.types


/** Playing with types hierarchy
  * Created by CAB on 03.12.2016.
  */

object TypeHierarchy extends App{
  println("==== TypeHierarchy ====")
  //
  trait Timed[V <: Timed[V]]{ _: V ⇒
    val time: Long
    def time(t: Long): V



  }





  case class TE(v: Double, time: Long) extends Timed[TE]{

    def time(t: Long): TE = copy(time = t)



  }




  def inc[T <: Timed[T]](v: T): T  = {

    val t = v.time(v.time + 1)

    t

  }

  val t1 = TE(1, 1)
  val t2 = inc(t1)

  println(t2)



}
