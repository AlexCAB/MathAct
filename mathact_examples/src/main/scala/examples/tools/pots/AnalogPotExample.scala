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

package examples.tools.pots

import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.data.analog.Sample
import mathact.tools.EmptyBlock
import mathact.tools.generators.AnalogGenerator
import mathact.tools.pots.AnalogPot
import mathact.tools.workbenches.SimpleWorkbench

/** Example of using of AnalogPot
  * Created by CAB on 24.11.2016.
  */

class AnalogPotExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Analog pot example"
  //Blocks
  val generator = new AnalogGenerator{
    //Params
    name = "Example generator"
    sampleRate = 10 //Hertz
    period = 1000
    //Gen function
    f = (t) ⇒ t}
  val pot = new AnalogPot{
    //Params
    name = "Example pot"
    init = .5}
  val logger =  new EmptyBlock with FunWiring with LinkIn[Sample]{
    name = "Logger"
    val in = In[Sample]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  generator ~> pot ~> logger}