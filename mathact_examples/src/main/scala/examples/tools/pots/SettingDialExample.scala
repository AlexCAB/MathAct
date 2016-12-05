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
import mathact.data.basic.SingleValue
import mathact.tools.EmptyBlock
import mathact.tools.pots.SettingDial
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using SettingDial tool.
  * Created by CAB on 05.12.2016.
  */

class SettingDialExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Setting dial example"
  //Blocks
  val dial = new SettingDial{
    name = "Setting dial"
    min = -1
    max = 1
    init = 0 }
  val logger =  new EmptyBlock with FunWiring with LinkIn[SingleValue]{
    name = "Logger"
    val in = In[SingleValue]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  dial ~> logger }