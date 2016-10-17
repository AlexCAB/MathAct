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

package mathact.core.sketch.infrastructure.controller

import java.util.concurrent.{ExecutionException, TimeoutException}

import akka.actor.PoisonPill
import mathact.core.bricks.{SketchContext, WorkbenchLike}
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M

import scala.collection.mutable.{ListBuffer => MutList}
import scala.concurrent.Future
import scalafx.scene.paint.Color

/** SketchControllerActor sketch building
  * Created by CAB on 04.09.2016.
  */

private [mathact] trait SketchControllerLife { _: SketchControllerActor ⇒
  import SketchController._
  import SketchUIElement._
  import SketchUiElemState._
  //Variables
  private val drivesErrors = MutList[Throwable]()
  //Functions
  def sketchError(error: Throwable): Unit = {  //Called only until plumbing run
    //Build message
    val msg = error match{
      case err: NoSuchMethodException ⇒ s"NoSuchMethodException, check if sketch class is not inner."
      case err ⇒ s"Exception on building of sketch."}
    //Log to user logging
    userLogging ! M.LogError(None, "Workbench", Seq(error), msg)
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
    sketchUi ! M.SetSketchUIStatusString("Launching...", Color.Black)}





















  //Run plumbing building
  def runPlumbingBuilding(): Unit = {
    log.debug("[SketchControllerLife.runPlumbingBuilding] Send BuildPumping.")
    pumping ! M.BuildPumping}


  //Update status string
  sketchUi ! M.SetSketchUIStatusString("Building...", Color.Black)





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






//  /** Error during pumping building */
//  def pumpingBuildingError(): Unit = {
//    log.error(
//      s"[SketchControllerLife.pumpingBuildingError] Error on building of pumping.")
//    buildingError(new Exception(
//      "[SketchControllerLife.pumpingBuildingError] Error on building of pumping."))}

  /** Pumping starting aborted */
  def pumpingBuildingAbort(): Unit = {
    log.debug(s"[SketchControllerLife.pumpingBuildingAbort] For now do nothing.")}
  /** Start pumping */
  def startPumping(): Unit = {
    log.debug(s"[SketchControllerLife.startPumping] Send StartPumping")
    pumping ! M.StartPumping
    sketchUi ! M.SetSketchUIStatusString("Starting of pumping...", Color.Black)}
//  /** Pumping starting aborted */
//  def pumpingStartingAbort(): Unit = {
//    log.debug(s"[SketchControllerLife.pumpingStartingAbort] For now do nothing.")}
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
//    pumping ! M.StopAndTerminatePumping
    ???
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


  def shutdownPlumbing(): Unit = {
    log.debug(s"[SketchControllerLife.shutdownPlumbing] Log and send ShutdownPumping")
    userLogging ! M.LogInfo(None, "Workbench", s"Shutdown plumbing.")
    pumping ! M.ShutdownPumping}


//  /** Sketch built, but SketchBuiltTimeout received earlier */
//  def lateBuiltMessage(): Unit = {
//    log.debug(
//      s"[Building] SketchBuilt receive but state BuildingFailed (probably SketchBuiltTimeout received earlier).")}
  /** Starting of destruct sketch */
//  def destructSketch(): Unit = {
//    log.debug(s"[SketchControllerLife.destructSketch] Starting of destruct sketch.")
//    mainController ! M.SketchDone(sketchData.className)
//    self ! SketchDestructed
//    sketchUi ! M.SetSketchUIStatusString("Destructing...", Color.Black)}


  /** Shutdown workbench controller */
  def shutdownSketch(): Unit = {
    log.debug(s"[SketchControllerLife.shutdownSketchController] Shutdown, log and sending ShutdownPumping.")
    userLogging ! M.LogInfo(None, "Workbench", "The Shutdown signal received, sketch will terminated.")
    sketchUi ! M.SetSketchUIStatusString("Shutdown sequence initiated...", Color.Black)
    pumping ! M.ShutdownPumping}


//  /** Pumping shutdown */
//  def pumpingShutdown(): Unit = {
//    log.debug(s"[SketchControllerLife.pumpingShutdown] errors: $errors")
//    //Save errors
//    drivesErrors ++= errors
//    //Log and update status
//    userLogging ! M.LogError(None, "Workbench", errors, "Fatal pumping error, sketch will terminated.")
//    sketchUi ! M.SetSketchUIStatusString("Fatal pumping error, shutdown...", Color.Red)}




  /** Pumping error */
  def pumpingError(errors: Seq[Throwable]): Unit = {
    log.debug(s"[SketchControllerLife.pumpingError] errors: $errors")
    //Save errors
    drivesErrors ++= errors
    //Log and update status
    userLogging ! M.LogError(None, "Workbench", errors, "Fatal pumping error.")
    sketchUi ! M.SetSketchUIStatusString("Fatal pumping error!", Color.Red)}



//
//
//  PumpingShutdown



//
//  def shutdownSketchController(state: State): State = state match{
//    case State.Creating | State.Building  | State.Starting | State.Stopping  ⇒
//      log.debug(s"[SketchControllerLife.shutdownSketchController] In state $state do nothing, wait for end operation.")
//      state
//    case State.Built ⇒
//
//
//
//    case State.Built ⇒
//    case State.Built ⇒
//
//
//      case Built ⇒
//        state = Destructing
//        destructSketch()
//      //        case BuildingFailed ⇒
//      //          state = Terminating
//      //          terminateAllUi()
//      case Working ⇒
//        state = Stopping
//        stopPumping()
//      case _ ⇒
//
//
//
//
//  }
//











  /** Terminate self */
  def terminateSelf(): Unit = {
    log.debug(s"[SketchControllerLife.terminateSelf] Send SketchControllerTerminated and terminate.")
//    mainController ! M.SketchControllerTerminated(sketchData.className)
    ???
    self ! PoisonPill}}
