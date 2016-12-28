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

import mathact.tools.indicators.ValueIndicator
import mathact.tools.pots.SettingDial
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using value indicator tool.
  * Created by CAB on 28.12.2016.
  */

class ValueIndicatorExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean indicator example"
  //Blocks
  val dial1 = new SettingDial{ name = "Dial 1" }
  val dial2 = new SettingDial{ name = "Dial 2" }
  val indicator = new ValueIndicator{ name = "Value indicator" }
  //Connecting
  dial1 ~> indicator
  dial2 ~> indicator.in("dial2")}