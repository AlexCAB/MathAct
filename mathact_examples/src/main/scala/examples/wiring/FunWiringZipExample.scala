/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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


/** Example of using of zipAll and zipEach functions
  * Created by CAB on 30.10.2016.
  */

class FunWiringZipExample extends SimpleWorkbench {
  //Definitions
  class Producer extends EmptyBlock with FunWiring with FunOnStart {
    //Parameters
    imagePath = "examples/wiring/producer.png"
    //Custom flow
    def delay(timeout: Long) = Flow[String, String]{ (v, push) ⇒ Future{
      Thread.sleep(timeout)
      push(v)}}
    //Pipes
    val out = Out[String]}
  class Consumer extends EmptyBlock with FunWiring with FunOnStart {
    //Parameters
    imagePath = "examples/wiring/consumer.png"
    //Pipes
    val in1 = In[String]
    val in2 = In[String]}
  //Blocks
  val fastProducer = new Producer{ name = "Fast producer"
    //Wiring
    start.map(_ ⇒ "Fast Ping") >> delay(1000) >> out}
  val slowProducer = new Producer{ name = "Slow producer"
    //Wiring
    start.map(_ ⇒ "Slow Ping") >> delay(3000) >> out}
  val zipAllConsumer = new Consumer{ name = "Zip all block"
    //Wiring and logging
    in1.zipAll(in2).foreach(v ⇒  logger.info("Consume: " + v))}
  val zipEachConsumer = new Consumer{ name = "Zip each block"
    //Wiring and logging
    (in1 ? "Default 1").zipEach(in2 ? "Default 2").foreach(v ⇒  logger.info("Consume: " + v))}
  //Connecting
  fastProducer.out ~> zipAllConsumer.in1
  fastProducer.out ~> zipEachConsumer.in1
  slowProducer.out ~> zipAllConsumer.in2
  slowProducer.out ~> zipEachConsumer.in2}
