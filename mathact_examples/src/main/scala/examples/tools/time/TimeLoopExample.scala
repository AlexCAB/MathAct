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

package examples.tools.time

import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.data.discrete.TimedValue
import mathact.tools.EmptyBlock
import mathact.tools.time.TimeLoop
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using TimeLoop
  * Created by CAB on 03.12.2016.
  */

class TimeLoopExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Time loop example"
  //Blocks
  val loop = new TimeLoop[TimedValue]{ name = "Time loop" }
  val logger =  new EmptyBlock with FunWiring with LinkThrough[TimedValue, TimedValue]{ name = "Logger"
    val in = In[TimedValue]
    val out = Out[TimedValue]
    in.foreach(v ⇒ logger.info("Logger received: " + v))      //Log next received value
    in.map(_ + 1).next(out)                                    //Increment value and send back
    in.map(_ + 1.5).filter(_ ⇒ math.random > 0.5) .next(out)} //Simulate accidental duplication of message
  //Connecting
  loop ~> logger ~> loop }
