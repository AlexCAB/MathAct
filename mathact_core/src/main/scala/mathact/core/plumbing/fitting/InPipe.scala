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

import mathact.core.bricks.plumbing.fitting.{InflowLike, Socket}
import mathact.core.plumbing.Pump


/** Wrapper fot Inlet
  * Created by CAB on 24.08.2016.
  */

private[core] class InPipe[H] (
  private[core] val in: InflowLike[H],
  private[core] val inletName: Option[String],
  private[core] val pump: Pump)
extends Pipe[H] with Socket[H]{
  //Construction
  private[core] val (blockId, inletId) = pump.addInlet(this, inletName)
  //Methods
  override def toString: String = s"InPipe(in: $in, outletName: $inletName, pump: $pump)"
  def processValue(value: Any): Unit = in.processValue(value)}
