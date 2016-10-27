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
    val out = Outlet("out")
    //On start
    start.map(_ ⇒ (1 to 10).toStream).map{s ⇒ Thread.sleep(1000); s}.to(out)}
  val processor = new EmptyBlock with FunWiring{
    //Parameters
    name = "Processor block"
    imagePath = "examples/connecting/processor.png"
    //Pipes
    val in = Inlet("in")
    val out = Outlet("out")
    //Mapping
    in.map(v ⇒ v * 100).to(out)}
  val consumer = new EmptyBlock with FunWiring{
    //Parameters
    name = "Consumer block"
    imagePath = "examples/connecting/consumer.png"
    //Pipes
    val in = Inlet("in")
    //Logging
    in.foreach(v ⇒  logger.info("Consume value: " + v))}
  //Connecting
  producer.out.attach(processor.in)
  processor.out.attach(consumer.in)}
