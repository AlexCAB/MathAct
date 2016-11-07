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

import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scala.concurrent.Future


/** Example of tap wiring in object style
  * Created by CAB on 27.10.2016.
  */

class ObjWiringExample extends SimpleWorkbench {
  //Blocks
  val producer = new EmptyBlock with ObjWiring with ObjOnStart{
    //Parameters
    name = "Producer block"
    imagePath = "examples/wiring/producer.png"
    //Handlers
    val outflowXXX = new Outflow[Double]{ def push(v: Double) = pour(v) }
    //On start
    protected def onStart(): Unit = Future{
      for (i ← 1 to 10){
        outflowXXX.push(i)
        Thread.sleep(1000)}} //Emulate heavy processing
    //Pipes
    val out = Outlet(outflowXXX, "out")}
  val processor = new EmptyBlock with ObjWiring{
    //Parameters
    name = "Processor block"
    imagePath = "examples/wiring/processor.png"
    //Handlers
    private val handler = new Outflow[Double] with Inflow[Double]{ def drain(v: Double): Unit = pour(v * 100) }
    //Pipes
    val in = Inlet(handler, "in")
    val out = Outlet(handler, "out")}
  val consumer = new EmptyBlock with ObjWiring{
    //Parameters
    name = "Consumer block"
    imagePath = "examples/wiring/consumer.png"
    //Handlers
    private val inflow = new Inflow[Double]{ def drain(v: Double): Unit = logger.info("Consume value: " + v)}
    //Pipes
    val in = Inlet(inflow, "in")}
  //Connecting
  producer.out ~> processor.in
  processor.out ~> consumer.in}
