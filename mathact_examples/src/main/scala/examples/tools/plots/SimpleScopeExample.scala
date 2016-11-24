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

package examples.tools.plots

import mathact.tools.generators.AnalogGenerator
import mathact.tools.plots.SimpleScope
import mathact.tools.pots.AnalogPot
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using of SimpleScope tool
  * Created by CAB on 24.11.2016.
  */

class SimpleScopeExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Simple scope example"
  //Blocks
  val generator = new AnalogGenerator{
    name = "Example generator"
    sampleRate = 100
    period = 100
    f = (t) ⇒ math.sin(t * math.Pi)}
  val pot = new AnalogPot{
    name = "Example pot"
    init = .5}
  val scope =  new SimpleScope{
    name = "Scope"}
  //Connecting
  generator ~> pot ~> scope}