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

package mathact.tools

import mathact.core.bricks.blocks.{Block, BlockContext}


/** Base class for tall tools.
  * Created by CAB on 07.05.2016.
  */

private[mathact] abstract class Tool(
  blockContext: BlockContext,
  toolTypeName: String,
  toolImgPath: String)
extends Block(blockContext){
  //Variables
  private var _name: Option[String] = None
  //DSL
  def name_=(v: String) { _name = v match{case "" ⇒ None; case s ⇒ Some(s)} }
  def name = _name
  //Abstract callbacks (will called by system after sketch will constructed)
  private[mathact] def blockName: Option[String] =
    Some(toolTypeName + (_name match{case Some(n) ⇒ " - " + n case _ ⇒ ""}))
  private[mathact] def blockImagePath: Option[String] = Some(toolImgPath)

  //TODO Add more

}
