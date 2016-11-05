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

import javafx.geometry.Rectangle2D

import mathact.core.model.config.LayoutConfigLike
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.holders.DriveRef


/** Layout
  * Created by CAB on 04.11.2016.
  */

object Layout {
  //Definitions
  type WinID = Tuple2[DriveRef, Int]
  case class WindowData(
    drive: DriveRef,
    windowId: Int,
    preference: WindowPreference)
  case class WindowPosition(
    drive: DriveRef,
    windowId: Int,
    x: Double,
    y: Double)
  case class ScreenData(
    topX: Double,
    topY: Double,
    bottomX: Double,
    bottomY: Double,
    currentX: Double,
    currentY: Double)
  trait LayoutCalcLike{
    val config: LayoutConfigLike
    val screenBounds: Rectangle2D
    val windows: Seq[WindowData]
    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition]
    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition}
  //Algorithms
  class FillAllScreenLayout(
    val config: LayoutConfigLike,
    val screenBounds: Rectangle2D,
    val windows: Seq[WindowData])
  extends LayoutCalcLike {

    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition] = {

      windows.toList.map{ window ⇒
        WindowPosition(window.drive, window.windowId, 0, 0)


      }


      //TODO Добавить при разварачивании запрос на размещение



    }




    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition  = ???

  }
  class WindowsStairsLayout(
    val config: LayoutConfigLike,
    val screenBounds: Rectangle2D,
    val windows: Seq[WindowData])
  extends LayoutCalcLike {
    //Variables
    private var currentScreenData = ScreenData(100, 100, 200, 200, 100, 100)
    //Functions
    private def evalNextPosition(screen: ScreenData): ScreenData = {
      screen.currentX < screen.bottomX && screen.currentY < screen.bottomY match {
        case true ⇒
          screen.copy(currentX = screen.currentX + config.stairsStep, currentY = screen.currentX + config.stairsStep)
        case false ⇒
          screen.copy(currentX = screen.topX, currentY = screen.topY)}}
    //Methods
    def evalAll(states: Map[WinID, WindowState]): List[WindowPosition] = {
      //Preparing
      val screen = ScreenData(
        config.screenIndent,
        config.screenIndent,
        screenBounds.getWidth - config.screenIndent * 2,
        screenBounds.getHeight - config.screenIndent * 2,
        config.screenIndent,
        config.screenIndent)
      val sortedWindows = states
        .filter(_._2.isShown)
        .toSeq.sortBy(_._2.title)
        .flatMap{ case ((d,i),_) ⇒ windows.find(w ⇒ w.drive == d && w.windowId == i) }
        .reverse
      //Calc
      val (newScreen, allPositions) = sortedWindows
        .foldRight((screen, List[WindowPosition]())){ case (window, (sc, positions)) ⇒ (
          evalNextPosition(sc),
          positions :+ WindowPosition(window.drive, window.windowId, sc.currentX, sc.currentY))}
      currentScreenData = newScreen
      allPositions}
    def evalGiven(states: Map[WinID, WindowState], drive: DriveRef, windowId: Int): WindowPosition = {
      currentScreenData = evalNextPosition(currentScreenData)
      WindowPosition(drive, windowId, currentScreenData.currentX, currentScreenData.currentY)}}}
