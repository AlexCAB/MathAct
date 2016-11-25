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
  val sinGen = new AnalogGenerator{
    name = "Example generator sin"
    sampleRate = 100
    period = 100
    f = (t) ⇒ math.sin(t * math.Pi)}
  val cosGen = new AnalogGenerator{
    name = "Example generator cos"
    sampleRate = 100
    period = 100
    f = (t) ⇒ math.cos(t * math.Pi)}
  val sinPot = new AnalogPot{
    name = "Example sin pot"
    init = .5}
  val cosPot = new AnalogPot{
    name = "Example cos pot"
    init = .5}
  val scope =  new SimpleScope{
    name = "Scope"}
  //Connecting
  sinGen ~> sinPot ~> scope
  cosGen ~> cosPot ~> scope}