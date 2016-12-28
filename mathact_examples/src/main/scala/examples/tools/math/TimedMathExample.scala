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
import mathact.tools.math.timed._
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
  val generator = new DiscreteGenerator{ name = "Time generator"; initFrequency = 10}
  val potX = new TimedValuesPot{ name = "Timed pot X" }
  val potY = new TimedValuesPot{ name = "Timed pot Y" }
  val dialX = new SettingDial{ name = "Dial X" }
  val dialY = new SettingDial{ name = "Dial Y" }
  val chart = new ChartRecorder{ name = "chart" }
  //Operators
  val adder = new Adder
  val multiplier = new Multiplier
  val signInverter = new SignInverter
  val integrator = new Integrator
  val differentiator = new Differentiator
  //Connecting
  generator ~> potX
  generator ~> potY
  //Adder
  potX  ~> adder
  potY  ~> adder
  dialX ~> adder
  dialY ~> adder ~> chart.line(name = "adder")
  //Multiplier
  potX  ~> multiplier
  potY  ~> multiplier
  dialX ~> multiplier
  dialY ~> multiplier ~> chart.line(name = "multiplier")
  //SignInverter
  potX  ~> signInverter ~> chart.line(name = "sign inverter")
  //Integrator
  potX  ~> integrator
  potY  ~> integrator
  dialX ~> integrator
  dialY ~> integrator ~> chart.line(name = "integrator")
  //Differentiator
  potX  ~> differentiator
  potY  ~> differentiator
  dialX ~> differentiator
  dialY ~> differentiator ~> chart.line(name = "differentiator")}
