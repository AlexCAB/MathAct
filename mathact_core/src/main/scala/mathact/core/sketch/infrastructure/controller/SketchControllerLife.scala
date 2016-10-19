///* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
// * @                                                                             @ *
// *           #          # #                                 #    (c) 2016 CAB      *
// *          # #      # #                                  #  #                     *
// *         #  #    #  #           # #     # #           #     #              # #   *
// *        #   #  #   #             #       #          #        #              #    *
// *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
// *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
// *     #          #   # # # #   #       #      #  #           #  #         #       *
// *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
// *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
// * @                                                                             @ *
//\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */
//
//package mathact.core.sketch.infrastructure.controller
//
//import akka.actor.{ActorRef, PoisonPill}
//import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
//import mathact.core.model.messages.M
//
//import scala.collection.mutable.{ListBuffer => MutList}
//import scalafx.scene.paint.Color
//
///** SketchControllerActor sketch building
//  * Created by CAB on 04.09.2016.
//  */
//
//private [mathact] trait SketchControllerLife { _: SketchControllerActor ⇒
//  import SketchController._
//  import SketchUIElement._
//  import SketchUiElemState._
//  //Variables
//  private val allErrors = MutList[Throwable]()
//  //Methods
//  /** Start workbench controller */
//  def startSketchController(): Unit = {
//    log.debug(s"[SketchControllerLife.startSketchController] Start creating, update status string")
//    sketchUi ! M.SetSketchUIStatusString("Launching...", Color.Black)}
//  /** Construct sketch instance */
//  def constructSketchInstance(): Unit = {
//    log.debug(s"[SketchControllerLife.constructSketchInstance] All UI was shown, send CreateSketchInstance")
//    sketchInstance ! M.CreateSketchInstance}
//  /** Run plumbing building */
//  def runPlumbingBuilding(): Unit = {
//    log.debug("[SketchControllerLife.runPlumbingBuilding] Send BuildPlumbing.")
//    plumbing ! M.BuildPlumbing
//    //Update status string
//    sketchUi ! M.SetSketchUIStatusString("Building...", Color.Black)}
//  /** Plumbing successfully built */
//  def plumbingBuilt(): Unit = {
//    log.debug("[SketchControllerLife.plumbingBuilt] Plumbing successfully built.")
//    //User log and status
//    userLogging ! M.LogInfo(
//      None,
//      "Workbench",
//      s"Sketch '$sketchName' successfully built. Auto-run is off, hit 'play' button to start sketch.")
//    sketchUi ! M.SetSketchUIStatusString("Sketch built, wait for start", Color.Black)
//    //Update UI
//    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemEnabled))
//    //Send started to main controller
//    mainController ! M.SketchBuilt(sketchData.className)}
//  /** Auto start plumbing */
//  def autoStartPlumbing(): Unit = {
//    log.debug("[SketchControllerLife.autoStartPlumbing] Plumbing successfully built.")
//    //User log and status
//    userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built.")
//    sketchUi ! M.SetSketchUIStatusString("Sketch built, starting...", Color.Black)
//    //Update UI
//    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemDisabled))
//    //Send started to main controller and start plumbing
//    mainController ! M.SketchBuilt(sketchData.className)
//    plumbing ! M.StartPlumbing}
//  /** Start plumbing */
//  def startPlumbing(): Unit = {
//    log.debug(s"[SketchControllerLife.startPlumbing] Send StartPlumbing")
//    plumbing ! M.StartPlumbing
//    sketchUi ! M.SetSketchUIStatusString("Starting of plumbing...", Color.Black)}
//  /** Plumbing started, update UI and log to user log */
//  def plumbingStarted(): Unit = {
//    log.debug(s"[SketchControllerLife.plumbingStarted] Started, Update UI and log")
//    //Update UI
//    sketchUi ! M.UpdateSketchUIState(Map(
//      RunBtn → ElemDisabled,
//      StopSketchBtn → ElemEnabled,
//      ShowAllToolsUiBtn → ElemEnabled,
//      HideAllToolsUiBtn → ElemEnabled,
//      SkipAllTimeoutTaskBtn → ElemEnabled))
//    //User log
//    userLogging ! M.LogInfo(None, "Workbench", s"Plumbing started.")
//    //Update status string
//    sketchUi ! M.SetSketchUIStatusString("PlumbingA started. Working...", Color.Green)}
//  /** Try to stop Plumbing, send StopAndTerminatePlumbing */
//  def stopPlumbing(): Unit = {
//    log.debug(s"[SketchControllerLife.stopPlumbing] Try to stop Plumbing.")
//    //Set status
//    sketchUi ! M.SetSketchUIStatusString("Stopping of plumbing...", Color.Black)
//    //Send StopPlumbing
//    plumbing ! M.StopPlumbing}
//  /** Plumbing stopped, log to user logger */
//  def plumbingStopped(): Unit = {
//    log.debug(s"[SketchControllerLife.plumbingStopped] Stopped.")
//    //Log to user log
//    userLogging ! M.LogInfo(None, "Workbench", s"Plumbing stopped.")
//    //Update UI
//    sketchUi ! M.UpdateSketchUIState(Map(
//      RunBtn → ElemDisabled,
//      ShowAllToolsUiBtn → ElemDisabled,
//      HideAllToolsUiBtn → ElemDisabled,
//      SkipAllTimeoutTaskBtn → ElemDisabled,
//      StopSketchBtn → ElemDisabled))
//    //Update status string
//    sketchUi ! M.SetSketchUIStatusString("Plumbing stopped, wait for shutdown.", Color.Black)}
//  /** Shutdown plumbing */
//  def shutdownSketch(st: SketchController.State): Unit = { import SketchController.State._
//    log.debug(s"[SketchControllerLife.shutdownPlumbing] Update UI and send ShutdownPlumbing")
//    //Log to user log and update status Shutdown sequence initiated...
//    sketchUi ! M.SetSketchUIStatusString("Shutdown sequence initiated...", Color.Black)
//    userLogging ! M.LogInfo(None, "Workbench", "The Shutdown signal received, sketch will terminated.")
//    //Update UI
//    sketchUi ! M.UpdateSketchUIState(Map(
//      RunBtn → ElemDisabled,
//      ShowAllToolsUiBtn → ElemDisabled,
//      HideAllToolsUiBtn → ElemDisabled,
//      SkipAllTimeoutTaskBtn → (if(st == Starting || st == Working || st == Stopping) ElemEnabled else ElemDisabled),
//      StopSketchBtn → ElemDisabled))
//    //Send ShutdownPlumbing
//    plumbing ! M.ShutdownPlumbing}
//  /** Plumbing shutdown */
//  def plumbingShutdown(): Unit = {
//    log.debug(s"[SketchControllerLife.plumbingShutdown] Update UI and log")
//    //Log to user log and update status Shutdown sequence initiated...
//    userLogging ! M.LogInfo(None, "Workbench", "Plumbing shutdown.")
//    //Update UI
//    sketchUi ! M.UpdateSketchUIState(Map(
//      SkipAllTimeoutTaskBtn → ElemDisabled))}
//  /** Plumbing terminated */
//  def plumbingTerminated(): Unit = {
//    log.debug(s"[SketchControllerLife.plumbingTerminated] Terminate sketch instance, send TerminateSketchInstance")
//    sketchInstance ! M.TerminateSketchInstance}
//  /** Sketch instance error */
//  def sketchInstanceError(error: Throwable): Unit = {
//    log.error(s"[SketchControllerLife.sketchInstanceError] Update UI, shutdown plumbing, error: $error")
//    //Store errors
//    allErrors += error
//    //Update status string
//    sketchUi ! M.SetSketchUIStatusString("Sketch instance error! Check logs.", Color.Red)
//    //Update UI
//    sketchUi !  M.UpdateSketchUIState(Map(
//      RunBtn → ElemDisabled,
//      ShowAllToolsUiBtn → ElemDisabled,
//      HideAllToolsUiBtn → ElemDisabled,
//      SkipAllTimeoutTaskBtn → ElemDisabled,
//      StopSketchBtn → ElemDisabled))
//    //Shutdown plumbing
//    plumbing ! M.ShutdownPlumbing}
//  /** Plumbing error */
//  def  plumbingError(errors: Seq[Throwable]): Unit = {
//    log.error(s"[SketchControllerLife.sketchInstanceError] Update UI, terminate sketch instance, errors: $errors")
//    //Store errors
//    allErrors ++= errors
//    //Update status string
//    sketchUi ! M.SetSketchUIStatusString("Fatal plumbing error! Check logs.", Color.Red)
//    //Update UI
//    sketchUi !  M.UpdateSketchUIState(Map(
//      RunBtn → ElemDisabled,
//      ShowAllToolsUiBtn → ElemDisabled,
//      HideAllToolsUiBtn → ElemDisabled,
//      SkipAllTimeoutTaskBtn → ElemDisabled,
//      StopSketchBtn → ElemDisabled))
//    //Terminate sketch instance
//    plumbing ! M.ShutdownPlumbing}
//  /** Termination at SketchFailed state */
//  def terminationAtSketchFailed(actor: ActorRef): Unit = {
//    log.error(s"[SketchControllerLife.terminationAtSketchFailed] Terminated actor: $actor")}
//  /** Report to main controller and terminate self */
//  def reportAndTerminateSelf(st: SketchController.State): Unit = {
//    //Response
//    st match{
//      case SketchController.State.Shutdown ⇒
//        log.debug(
//          s"[SketchControllerLife.reportAndTerminateSelf @ Shutdown] Send SketchDone.")
//        mainController ! M.SketchDone(sketchData.className)
//      case SketchController.State.SketchFailed ⇒
//        log.error(
//          s"[SketchControllerLife.reportAndTerminateSelf @ SketchFailed] Send SketchError with errors: $allErrors.")
//        mainController ! M.SketchError(sketchData.className, allErrors.toList)}
//    //Terminate self
//    self ! PoisonPill}
//  /** Unexpected termination */
//  def unexpectedTermination(actor: ActorRef): Unit = {
//    log.error(s"[SketchControllerLife.unexpectedTermination] actor: $actor")
//    //Send error
//    mainController ! M.SketchError(
//      sketchData.className,
//      Seq(new IllegalStateException(s"[SketchControllerLife.unexpectedTermination] actor: $actor")))
//    //Terminate all and self
//    plumbing ! M.ShutdownPlumbing
//    sketchInstance ! M.TerminateSketchInstance
//    visualization ! M.TerminateVisualization
//    userLogging ! M.TerminateUserLogging
//    sketchUi ! M.TerminateSketchUI
//    self ! PoisonPill}}
