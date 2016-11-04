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
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.enums.WindowsLayoutKind
import mathact.core.model.holders.{DriveRef, SketchControllerRef}
import mathact.core.model.messages.M
import collection.mutable.{Map ⇒ MutMap}


/** Control of block UI layout
  * Created by CAB on 28.09.2016.
  */

private[core] class LayoutActor(
  config: LayoutConfigLike,
  sketchController: SketchControllerRef)
extends WorkerBase with JFXInteraction { import Layout._
  //Variables
  val windows = MutMap[(DriveRef, Int), WindowData]()



  //TODO При размещении для заполения экрана учитывается предпочтительные координаты, при размещении стопкой нет.


//  drive ! M.SetWindowPosition(id, x = 0, y = 0)

  //Functions
  def applyLayout(kind: WindowsLayoutKind): Unit = {
//    //Calc
//    val positions = kind match{
//      case WindowsLayoutKind.FillScreen ⇒ fillAllScreenLayout(windows.values.toSeq)
//      case WindowsLayoutKind.WindowsStairs ⇒ windowsStairsLayout(windows.values.toSeq)}
//    log.debug(s"[LayoutActor.applyLayout] For $kind layout evaluated:")
//    windows.values.zip(positions).foreach{ case (wd, wp) ⇒ log.debug(s"[LayoutActor.applyLayout]       $wd --> $wp")}
//    //Update
//    positions.foreach(p ⇒ p.drive ! M.SetWindowPosition(p.windowId, p.x, p.y))

  }

  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Adding window to list
    case M.RegisterWindow(drive, windowId, state, prefs) ⇒
      windows += ((drive, windowId) → WindowData(drive, windowId, prefs, state))
    //All drives construct, do initial layout
    case M.AllDrivesConstruct ⇒
      applyLayout(config.initialLayoutType)










  }

  //Cleanup
  def cleanup(): Unit = { }

}
