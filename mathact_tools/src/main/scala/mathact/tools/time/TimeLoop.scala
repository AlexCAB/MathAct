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

package mathact.tools.time

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.Timed
import mathact.data.ui.C
import mathact.tools.Tool


/** Timed messages loop controller
  * Created by CAB on 03.12.2016.
  */

abstract class TimeLoop[V <: Timed[V]](implicit blockContext: BlockContext)
extends Tool(blockContext, "TL", "mathact/tools/time/time_loop.png")                      //<--- TODO icon
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkThrough[V, V]{


  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }

  //Handler
  private val handler = new Inflow[V] with Outflow[V]{


    protected def drain(value: V): Unit = {}

  }


  //Connection points
  val in = Inlet[V](handler)
  val out = Outlet[V](handler)

}
