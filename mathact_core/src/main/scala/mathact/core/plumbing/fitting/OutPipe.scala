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

package mathact.core.plumbing.fitting

import mathact.core.bricks.plumbing.fitting.{OutletLike, Plug}
import mathact.core.model.data.pipes.OutletData
import mathact.core.plumbing.Pump


/** Wrapper for Outlet
  * Created by CAB on 24.08.2016.
  */

private[core] class OutPipe[H](
  out: OutletLike[H],
  protected val pipeName: Option[String],
  protected val pump: Pump)
extends Pipe[H] with Plug[H]{
  //Construction
  protected val (blockId, pipeId) = pump.addOutlet(this, pipeName)
  out.injectOutPipe(this)
  //Fields
  lazy val pipeData = OutletData(blockId, pump.drive, pump.blockName, pipeId, pipeName)
  //Methods
  override def toString: String = s"OutPipe(in: $out, pipeName: $pipeName, pump: $pump)"





}
