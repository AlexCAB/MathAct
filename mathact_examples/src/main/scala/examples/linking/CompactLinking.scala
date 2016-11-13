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

package examples.linking

import mathact.core.bricks.linking._
import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.bricks.plumbing.wiring.fun.{FunOnStart, FunWiring}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using of compact linking DSL
  * Created by CAB on 13.11.2016.
  */

class CompactLinking extends SimpleWorkbench {
  //Blocks
  val producer = new EmptyBlock with FunWiring with FunOnStart with LinkOut[Int]{ name = "producer"
    //Pipes
    val out = Out[Int]
    //Wiring
    start.map(_ ⇒ 1) >> out
    start.foreach(v ⇒ logger.info("Producing:  1"))}
  val preprocessor = new EmptyBlock with FunWiring with LinkThrough[Int, Int]{ name = "preprocessor"
    //Pipes
    val in = In[Int]
    val out = Out[Int]
    //Wiring
    in.filter(_ < 5) >> out
    in.foreach(v ⇒ logger.info("Preprocessing: " + v))}
  val loop = new EmptyBlock with FunWiring with LinkThrough[Int, Int]{ name = "loop"
    //Pipes
    val in = In[Int]
    val out = Out[Int]
    //Wiring
    in.map(_ + 1) >> out
    in.foreach(v ⇒ logger.info("Looping: " + v))}
  val postprocessor = new EmptyBlock with FunWiring with LinkThrough[Int, String]{ name = "postprocessor"
    //Pipes
    val in = In[Int]
    val out = Out[String]
    //Wiring
    in.map(i ⇒ "Val: " + i) >> out
    in.foreach(v ⇒ logger.info("Postprocessing: " + v))}
  val consumer = new EmptyBlock with FunWiring with LinkIn[String]{ name = "consumer"
    //Pipes
    val in = In[String]
    //Wiring
    in.foreach(v ⇒ logger.info("Consuming: " + v))}
  //Connecting
  producer ~> preprocessor ~> postprocessor ~> consumer
              preprocessor ~> loop
              preprocessor <~ loop
}
