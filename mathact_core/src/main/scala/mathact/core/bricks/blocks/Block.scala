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

package mathact.core.bricks.blocks

import mathact.core.plumbing.Pump
import mathact.core.sketch.blocks.BlockLike


/** Base class for sketch block.
  * Created by CAB on 22.10.2016.
  */

abstract class Block (context: SketchContext) extends BlockLike{
  //Pump
  private[core] val pump: Pump = new Pump(context, this)
  //Helpers
  implicit val executionContext = context.system.dispatcher
  //User logger
  object logger {
    def info(message: String): Unit = pump.userLogInfo(message)
    def warn(message: String): Unit = pump.userLogWarn(message)
    def error(message: String): Unit = pump.userLogError(None, message)
    def error(error: Throwable): Unit = pump.userLogError(Some(error), "")
    def error(error: Throwable, message: String): Unit = pump.userLogError(Some(error), message)}
  //Methods

  //TODO

}
