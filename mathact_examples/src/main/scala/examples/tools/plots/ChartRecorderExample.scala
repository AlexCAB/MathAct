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

package examples.tools.plots

import mathact.tools.generators.DiscreteGenerator
import mathact.tools.plots.ChartRecorder
import mathact.tools.pots.TimedValuesPot
import mathact.tools.workbenches.SimpleWorkbench
import scalafx.scene.paint.Color._


/** Example of using of simple chart recorder
  * Created by CAB on 13.11.2016.
  */

class ChartRecorderExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Chart simple recorder example"
  //Blocks
  val generator = new DiscreteGenerator{ name = "generator"}
  val pot1 = new TimedValuesPot{ name = "pot1" }
  val pot2 = new TimedValuesPot{ name = "pot2" }
  val pot3 = new TimedValuesPot{ name = "pot3" }
  val chart = new ChartRecorder{ name = "chart" }
  //Connecting
  generator ~> pot1 ~> chart
  generator ~> pot2 ~> chart
  generator ~> pot3 ~> chart.line(name = "Pot 3 line", color = Red)}
