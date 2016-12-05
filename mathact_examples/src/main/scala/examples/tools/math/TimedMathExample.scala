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

package examples.tools.math

import mathact.tools.generators.DiscreteGenerator
import mathact.tools.math.timed.Adder
import mathact.tools.plots.ChartRecorder
import mathact.tools.pots.{SettingDial, TimedValuesPot}
import mathact.tools.workbenches.SimpleWorkbench


/** Example timed math operators
  * Created by CAB on 05.12.2016.
  */

class TimedMathExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Timed math example"
  //Helpers
  val generator = new DiscreteGenerator{ name = "Time generator" }
  val potX = new TimedValuesPot{ name = "Timed pot X" }
  val potY = new TimedValuesPot{ name = "Timed pot Y" }
  val dialX = new SettingDial{ name = "Dial X" }
  val dialY = new SettingDial{ name = "Dial X" }
  val chart = new ChartRecorder{ name = "chart" }
  //Operators
  val adder = new Adder
  //Connecting
  generator ~> potX
  generator ~> potY
  potX  ~> adder
  potY  ~> adder
  dialX ~> adder
  dialY ~> adder ~> chart.line(name = "adder")





}
