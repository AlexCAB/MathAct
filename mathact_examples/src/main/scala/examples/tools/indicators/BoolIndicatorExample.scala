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

package examples.tools.indicators

import mathact.tools.indicators.BoolIndicator
import mathact.tools.pots.BoolSwitch
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using boolean indicator tool.
  * Created by CAB on 24.12.2016.
  */

class BoolIndicatorExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean indicator example"
  //Blocks
  val switch1 = new BoolSwitch{ name = "switch1" }
  val switch2 = new BoolSwitch{ name = "switch2" }
  val indicator = new BoolIndicator{ name = "Boolean indicator" }
  //Connecting
  switch1 ~> indicator
  switch2 ~> indicator.in("switch2")}