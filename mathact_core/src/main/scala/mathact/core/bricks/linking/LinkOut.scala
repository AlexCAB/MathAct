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

import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.sketch.blocks.BlockLike


/** Chain connecting for blocks with single outflow
  * Created by CAB on 13.11.2016.
  */

trait LinkOut[T]{ _: BlockLike ⇒
  //Outlet producer method
  def out: Plug[T]
  //Connecting methods
  def ~>(linkIn: LinkIn[T]): Unit = out.attach(linkIn.in)
  def ~>(in: Socket[T]): Unit = out.attach(in)
  def ~>[H](linkThrough: LinkThrough[T,H]): LinkThrough[T,H] = {
    out.attach(linkThrough.in)
    linkThrough}}
