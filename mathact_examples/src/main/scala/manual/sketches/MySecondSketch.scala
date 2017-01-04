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

import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench


/** My second sketch
  * Created by CAB on 31.12.2016.
  */

class MySecondSketch extends SimpleWorkbench {
  //Blocks
  class BlockB extends EmptyBlock with FunWiring {
    //Connection points
    val in1 = In[Double]
    val in2 = In[String]
    val out1 = Out[Double]
    val out2 = Out[String]
    //Wiring
    in1.map(_.toString) >> out2
    in1.filter(_ != 0) >> out1
    in2.map(s ⇒ "Received: " + s) >> out2
  }
  //Connecting

  //TODO

}
