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

package examples.tools.indicators

import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.data.basic.SingleValue
import mathact.tools.EmptyBlock
import mathact.tools.indicators.BoolIndicator
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using boolean indicator tool.
  * Created by CAB on 24.12.2016.
  */

class BoolIndicatorExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean indicator example"
  //Blocks
  BoolSwitch


  val indicator = new BoolIndicator{
    name = "Boolean indicator"}
  val logger =  new EmptyBlock with FunWiring with LinkIn[SingleValue]{
    name = "Logger"
    val in = In[Boolean]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  indicator ~> logger }