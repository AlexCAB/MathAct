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

package mathact.core.bricks.linking

import mathact.core.bricks.plumbing.fitting.{Plug, Socket}
import mathact.core.sketch.blocks.BlockLike


/** Chain connecting for blocks with single inflow
  * Created by CAB on 13.11.2016.
  */

trait LinkIn[H]{ _: BlockLike ⇒
  //Inlet producer method
  def in: Socket[H]
  //Connecting methods
  def <~(linkOut: LinkOut[H]): Unit = in.plug(linkOut.out)
  def <~(out: Plug[H]): Unit = in.plug(out)
  def <~[T](linkThrough: LinkThrough[T,H]): LinkThrough[T,H] = {
    in.plug(linkThrough.out)
    linkThrough}}
