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

import akka.actor.ActorRef
import mathact.core.bricks.WorkbenchLike
import mathact.core.model.config.MainConfigLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums.{SketchUiElemState, SketchUIElement, ActorState}
import mathact.core.model.messages.{StateMsg, M, Msg}
import mathact.core.StateActorBase


/** Sketch controller
  * Created by CAB on 21.05.2016.
  */

private [mathact] abstract class SketchController(
  val config: MainConfigLike,
  val sketchData: SketchData,
  val mainController: ActorRef)
extends StateActorBase(ActorState.Init) with SketchControllerUIControl
with SketchControllerLife with SketchControllerUIActions
{ import ActorState._, SketchUIElement._
  //Values
  val sketchName = sketchData.sketchName.getOrElse(sketchData.className)
  //Messages
  case class SketchBuilt(instance: WorkbenchLike) extends Msg
  case class SketchBuiltError(error: Throwable) extends Msg
  case object SketchBuiltTimeout extends Msg
  case object SketchDestructed extends StateMsg
  //Variables
  var isShutdown = false
  //Sub actors (abstract fields defined here to capture this actor context)
  val sketchUi: ActorRef
  val userLogging: ActorRef
  val visualization: ActorRef
  val pumping: ActorRef
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    //On SketchControllerStart show all UI
    case (M.StartSketchController, Init) ⇒
      state = Creating
      startSketchController()
      showAllUi()
    //On SketchControllerStart trigger shutdown process on any state
    case (M.ShutdownSketchController, _) ⇒
      shutdownSketchController()
      isShutdown = true
      state match{
        case Built ⇒
          state = Destructing
          destructSketch()
        case BuildingFailed ⇒
          state = Terminating
          terminateAllUi()
        case Working ⇒
          state = Stopping
          stopPumping()
        case _ ⇒}
    //On SketchDestructed switch to Terminating state, hide all UI (start terminating process)
    case (SketchDestructed, Destructing) ⇒
      state = Terminating
      terminateAllUi()}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all UI showed, and if so switch to Building, and create Workbench instance
    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Creating) ⇒
      isAllUiShowed match{
        case true ⇒ isShutdown match{
          case false ⇒
            log.debug("[Creating] All UI showed, run sketch building.")
            state = Building
            sketchRunBuilding()
          case true ⇒
            log.debug("[Creating] All UI showed, but isShutdown == true, hide UI")
            state = Terminating
            terminateAllUi()}
        case false ⇒
          log.debug(s"[Creating] Not all UI showed yet.")}
    //Sketch built, set Built state if no autorun or Starting else
    case (_: SketchBuilt, Building) ⇒ isShutdown match{
      case false ⇒
        sketchData.autorun match{
          case false ⇒
            log.debug(s"[Building] Sketch built, autorun == false, wait for start.")
            state = Built
          case true ⇒
            log.debug(s"[Building] Sketch built, autorun == true, satrting of plumping")
            startPumping()
            state = Starting}
      case true ⇒
        log.debug(s"[Building] Sketch built, but isShutdown == true, switch to Destructing")
        state = Destructing
        destructSketch()}
    //If receive SketchBuiltError or SketchBuiltTimeout in Building state, switch state to BuildingFailed
    case (_: SketchBuiltError | SketchBuiltTimeout, Building) ⇒
      state = BuildingFailed
    //If RunBtn hit switch to Starting state
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒
      startPumping()
      state = Starting
    //If plumbing started set state Working
    case (M.PumpingStarted, Starting) ⇒ isShutdown match{
      case false ⇒
        log.debug(s"[Starting] Sketch started.")
        state = Working
      case true ⇒
        log.debug(s"[Starting] Sketch started, but isShutdown == true, stopping sketch")
        state = Stopping
        stopPumping()}
    //If StopSketchBtn hit, switch to Stopping
    case (M.SketchUIActionTriggered(StopSketchBtn, _), Working) ⇒
      state = Stopping
    //If pumping stopped, deconstruct sketch
    case (M.PumpingStopped, Stopping) ⇒
      isShutdown match{
        case true ⇒
          log.debug(s"[PumpingStopped] isShutdown == true, continue shutdown.")
          state = Destructing
          destructSketch()
        case false ⇒
          log.debug(s"[PumpingStopped] isShutdown == false, switch to Stopped.")
          state = Stopped}
    //If All UI terminated, response with SketchControllerTerminated t main controller and  stop a self.
    case (M.SketchUITerminated | M.UserLoggingTerminated | M.VisualizationTerminated, Terminating) ⇒
      isAllUiTerminated match{
        case true ⇒
          log.debug(s"[Terminating] All UI terminated, stop a self.")
          terminateSelf()
        case false ⇒
          log.debug(s"[Terminating] Not all UI terminated yet.")}}
  /** Actor reaction on messages (not change the 'state' variable) */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //From objects asks
    case (M.GetSketchContext(sender), _) ⇒ sender ! getSketchContext
    //UI showed/headed
    case (M.SketchUIChanged(isShow), _) ⇒ sketchUiChanged(isShow)
    case (M.UserLoggingUIChanged(isShow), _) ⇒ userLoggingUIChanged(isShow)
    case (M.VisualizationUIChanged(isShow), _) ⇒ visualizationUIChanged(isShow)
    //Sketch building
    case (SketchBuilt(sketchInstance), Building) ⇒ sketchBuilt(sketchInstance)
    case (SketchBuilt(sketchInstance), BuildingFailed) ⇒ lateSketchBuilt()
    case (SketchBuiltError(error), Building) ⇒ sketchBuiltError(error)
    case (SketchBuiltTimeout, state) ⇒ sketchBuiltTimeout(state)
    case (M.PumpingStarted, Starting) ⇒ pumpingStarted()
    case (M.PumpingStopped, Stopping) ⇒ pumpingStopped()
    //UI actions
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒ hitRunBtn()
    case (M.SketchUIActionTriggered(ShowAllToolsUiBtn, _), Working) ⇒ showAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(HideAllToolsUiBtn, _), Working) ⇒ hideAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, _), Working) ⇒ skipAllTimeoutTaskBtnHit()
    case (M.SketchUIActionTriggered(LogBtn, act: SketchUiElemState), Working) ⇒ logBtnHit(act)
    case (M.SketchUIActionTriggered(VisualisationBtn, act: SketchUiElemState), Working) ⇒ visualisationBtnHit(act)
    case (M.SketchUIActionTriggered(StopSketchBtn, _), Working) ⇒ stopPumping()
    case (M.SketchUIActionTriggered(CloseBtn, _), s) if s != Init ⇒ closeBtnHit()
    //UI terminated
    case (M.SketchUITerminated, _) ⇒ sketchUITerminated()
    case (M.UserLoggingTerminated, _) ⇒ userLoggingTerminated()
    case (M.VisualizationTerminated, _) ⇒ visualizationTerminated()}}
