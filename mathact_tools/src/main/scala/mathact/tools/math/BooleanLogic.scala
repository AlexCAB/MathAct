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
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.tools.Tool


/** Base class for boolean logic operators
  * Created by CAB on 26.12.2016.
  */

abstract class BooleanLogic(context: BlockContext, name: String, imgPath: String)
extends Tool(context, name, imgPath) with ObjWiring with ObjOnStart
with LinkThrough[Boolean, Boolean]{
  //Parameters
  val randomDelayRange = 10
  //Evaluation function
  protected def eval(input: Seq[Boolean]): Boolean
  //Variables
  @volatile private var _default = false
  @volatile private var values = Array[Boolean]()
  //Outflow
  private val outflow = new Outflow[Boolean] { def send(v: Boolean): Unit = pour(v) }
  //Functions
  private def buildInflow(): Inflow[Boolean] = new Inflow[Boolean]{
    //Construction
    val i = values.length
    values = Array.fill(i + 1)(_default)
    //Methods
    protected def drain(v: Boolean) = {
      if(values(i) != v){
        values(i) = v
        outflow.send(eval(values))}}}
  //DSL
  def default: Boolean = _default
  def default_=(v: Boolean){ _default = v }
  //On start
  protected def onStart(): Unit = outflow.send(eval(values))
  //Connection point
  def in = Inlet(buildInflow())
  val out = Outlet(outflow)}
