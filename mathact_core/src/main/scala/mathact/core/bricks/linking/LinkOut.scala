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


/** Chain connecting for blocks with single outflow
  * Created by CAB on 13.11.2016.
  */

trait LinkOut[T]{ _: BlockLike ⇒
  //Outlet producer method
  def out: Plug[T]
  //Connecting methods
  def ~>(linkIn: LinkIn[T])(implicit context: BlockContext): Unit = out.attach(linkIn.in)(context)
  def ~>(linkIn: InF[T])(implicit context: BlockContext): Unit = out.attach(linkIn.inF)(context)
  def ~>(linkIn: InS[T])(implicit context: BlockContext): Unit = out.attach(linkIn.inS)(context)
  def ~>(in: Socket[T])(implicit context: BlockContext): Unit = out.attach(in)(context)
  def ~>[H](linkThrough: LinkThrough[T,H])(implicit context: BlockContext): LinkThrough[T,H] = {
    out.attach(linkThrough.in)(context)
    linkThrough}
  def ~>[H](through: ThroughF[T,H])(implicit context: BlockContext): ThroughF[T,H] = {
    out.attach(through.inF)(context)
    through}
  def ~>[H](through: ThroughS[T,H])(implicit context: BlockContext): ThroughS[T,H] = {
    out.attach(through.inS)(context)
    through}}
