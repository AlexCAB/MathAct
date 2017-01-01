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

package manual.sketches

import mathact.core.bricks.linking.{LinkIn, LinkOut, LinkThrough}
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scala.concurrent.Future


/** My third sketch
  * Created by CAB on 01.01.2017.
  */

class MyThirdSketch extends SimpleWorkbench {
  //Blocks
  object BlockA extends EmptyBlock with ObjWiring with ObjOnStart with LinkOut[Double]{
    //Parameters
    name = "BlockA"
    //Wiring
    private val gen = new Outflow[Double] {
      def start(): Unit = Future{
        (0 to 10).foreach{ i ⇒
          pour(i)
          Thread.sleep(500)}}}
    protected def onStart(): Unit = gen.start()
    //Connection points
    val out = Outlet(gen) }
  object BlockB extends EmptyBlock with FunWiring with LinkThrough[Double, String]{
    //Parameters
    name = "BlockB"
    //Connection points
    val in = In[Double]
    val out = Out[String]
    val out2 = Out[Double]
    //Wiring
    in.map(s ⇒ "Received: " + s) >> out
    in.filter(_ % 2 == 0) >> out2 }
  object BlockC extends EmptyBlock with FunWiring with LinkIn[String]{
    //Parameters
    name = "BlockC"
    //Connection points
    val in = In[String]
    val in2 = In[Double]
    //Wiring
    in.foreach(v ⇒ logger.info("IN: " + v))
    in2.foreach(v ⇒ logger.info("IN2: " + v)) }
  object BlockD extends EmptyBlock with FunWiring with LinkIn[String]{
    //Parameters
    name = "BlockD"
    //Connection points
    val in = In[String]
    //Wiring
    in.foreach(v ⇒ logger.info("IN: " + v))}
  //Connecting
  BlockA ~> BlockB      ~> BlockC          //<-- Shortcut DSL
            BlockB.out2 ~> BlockC.in2      //<-- Standard way of connecting
            BlockB      ~> BlockD
}
