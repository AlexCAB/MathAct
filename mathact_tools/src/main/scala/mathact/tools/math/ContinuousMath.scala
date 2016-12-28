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

package mathact.tools.math

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.fitting.{Plug, Socket}
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.core.model.enums.DequeueAlgo
import mathact.data.basic.SingleValue
import mathact.tools.Tool


/** Continuous math
  * Created by CAB on 28.12.2016.
  */

abstract class ContinuousMath(context: BlockContext, name: String, imgPath: String)
extends Tool(context, name, imgPath) with ObjWiring with ObjOnStart with LinkThrough[SingleValue, SingleValue]{
  //Parameters
  val defaultEpsilon: Double = .00001
  //Abstract evaluation function
  protected def eval(input: Seq[Double]): Double
  //Properties
  private var _epsilon = defaultEpsilon
  //Processor
  private val processor = new Outflow[SingleValue]{
    //Variables
    private var values = Array[Double]()
    private var lastResult = 0.0
    //Methods
    def addInflow(): Int = {
      val i = values.length
      values = Array.fill(i + 1)(0.0)
      i}
    def start(): Unit = {
      val res = eval(values)
      lastResult = res
      pour(SingleValue(res))}
    def process(i: Int, v: SingleValue): Unit = {
      values(i) = v.value
      val res = eval(values)
      if((lastResult - res).abs > _epsilon){
        lastResult = res
        pour(SingleValue(res))}}}
  //On start
  protected def onStart(): Unit = processor.start()
  //DSL
  def epsilon: Double = _epsilon
  def epsilon_=(v: Double){ _epsilon = v }
  //Connection points
  def in: Socket[SingleValue] = Inlet(
    new Inflow[SingleValue]{
      val i = processor.addInflow()
      protected def drain(v: SingleValue): Unit = processor.process(i, v)},
    DequeueAlgo.Last)
  val out: Plug[SingleValue] = Outlet(processor)}
