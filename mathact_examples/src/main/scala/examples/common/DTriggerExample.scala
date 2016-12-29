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

package examples.common

import mathact.tools.indicators.BoolIndicator
import mathact.tools.math.logic.bool.{And, FlipFlop, Not}
import mathact.tools.pots.{BoolStrobe, BoolSwitch}
import mathact.tools.workbenches.SimpleWorkbench


/** D-rigger example
  * Created by CAB on 26.12.2016.
  */

class DTriggerExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "D-trigger example"
  //Helpers
  val dIn = new BoolSwitch{ name = "D in" }
  val eIn = new BoolStrobe{ name = "E in" }
  val indicator = new BoolIndicator{ name = "Out" }
  //Operators
  val fAnd = new And{ name = "fAnd" }
  val iAnd = new And{ name = "iAnd" }
  val flipFlop = new FlipFlop
  //Connecting
  dIn ~> new Not ~> fAnd ~> flipFlop.r
             eIn ~> fAnd
             eIn ~> iAnd
  dIn            ~> iAnd ~> flipFlop.s
  flipFlop.out ~> indicator.in("Q")
  flipFlop.inv ~> indicator.in("!Q")}