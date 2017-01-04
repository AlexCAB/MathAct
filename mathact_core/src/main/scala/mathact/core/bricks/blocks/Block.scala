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

package mathact.core.bricks.blocks

import akka.actor.{ActorRef, Props}
import mathact.core.model.enums.BlockType
import mathact.core.plumbing.Pump
import mathact.core.sketch.blocks.BlockLike


/** Base class for sketch block.
  * Created by CAB on 22.10.2016.
  */

abstract class Block (blockContext: BlockContext) extends BlockLike{
  //Context
  if(blockContext.blockType != BlockType.Workbench) throw new IllegalArgumentException(
    "[Block.<init>] Block should not be created inside other Block, create it in Workbench.")
  protected implicit val context = blockContext.copy(blockType = BlockType.Block)
  //Pump
  private[core] val pump: Pump = new Pump(context, this)
  //Helpers
  protected implicit val executionContext = context.system.dispatcher
  protected def actorOf(props: Props): ActorRef = pump.askForNewUserActor(props, None)
  protected def actorOf(props: Props, name: String): ActorRef = pump.askForNewUserActor(props, Some(name))
  //User logger
  protected object logger {
    def info(message: String): Unit = pump.userLogInfo(message)
    def warn(message: String): Unit = pump.userLogWarn(message)
    def error(message: String): Unit = pump.userLogError(None, message)
    def error(error: Throwable): Unit = pump.userLogError(Some(error), "")
    def error(error: Throwable, message: String): Unit = pump.userLogError(Some(error), message)}}
