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
import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.UserLoggingConfigLike


/** Control of block UI layout
  * Created by CAB on 28.09.2016.
  */

private [mathact] class LayoutController(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends WorkerBase with JFXInteraction {






  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {


    case m ⇒ println("[LayoutController] message: " + m)

  }


}
