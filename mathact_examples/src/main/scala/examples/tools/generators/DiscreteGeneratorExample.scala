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

package examples.tools.generators

import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.tools.EmptyBlock
import mathact.tools.generators.DiscreteGenerator
import mathact.tools.generators.DiscreteGenerator.TimedEvent
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using of discrete generator
  * Created by CAB on 10.11.2016.
  */

class DiscreteGeneratorExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Discrete generator example"
  //Blocks
  val generator = new DiscreteGenerator{
    name = "Example generator"



  }
  val logger =  new EmptyBlock with FunWiring{  name = "Logger"
    val in = In[TimedEvent]
    in.foreach(v ⇒ logger.info("Logger receive: " + v))}
  //Connecting
  generator.out ~> logger.in}
