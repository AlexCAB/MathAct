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

import mathact.tools.indicators.ValueIndicator
import mathact.tools.math.continuous.{Adder, Multiplier}
import mathact.tools.pots.SettingDial
import mathact.tools.workbenches.SimpleWorkbench


/** Example of feedback loop.
  * Created by CAB on 28.12.2016.
  */

class FeedbackExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Continuous math example"
  //Helpers
  val input = new SettingDial{ name = "input"; min = -2; max = 2; init = 1}
  val amplifyingRate = new SettingDial{ name = "amplifying"; min = 0; max = 10; init = 2}
  val feedbackRate = new SettingDial{ name = "feedback"; min = -2; max = 2; init = 0}
  val output = new ValueIndicator{ name = "output" }
  //Operators
  val amplifier = new Multiplier
  val feedback = new Multiplier
  val adder = new Adder
  //Connecting
  amplifyingRate ~> amplifier
  feedbackRate ~> feedback
  input ~> adder ~> amplifier ~> output
  adder <~ feedback <~ amplifier}