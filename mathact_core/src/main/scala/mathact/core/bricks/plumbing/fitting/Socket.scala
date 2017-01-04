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

package mathact.core.bricks.plumbing.fitting

import mathact.core.bricks.blocks.BlockContext
import mathact.core.plumbing.fitting.Flange
import mathact.core.plumbing.fitting.pipes.InPipe


/** Event receiver must be implemented by Inlet
  * Created by CAB on 17.05.2016.
  */

trait Socket[H] extends Flange[H] { _: InPipe[H] ⇒
  //Methods
  /** Connecting of this Socket to given Plug
    * @param plug - Plug[T] */
  def plug(plug: ⇒Plug[H])(implicit context: BlockContext): Unit = pump.connect(context, plug, this)
  def <~ (plug: ⇒Plug[H])(implicit context: BlockContext): Unit = pump.connect(context, plug, this)}
