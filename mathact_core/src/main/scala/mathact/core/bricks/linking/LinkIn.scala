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

package mathact.core.bricks.linking

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.plumbing.fitting.{Plug, Socket}
import mathact.core.sketch.blocks.BlockLike


/** Chain connecting for blocks with single inflow
  * Created by CAB on 13.11.2016.
  */

trait LinkIn[H]{ _: BlockLike ⇒
  //Inlet producer method
  def in: Socket[H]
  //Connecting methods
  def <~(linkOut: LinkOut[H])(implicit context: BlockContext): Unit = in.plug(linkOut.out)(context)
  def <~(out: Plug[H])(implicit context: BlockContext): Unit = in.plug(out)(context)
  def <~[T](linkThrough: LinkThrough[T,H])(implicit context: BlockContext): LinkThrough[T,H] = {
    in.plug(linkThrough.out)(context)
    linkThrough}}
