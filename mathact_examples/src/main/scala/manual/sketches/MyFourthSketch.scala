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

import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench


/** Wiring example.
  * Created by CAB on 03.01.2017.
  */

class MyFourthSketch extends SimpleWorkbench with HelperBlocks{
  //OOP style
  object ObjIntPrinter extends EmptyBlock with ObjWiring with LinkThrough[Int, String]{
    //Wiring
    private val outflow = new Outflow[String] {
      def send(str: String): Unit = pour(str)
    }
    private val inflow = new Inflow[Int] {
      protected def drain(v: Int): Unit = {
        outflow.send("Converted" + v.toString)
      }
    }
    //Connection points
    val in = Inlet(inflow)
    val out = Outlet(outflow)
  }
  //Functional style
  object FunIntPrinter extends EmptyBlock with FunWiring with LinkThrough[Int, String]{
    //Connection points
    val in = In[Int]
    val out = Out[String]
    //Wiring
    in.map(v ⇒ "Converted" + v.toString) >> out
  }
  //Connecting
  Generator ~> ObjIntPrinter ~> Logger
  Generator ~> FunIntPrinter ~> Logger
}
