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

import mathact.core.bricks.plumbing.fitting.Plug
import mathact.core.bricks.plumbing.wiring.OutflowLike
import mathact.core.model.messages.M
import mathact.core.plumbing.Pump


/** Wrapper for Outlet
  * Created by CAB on 24.08.2016.
  */

private[core] class OutPipe[H](
  private[core] val out: OutflowLike[H],
  private[core] val outletName: Option[String],
  private[core] val pump: Pump)
extends Pipe[H] with Plug[H]{
  //Construction
  private[core] val (blockId, outletId) = pump.addOutlet(this, outletName)
  out.injectOutPipe(this)
  //Methods
  private[core] def pushUserData(value: H): Unit = pump.pushUserMessage(M.UserData[H](outletId, value))
  override def toString: String = s"OutPipe(in: $out, outletName: $outletName, pump: $pump)"}


