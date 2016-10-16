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

package mathact.core.layout.infrastructure

import akka.actor.ActorRef
import mathact.core.ActorBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.UserLoggingConfigLike


/** Control of tool UI layout
  * Created by CAB on 28.09.2016.
  */

private [mathact] class LayoutController(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {






  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {


    case m ⇒ println("[LayoutController] message: " + m)

  }


}
