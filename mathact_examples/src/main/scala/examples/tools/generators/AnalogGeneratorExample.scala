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

import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.data.analog.Sample
import mathact.tools.EmptyBlock
import mathact.tools.generators.AnalogGenerator
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using AnalogGenerator tool
  * Created by CAB on 19.11.2016.
  */

class AnalogGeneratorExample extends SimpleWorkbench {
  //Sketch parameters
  heading = " Analog generator example"
  //Blocks
  val generator = new AnalogGenerator{
    //Params
    name = "Analog generator"
    sampleRate = 100 //Hertz
    period = 500     //Milliseconds
    //Gen function
    f = (t) ⇒ math.sin(t * math.Pi)}
  val logger =  new EmptyBlock with FunWiring with LinkIn[Sample]{
    name = "Logger"
    val in = In[Sample]
    in.foreach(v ⇒ logger.info("Logger received: " + v))}
  //Connecting
  generator ~> logger}