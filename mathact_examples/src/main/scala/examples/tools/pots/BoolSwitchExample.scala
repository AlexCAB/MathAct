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

package examples.tools.pots

import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.tools.EmptyBlock
import mathact.tools.pots.BoolSwitch
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using boolean switch
  * Created by CAB on 24.12.2016.
  */

class BoolSwitchExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean switch example"
  //Blocks
  val switch = new BoolSwitch{
    name = "Boolean switch"}
  val logger =  new EmptyBlock with FunWiring with LinkIn[Boolean]{
    name = "Logger"
    val in = In[Boolean]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  switch ~> logger }