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

package mathact.core.control.infrastructure

import java.util.concurrent.{TimeoutException, ExecutionException}

import akka.actor.PoisonPill
import mathact.core.bricks.{WorkbenchLike, SketchContext}
import mathact.core.model.enums.{ActorState, SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M

import scala.concurrent.Future
import scalafx.scene.paint.Color


/** SketchController sketch building
  * Created by CAB on 04.09.2016.
  */

private [mathact] trait SketchControllerLife { _: SketchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Variables
  private var isSketchContextBuilt = false
  //Functions
  private def buildingError(error: Throwable): Unit = {  //Called only until plumbing run
    //Build message
    val msg = error match{
      case err: NoSuchMethodException ⇒ s"NoSuchMethodException, check if sketch class is not inner."
      case err ⇒ s"Exception on building of sketch."}
    //Log to user logging
    userLogging ! M.LogError(None, "Workbench", Some(error), msg)
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))
    //Inform MainController
    mainController ! M.SketchError(sketchData.className, error)
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Building error! Check logs.", Color.Red)}
  //Methods
  /** Start workbench controller */
  def startSketchController(): Unit = {
    log.debug(s"[SketchControllerLife.startSketchController] Start creating.")
    sketchUi ! M.SetSketchUIStatusString("Creating...", Color.Black)}
  /** Sketch run building, called after all UI shown */
  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[SketchControllerLife.sketchRunBuilding] Try to create Workbench instance, " +
      s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
    //Run building timeout
    context.system.scheduler.scheduleOnce(
      config.sketchBuildingTimeout,
      self,
      SketchBuiltTimeout)
    //Build sketch
    Future{sketchData.clazz.newInstance()}
      .map{ s ⇒ self ! SketchBuilt(s.asInstanceOf[WorkbenchLike])}
      .recover{
        case t: ExecutionException ⇒ self ! SketchBuiltError(t.getCause)
        case t: Throwable ⇒ self ! SketchBuiltError(t)}
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Building...", Color.Black)}
  /** Get workbench context, create and return of SketchContext
    * @return - Either[Exception, SketchContext] */
  def getSketchContext: Either[Exception, SketchContext] = isSketchContextBuilt match{
    case false ⇒
      log.debug(s"[SketchControllerLife.getSketchContext] Build SketchContext")
      val response = Right{ new SketchContext(
        context.system,
        mainController,
        pumping,
        config.pumping.pump,
        config.config)}
      isSketchContextBuilt = true
      response
    case true⇒
      val err = new IllegalStateException(s"[SketchControllerLife.getSketchContext] Context already created.")
      log.error(err, s"[SketchControllerLife.getSketchContext] Error on creating.")
      Left(err)}
  /** Sketch successfully built
    * @param workbench - WorkbenchLike */
  def sketchBuilt(workbench: WorkbenchLike): Unit = {
    //Check if SketchContext built
    isSketchContextBuilt match{
      case true ⇒
        log.debug(s"[SketchControllerLife.sketchBuilt] workbench: $workbench, run of pumping building.")
        //Run pumping building
        pumping ! M.BuildPumping
      case false ⇒
        log.error(s"[SketchControllerLife.sketchBuilt] Building failed, SketchContext is not built.")
        buildingError(new IllegalStateException(
          "[SketchControllerLife.sketchBuilt] SketchContext is not built."))}}
  /** Pumping successfully built */
  def pumpingBuilt(): Unit = {
    log.debug("[SketchControllerLife.pumpingBuilt] Pumping successfully built.")
    //User log
    val autorunMsg = sketchData.autorun match{
      case false ⇒ ". Auto-run is off, hit 'play' button to start sketch."
      case true ⇒ "."}
    userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built$autorunMsg")
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → (if(sketchData.autorun) ElemDisabled else ElemEnabled)))
    //Send started to main controller
    mainController ! M.SketchBuilt(sketchData.className)
    //Update status string
    sketchUi ! M.SetSketchUIStatusString(
      s"Sketch built. ${if(sketchData.autorun) "" else "Wait for start."}",
      if(sketchData.autorun) Color.Black else Color.Green)}
  /** Error during sketch building
    * @param error - Throwable */
  def sketchBuiltError(error: Throwable): Unit = {
    log.error(
      error,
      s"[SketchControllerLife.sketchBuildingError] Error on creating Sketch extends Workbench instance.")
     buildingError(error)}
  /** Error during pumping building */
  def pumpingBuildingError(): Unit = {
    log.error(
      s"[SketchControllerLife.pumpingBuildingError] Error on building of pumping.")
    buildingError(new Exception(
      "[SketchControllerLife.pumpingBuildingError] Error on building of pumping."))}
  /** Sketch not build in required time.
    * @param state - ActorState */
  def sketchBuiltTimeout(state: ActorState): Unit = state match{
    case ActorState.Building ⇒
      log.error(
        s"[SketchControllerLife.sketchBuiltTimeout] Building failed, sketch not built " +
          s"in ${config.sketchBuildingTimeout}.")
      buildingError(new TimeoutException(
        s"[SketchControllerLife.sketchBuiltTimeout] Sketch not built in ${config.sketchBuildingTimeout}"))
    case st ⇒
      log.debug(s"[SketchControllerLife.sketchBuiltTimeout] Not a Building state do nothing, state: $st")}
  /** Pumping starting aborted */
  def pumpingBuildingAbort(): Unit = {
    log.debug(s"[SketchControllerLife.pumpingBuildingAbort] For now do nothing.")}
  /** Start pumping */
  def startPumping(): Unit = {
    log.debug(s"[SketchControllerLife.startPumping] Send StartPumping")
    pumping ! M.StartPumping
    sketchUi ! M.SetSketchUIStatusString("Starting of pumping...", Color.Black)}
  /** Pumping starting aborted */
  def pumpingStartingAbort(): Unit = {
    log.debug(s"[SketchControllerLife.pumpingStartingAbort] For now do nothing.")}
  /** PumpingActor started, update UI and log to user log */
  def pumpingStarted(): Unit = {
    log.debug(s"[SketchControllerLife.pumpingStarted] Started.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemEnabled,
      ShowAllToolsUiBtn → ElemEnabled,
      HideAllToolsUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))
    //User log
    userLogging ! M.LogInfo(None, "Workbench", s"PumpingActor started.")
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("PumpingActor started. Working...", Color.Green)}
  /** Try to stop PumpingActor, send StopAndTerminatePumping */
  def stopPumping(): Unit = {
    log.debug(s"[SketchControllerLife.stopPumping] Try to stop PumpingActor.")
    pumping ! M.StopAndTerminatePumping
    sketchUi ! M.SetSketchUIStatusString("Stopping of pumping...", Color.Black)}
  /** PumpingActor stopped, log to user logger */
  def pumpingStopped(): Unit = {
    log.debug(s"[SketchControllerLife.pumpingStopped] Stopped.")
    //Log to user log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping stopped.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("PumpingActor stopped.", Color.Black)}
  /** Sketch built, but SketchBuiltTimeout received earlier */
  def lateBuiltMessage(): Unit = {
    log.debug(
      s"[Building] SketchBuilt receive but state BuildingFailed (probably SketchBuiltTimeout received earlier).")}
  /** Starting of destruct sketch */
  def destructSketch(): Unit = {
    log.debug(s"[SketchControllerLife.destructSketch] Starting of destruct sketch.")
    mainController ! M.SketchDone(sketchData.className)
    self ! SketchDestructed
    sketchUi ! M.SetSketchUIStatusString("Destructing...", Color.Black)}
  /** Shutdown workbench controller */
  def shutdownSketchController(): Unit = {
    log.debug(s"[SketchControllerLife.shutdownSketchController] Shutdown.")
    userLogging ! M.LogInfo(None, "Workbench", "The Shutdown signal received, sketch will terminated.")
    sketchUi ! M.SetSketchUIStatusString("Shutdown signal received.", Color.Black)}
  /** Terminate self */
  def terminateSelf(): Unit = {
    log.debug(s"[SketchControllerLife.terminateSelf] Send SketchControllerTerminated and terminate.")
    mainController ! M.SketchControllerTerminated(sketchData.className)
    self ! PoisonPill}}
