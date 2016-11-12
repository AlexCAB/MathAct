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

import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.tools.EmptyBlock
import mathact.tools.generators.DiscreteGenerator
import mathact.tools.pots.TimedValuesPot
import mathact.tools.pots.TimedValuesPot.TimedValue
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using of TimedValuesPot
  * Created by CAB on 12.11.2016.
  */

class TimedValuesPotExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Timed values pot example"
  //Blocks
  val generator = new DiscreteGenerator{
    name = "Example generator"
    initFrequency = 2}
  val pot = new TimedValuesPot{
      name = "Example pot"
      init = 0
      min = -1
      max = 1}
  val logger =  new EmptyBlock with FunWiring{  name = "Logger"
      val in = In[TimedValue]
      in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  generator.out ~> pot.in
  pot.out ~> logger.in}