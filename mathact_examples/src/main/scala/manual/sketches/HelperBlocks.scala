/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package manual.sketches

import mathact.core.bricks.linking.{LinkIn, LinkOut}
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scala.concurrent.Future


/** Helper blocks to use in examples
  * Created by CAB on 03.01.2017.
  */

trait HelperBlocks { _: SimpleWorkbench ⇒
  //Int value generator
  object Generator extends EmptyBlock with ObjWiring with ObjOnStart with LinkOut[Int]{ name = "Int Generator"
    private val gen = new Outflow[Int] {
      def start(): Unit = Future{
        (0 to 10).foreach{ i ⇒
          pour(i)
          Thread.sleep(1000)}}}
    protected def onStart(): Unit = gen.start()
    val out = Outlet(gen) }
  //String logger
  object Logger extends EmptyBlock with FunWiring with LinkIn[String]{ name = "Logger"
    val in = In[String]
    in.foreach(v ⇒ logger.info("Logger: " + v))}}
