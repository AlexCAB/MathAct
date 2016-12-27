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
import mathact.tools.pots.BoolStrobe
import mathact.tools.workbenches.SimpleWorkbench


/** Bool strobe example
  * Created by CAB on 26.12.2016.
  */

class BoolStrobeExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean strobe example"
  //Blocks
  val switch = new BoolStrobe{
    name = "Boolean strobe"
    default = false}
  val logger =  new EmptyBlock with FunWiring with LinkIn[Boolean]{
    name = "Logger"
    val in = In[Boolean]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  switch ~> logger }