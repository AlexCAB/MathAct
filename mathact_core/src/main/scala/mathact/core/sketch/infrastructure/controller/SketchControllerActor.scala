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

import akka.actor.ActorRef
import mathact.core.StateActorBase
import mathact.core.model.config.MainConfigLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums._
import mathact.core.model.messages.{M, Msg, StateMsg}


/** Sketch controller
  * Created by CAB on 21.05.2016.
  */

private [mathact] abstract class SketchControllerActor(
  val config: MainConfigLike,
  val sketchData: SketchData,
  val mainController: ActorRef)
extends StateActorBase(SketchController.State.Init) with SketchControllerUIControl
with SketchControllerLife with SketchControllerUIActions{ import SketchController.State._, SketchUIElement._
  //Values
  val sketchName = sketchData.sketchName.getOrElse(sketchData.className)
  //Sub actors (abstract fields defined here to capture this actor context)
  val sketchUi: ActorRef
  val userLogging: ActorRef
  val visualization: ActorRef
  val plumbing: ActorRef
  val sketchInstance: ActorRef
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, SketchController.State), Unit] = {
    //On SketchControllerStart show all UI
    case (M.LaunchSketch, Init) ⇒
      state = Creating
      startSketchController()
      showAllSketchUi()
    //Shutdown sketch, log and run shutdown (may be send by main controller on him error)
    case (M.ShutdownSketch, st) ⇒
      state = Shutdown
      shutdownSketch(st)}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, SketchController.State), Unit] = {
    //Check if all UI showed, and if so switch to Building and create Workbench instance
    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Creating) ⇒
      if(isAllUiShowed){
        state = Constructing
        constructSketchInstance()}
    //Sketch instance built, run plumbing building
    case (_: M.SketchInstanceReady, Constructing) ⇒
      state = Building
      runPlumbingBuilding()
    //Plumbing built, start plumbing if auto run or wait for user command
    case (M.PlumbingBuilt, Building) ⇒
      sketchData.autorun match{
        case false ⇒
          state = Built
          plumbingBuilt()
        case true ⇒
          state = Starting
          autoStartPlumbing()}
    //If RunBtn hit switch to Starting state
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒
      state = Starting
      hitRunBtn()
      startPlumbing()
    //If plumbing started set state Working
    case (M.PlumbingStarted, Starting) ⇒
      state = Working
      plumbingStarted()
    //If StopSketchBtn hit, switch to Stopping
    case (M.SketchUIActionTriggered(StopSketchBtn, _), Working) ⇒
      state = Stopping
      stopPlumbing()
    //If plumbing stopped, switch to Stopped, wait for shutdown
    case (M.PlumbingStopped, Stopping) ⇒
      state = Stopped
      plumbingStopped()
    //On hit close start shutdown
    case (M.SketchUIActionTriggered(CloseBtn, _), st) if st != Init && st != Creating ⇒
      state = Shutdown
      shutdownSketch(st)
    //Sketch instance error, shutdown plumbing
    case (M.SketchInstanceError(error), _) ⇒
      state = SketchFailed
      sketchInstanceError(error)
    //Plumbing error, terminate sketch instance
    case (M.PlumbingError(errors), _) ⇒
      state = SketchFailed
      plumbingError(errors)
    //On hit close start shutdown after fail
    case (M.SketchUIActionTriggered(CloseBtn, _), SketchFailed) ⇒
      terminateAllUi()}
  /** Handling of actor termination*/
  def terminationHandling: PartialFunction[(ActorRef, SketchController.State), Unit] = {
    //Plumbing terminated terminate sketch instance
    case (`plumbing`, Shutdown) ⇒
      plumbingTerminated()
    //Sketch instance terminated, terminate UI
    case (`sketchInstance`, Shutdown) ⇒
      terminateAllUi()
    //Check if all UI terminated and if so response to main controlled and terminate self
    case (`visualization` | `userLogging` | `sketchUi`, st) if st == Shutdown && st == SketchFailed ⇒
    if(isAllUiTerminated) reportAndTerminateSelf(st)
    //Plumbing terminated at PlumbingFail, switch to SketchFailed
    case (actor, SketchFailed) if actor == plumbing || actor == sketchInstance ⇒
      terminationAtSketchFailed(actor)
    //If unexpected termination, terminate all and self
    case (actor, _) ⇒
      unexpectedTermination(actor)}
  /** Actor reaction on messages (not change the 'state' variable) */
  def reaction: PartialFunction[(Msg, SketchController.State), Unit] = {
    //From objects asks
    case (M.GetSketchContext(actor), _) ⇒ sketchInstance ! M.BuildSketchContextFor(actor)
    //UI showed/headed
    case (M.SketchUIChanged(isShow), _) ⇒ sketchUiChanged(isShow)
    case (M.UserLoggingUIChanged(isShow), _) ⇒ userLoggingUIChanged(isShow)
    case (M.VisualizationUIChanged(isShow), _) ⇒ visualizationUIChanged(isShow)
    //Plumbing
    case (M.PlumbingShutdown, _) ⇒ plumbingShutdown()
    //UI actions
    case (M.SketchUIActionTriggered(ShowAllToolsUiBtn, _), Working) ⇒ showAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(HideAllToolsUiBtn, _), Working) ⇒ hideAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, _), Working) ⇒ skipAllTimeoutTaskBtnHit()
    case (M.SketchUIActionTriggered(LogBtn, act: SketchUiElemState), _) ⇒ logBtnHit(act)
    case (M.SketchUIActionTriggered(VisualisationBtn, act: SketchUiElemState), _) ⇒ visualisationBtnHit(act)}}
