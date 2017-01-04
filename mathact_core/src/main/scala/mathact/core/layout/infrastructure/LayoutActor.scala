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

package mathact.core.layout.infrastructure

import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.LayoutConfigLike
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.enums.WindowsLayoutKind
import mathact.core.model.holders.{DriveRef, SketchControllerRef}
import mathact.core.model.messages.M

import javafx.geometry.Rectangle2D


/** Control of block UI layout
  * Created by CAB on 28.09.2016.
  */

private[core] class LayoutActor(
  config: LayoutConfigLike,
  sketchController: SketchControllerRef,
  screenBounds: Rectangle2D)
extends WorkerBase with JFXInteraction { import Layout._
  //Variables
  var windows = List[WindowData]()
  var states =  Map[(DriveRef, Int), WindowState]()
  var toUpdateQueue = List[WindowPosition]()
  var calculator: Option[LayoutCalcLike] = None
  //Functions
  /** Adding new window to list */
  def addWindow(drive: DriveRef, windowId: Int, state: WindowState, prefs: WindowPreference): Unit = {
    //Preparing
    val id = Tuple2(drive, windowId)
    //Adding
    windows :+= WindowData(drive, windowId, prefs)
    states += (id → state)}
  /** Send next WindowPosition form toUpdateQueue */
  def sendNextWindowUpdate(): Unit =
    toUpdateQueue.headOption.foreach(d ⇒ d.drive !  M.SetWindowPosition(d.windowId, d.pos.x, d.pos.y))
  /** Check if window in list and run proc */
  def runIfWindowExist(drive: DriveRef, windowId: Int)(proc: (WinID, WindowState) ⇒ Unit): Unit = {
    val id = Tuple2(drive, windowId)
    states.get(id) match{
      case Some(state) ⇒
        proc(id, state)
      case None ⇒
        log.error(s"[LayoutActor.runIfWindowExist] Window not found, drive: $drive, windowId: $windowId")}}
  /** Check if window exist in list and if so update state and send update to next window*/
  def windowUpdated(drive: DriveRef, windowId: Int, newState: WindowState): Unit =
    runIfWindowExist(drive,windowId){ (id, state) ⇒
      log.debug(s"[LayoutActor.windowUpdated] id: $id, current state: $state, new state: $newState")
      states += (id → newState)}
  /** Window position updated, remove from queue */
  def windowPositionUpdated(drive: DriveRef, windowId: Int): Unit = runIfWindowExist(drive,windowId){ (id, state) ⇒
    log.debug(s"[LayoutActor.windowPositionUpdated] id: $id, current state: $state, toUpdateQueue: $toUpdateQueue")
    toUpdateQueue = toUpdateQueue.filterNot(d ⇒ d.drive == drive && d.windowId == windowId)
    sendNextWindowUpdate()}
  /**  Construction of layout of given kind, calc new window positions and start of windows updating */
  def constructCalcAndUpdatePositions(kind: WindowsLayoutKind): Unit = {
    //Construct calc
    val calc = kind match{
      case WindowsLayoutKind.FillScreen ⇒ new FillAllScreenLayout(config, screenBounds, windows)
      case WindowsLayoutKind.WindowsStairs ⇒ new WindowsStairsLayout(config, screenBounds, windows)}
    calculator = Some(calc)
    //Calc and update positions
    val positions = calc.evalAll(states)
    toUpdateQueue = positions
    log.debug(s"[LayoutActor.constructCalcAndUpdatePositions] Calc: $calc positions:")
    positions.foreach(p ⇒ log.debug(
      s"[LayoutActor.constructCalcAndUpdatePositions]     ${states(Tuple2(p.drive, p.windowId)).title} --> $p"))
    //Run first update
    sendNextWindowUpdate()}
  /** layout window */
  def layoutWindow(drive: DriveRef, windowId: Int): Unit =  runIfWindowExist(drive,windowId){ (id, state) ⇒
    calculator match{
      case Some(c) ⇒
        val position = c.evalGiven(states, drive, windowId)
        log.debug(s"[LayoutActor.layoutWindow] drive: $drive, windowId: $windowId, position: $position")
        position.drive ! M.SetWindowPosition(position.windowId, position.pos.x, position.pos.y)
      case None ⇒
        log.error(s"[LayoutActor.layoutWindow] No calc setup, drive: $drive, windowId: $windowId")}}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Adding window to list
    case M.RegisterWindow(drive, windowId, state, prefs) ⇒
      addWindow(drive, windowId, state, prefs)
    //Update window state
    case M.WindowUpdated(drive, windowId, state) ⇒
      windowUpdated(drive, windowId, state)
    //Window position updated, remove from queue
    case M.WindowPositionUpdated(windowId) ⇒
      windowPositionUpdated(DriveRef(sender), windowId)
    //All drives construct, do initial layout
    case M.AllUiInitialized ⇒
      //TODO Do nothing for now
    case M.AllUiCreated ⇒
      constructCalcAndUpdatePositions(config.initialLayoutKind)
    //Layout of given window
    case M.LayoutWindow(drive, windowId) ⇒
      layoutWindow(drive, windowId)
    //Do layout
    case M.DoLayout(kind) ⇒
      constructCalcAndUpdatePositions(kind)}
  //Cleanup
  def cleanup(): Unit = { }}
