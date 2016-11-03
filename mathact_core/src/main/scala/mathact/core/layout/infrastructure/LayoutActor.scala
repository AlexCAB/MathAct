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

import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.LayoutConfigLike
import mathact.core.model.holders.SketchControllerRef
import mathact.core.model.messages.M


/** Control of block UI layout
  * Created by CAB on 28.09.2016.
  */

private[core] class LayoutActor(
  config: LayoutConfigLike,
  sketchController: SketchControllerRef)
extends WorkerBase with JFXInteraction {






  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {


    //TODO При размещении для заполения экрана учитывается предпочтительные координаты, при размещении стопкой нет.




    case M.RegisterWindow(drive, id, state, prefs) ⇒

      drive ! M.UpdateWindowPosition(id, x = 0, y = 0)


    case M.AllDrivesConstruct ⇒

      println("[LayoutActor @ AllDrivesConstruct]")

    case m ⇒ println("[LayoutActor] message: " + m)

  }

  //Cleanup
  def cleanup(): Unit = {  }

}
