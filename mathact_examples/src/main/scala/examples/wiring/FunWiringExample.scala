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

package examples.wiring

import mathact.core.bricks.plumbing.wiring.fun.{FunOnStart, FunWiring}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench


/** Example of tap wiring in functional style
  * Created by CAB on 27.10.2016.
  */

class FunWiringExample extends SimpleWorkbench {
  //Blocks
  val producer = new EmptyBlock with FunWiring with FunOnStart{
    //Parameters
    name = "Producer block"
    imagePath = "examples/connecting/producer.png"
    //Pipes
    val out = Outlet[Double]("out")
    //On start
    start.unfold(_ ⇒ 1.0 to 10.0 by 1).map{s ⇒ Thread.sleep(1000); s}.to(out)}  //Thread.sleep(1000) emulate heavy processing
  val processor = new EmptyBlock with FunWiring{
    //Parameters
    name = "Processor block"
    imagePath = "examples/connecting/processor.png"
    //Pipes
    val in = Inlet[Double]("in")
    val out = Outlet[Double]("out")
    //Mapping
    in.map(v ⇒ v * 100).to(out)}
  val consumer = new EmptyBlock with FunWiring{
    //Parameters
    name = "Consumer block"
    imagePath = "examples/connecting/consumer.png"
    //Pipes
    val in = Inlet[Double]
    //Logging
    in.foreach(v ⇒  logger.info("Consume value: " + v))}
  //Connecting
  producer.out ~> processor.in
  processor.out ~> consumer.in}
