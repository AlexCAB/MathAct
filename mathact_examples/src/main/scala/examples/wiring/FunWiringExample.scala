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

import scala.concurrent.Future


/** Example of tap wiring in functional style
  * Created by CAB on 27.10.2016.
  */

class FunWiringExample extends SimpleWorkbench {
  //Blocks
  val producer = new EmptyBlock with FunWiring with FunOnStart{
    //Parameters
    name = "Producer block"
    imagePath = "examples/wiring/producer.png"
    //Custom flow
    val gen = Flow[Unit, Double]{ (_, push) ⇒ Future{
      (1 to 10).foreach{i ⇒
        Thread.sleep(1000) //Thread.sleep(1000) emulate heavy processing
        push(i)}}}
    //Pipes
    val out = Out[Double]("out")
    //Wiring
    start >> gen >> out}
  val processor = new EmptyBlock with FunWiring{
    //Parameters
    name = "Processor block"
    imagePath = "examples/wiring/processor.png"
    //Pipes
    val in = In[Double]("in")
    val out = Out[Double]("out")
    //Mapping
    in.map(v ⇒ v * 100).next(out)}
  val consumer = new EmptyBlock with FunWiring{
    //Parameters
    name = "Consumer block"
    imagePath = "examples/wiring/consumer.png"
    //Pipes
    val in = In[Double]
    //Logging
    in.foreach(v ⇒ logger.info("Consume value: " + v))}
  //Connecting
  producer.out ~> processor.in
  processor.out ~> consumer.in}
