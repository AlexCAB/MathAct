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

package examples.tools.math

import mathact.tools.indicators.ValueIndicator
import mathact.tools.math.continuous._
import mathact.tools.pots.SettingDial
import mathact.tools.workbenches.SimpleWorkbench


/** Example of continuous math operators
  * Created by CAB on 28.12.2016.
  */

class ContinuousMathExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Continuous math example"
  //Helpers
  val dialX = new SettingDial{ name = "Dial X" }
  val dialY = new SettingDial{ name = "Dial Y" }
  val indicator = new ValueIndicator{ name = "Value indicator" }
  //Operators
  val adder = new Adder
  val multiplier = new Multiplier
  val signInverter = new SignInverter
  //Connecting
  dialX ~> adder
  dialY ~> adder ~> indicator.in(name = "adder")
  dialX ~> multiplier
  dialY ~> multiplier ~> indicator.in(name = "multiplier")
  dialX ~> signInverter ~> indicator.in(name = "signInverter")}
