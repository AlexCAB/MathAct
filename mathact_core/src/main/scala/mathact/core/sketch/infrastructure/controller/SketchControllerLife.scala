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

package mathact.core.sketch.infrastructure.controller

import akka.actor.PoisonPill
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M
import mathact.core.sketch.blocks.WorkbenchLike

import scala.collection.mutable.{ListBuffer => MutList}
import scalafx.scene.paint.Color


/** SketchControllerActor sketch building
  * Created by CAB on 04.09.2016.
  */

private[core] trait SketchControllerLife { _: SketchControllerActor ⇒
  import SketchController._, SketchUIElement._, SketchUiElemState._
  //Variables
  private val allErrors = MutList[Throwable]()
  //Methods
  /** Start workbench controller */
  def startSketchController(): Unit = {
    log.debug(s"[SketchControllerLife.startSketchController] Start creating, update status string")
    sketchUi ! M.SetSketchUIStatusString("Launching...", Color.Black)}
  /** Construct sketch instance */
  def constructSketchInstance(): Unit = {
    log.debug(s"[SketchControllerLife.constructSketchInstance] All UI was shown, send CreateSketchInstance")
    sketchInstance ! M.CreateSketchInstance}
  /** Construct sketch instance */
  def sketchInstanceReady(instance: WorkbenchLike): Unit = {
    val newTitle = instance.sketchTitle.getOrElse(sketchName) + " - Mathact v0.2"
    log.debug(s"[SketchControllerLife.sketchInstanceReady] Update sketch UI title, newTitle: $newTitle")
    sketchUi ! M.UpdateSketchUITitle(newTitle)}
  /** Run plumbing building */
  def runPlumbingBuilding(): Unit = {
    log.debug("[SketchControllerLife.runPlumbingBuilding] Send BuildPlumbing.")
    plumbing ! M.BuildPlumbing
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Building...", Color.Black)}
  /** Sketch instance error */
  def sketchInstanceError(error: Throwable): Unit = {
    log.error(s"[SketchControllerLife.sketchInstanceError] Update UI, inform controller, error: $error")
    //Store errors
    allErrors += error
    //Report with SketchFail
    mainController ! M.SketchFail(sketchData.className)
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Sketch instance error! Check logs.", Color.Red)}
  /** Plumbing successfully built */
  def plumbingBuilt(): Unit = {
    log.debug("[SketchControllerLife.plumbingBuilt] Plumbing successfully built.")
    //User log and status
    userLogging ! M.LogInfo(
      None,
      "Workbench",
      s"Sketch '$sketchName' successfully built. Auto-run is off, hit 'play' button to start sketch.")
    sketchUi ! M.SetSketchUIStatusString("Sketch built, wait for start", Color.Black)
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemEnabled))
    //Send started to main controller
    mainController ! M.SketchBuilt(sketchData.className)}
  /** Auto start plumbing */
  def autoStartPlumbing(): Unit = {
    log.debug("[SketchControllerLife.autoStartPlumbing] Plumbing successfully built.")
    //User log and status
    userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built.")
    sketchUi ! M.SetSketchUIStatusString("Sketch built, starting...", Color.Black)
    //Send started to main controller and start plumbing
    mainController ! M.SketchBuilt(sketchData.className)
    plumbing ! M.StartPlumbing}
  /** Plumbing no drives found */
  def plumbingNoDrivesFound(): Unit = {
    log.debug("[SketchControllerLife.plumbingNoDrivesFound] Update status string and UI, send report to controller.")
    //Update UI
    sketchUi ! M.SetSketchUIStatusString("No blocks found, halted.", Color.Red)
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled,
      LayoutFillBtn → ElemDisabled,
      LayoutStairsBtn → ElemDisabled,
      ShowAllBlocksUiBtn → ElemDisabled,
      HideAllBlocksUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled))
    //Send started to main controller
    mainController ! M.SketchBuilt(sketchData.className)}
  /** Start plumbing */
  def startPlumbing(): Unit = {
    log.debug(s"[SketchControllerLife.startPlumbing] Send StartPlumbing")
    //Update UI
    sketchUi ! M.SetSketchUIStatusString("Starting of plumbing...", Color.Black)
    //Starting
    plumbing ! M.StartPlumbing}
  /** Plumbing started, update UI and log to user log */
  def plumbingStarted(): Unit = {
    log.debug(s"[SketchControllerLife.plumbingStarted] Started, Update UI and log")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemEnabled,
      LayoutFillBtn → ElemEnabled,
      LayoutStairsBtn → ElemEnabled,
      ShowAllBlocksUiBtn → ElemEnabled,
      HideAllBlocksUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Plumbing started. Working...", Color.Green)}
  /** Try to stop Plumbing, send StopAndTerminatePlumbing */
  def stopPlumbing(): Unit = {
    log.debug(s"[SketchControllerLife.stopPlumbing] Try to stop Plumbing.")
    //Set status
    sketchUi ! M.SetSketchUIStatusString("Stopping of plumbing...", Color.Black)
    //Send StopPlumbing
    plumbing ! M.StopPlumbing}
  /** Plumbing stopped, log to user logger */
  def plumbingStopped(): Unit = {
    log.debug(s"[SketchControllerLife.plumbingStopped] Stopped.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      LayoutFillBtn → ElemDisabled,
      LayoutStairsBtn → ElemDisabled,
      ShowAllBlocksUiBtn → ElemDisabled,
      HideAllBlocksUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Plumbing stopped, wait for shutdown.", Color.Black)}
  /** Close hit in not interrupt state */
  def closeHitInNotInterruptState(): Unit = {
    log.debug(s"[SketchControllerLife.closeHitInNotInterruptState] Update status string.")
    sketchUi ! M.SetSketchUIStatusString("Initiated shutdown sequence...", Color.Black)}
  /** Report to main controller and terminate self */
  def terminateSelf(mode: Mode): Unit = {
    //Response
    log.debug(s"[SketchControllerLife.terminateSelf in $mode] Send PoisonPill.")
    mode match{
      case Mode.Shutdown |  Mode.Work ⇒
        log.debug(s"[SketchControllerLife.terminateSelf @ Shutdown] Send SketchDone.")
        mainController ! M.SketchDone(sketchData.className)
      case Mode.Fail ⇒
        log.error(s"[SketchControllerLife.terminateSelf @ Fail] Send SketchError with errors: $allErrors.")
        mainController ! M.SketchError(sketchData.className, allErrors.toList)}
    //Terminate self
    self ! PoisonPill}}
