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

package mathact.tools.math.logic.bool

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.tools.Tool


/** Flip flop
  * Created by CAB on 27.12.2016.
  */

class FlipFlop(implicit context: BlockContext)
extends Tool(context, "FlipFlop", "mathact/tools/math/logic/bool/flip_flop.png")
with ObjWiring with ObjOnStart{
  //Variables
  @volatile private var state = false
  //Outflow
  private val outflow = new Outflow[Boolean] { def send(v: Boolean): Unit = pour(v) }
  private val invflow = new Outflow[Boolean] { def send(v: Boolean): Unit = pour(v) }
  //Connection points

  //DSL
  def default: Boolean = state
  def default_=(v: Boolean){ state = v }
  //On start
  protected def onStart(): Unit = {
    outflow.send(state)
    invflow.send(! state)}
  //Connection point
  val s = Inlet(
    new Inflow[Boolean]{
      protected def drain(v: Boolean) = if(v){
        state = true
        outflow.send(state)
        invflow.send(! state)}},
    "S")
  val r = Inlet(
    new Inflow[Boolean]{
      protected def drain(v: Boolean) = if(v){
        state = false
        outflow.send(state)
        invflow.send(! state)}},
    "R")
  val out = Outlet(outflow, "Q")
  val inv = Outlet(invflow, "!Q")}
