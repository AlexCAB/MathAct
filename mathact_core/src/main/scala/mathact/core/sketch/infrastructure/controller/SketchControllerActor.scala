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
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.{M, Msg, StateMsg}


/** Sketch controller
  * Created by CAB on 21.05.2016.
  */

private [mathact] abstract class SketchControllerActor(
  val config: MainConfigLike,
  val sketchData: SketchData,
  val mainController: ActorRef)
extends StateActorBase(SketchController.State.Init) with SketchControllerUIControl
with SketchControllerLife with SketchControllerUIActions
{ import SketchController.State._
 import SketchController._
 import SketchUIElement._
  //Values
  val sketchName = sketchData.sketchName.getOrElse(sketchData.className)

  //Variables
//  var isShutdown = false



  //Sub actors (abstract fields defined here to capture this actor context)
  val sketchUi: ActorRef
  val userLogging: ActorRef
  val visualization: ActorRef
  val pumping: ActorRef
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, SketchController.State), Unit] = {
    //On SketchControllerStart show all UI
    case (M.LaunchSketch, Init) ⇒
      state = Creating
      startSketchController()
      showAllSketchUi()
    //Shutdown sketch, log and run shutdown
    case (M.ShutdownSketch, _) ⇒     //TODO Может быть послан гдавным контроллером
      state = Shutdown
      shutdownSketch()



//    //Pumping error, log and run shutdown
//    case (M.PumpingShutdown, _) ⇒
//      state = Terminating
//      pumpingShutdown()
//
//    //Pumping error, log and run shutdown
//    case (M.PumpingError(errors), _) ⇒
//      state = Fail
//      pumpingError(errors)






//
//    //Shutdown sketch controller
//    case (ShutdownSketchController, st) ⇒
//








//    //On SketchDestructed switch to Terminating state, hide all UI (start terminating process)
//    case (SketchDestructed, Destructing) ⇒
//      state = Terminating
//      terminateAllUi()



  }
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, SketchController.State), Unit] = {
    //Check if all UI showed, and if so switch to Building and create Workbench instance
//    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Creating) ⇒
//      if(isAllUiShowed){
//        state = Constructing
//        constructSketchInstance()}
//    //Sketch instance built, run plumbing building
//    case (_: SketchInstanceReady, Constructing) ⇒
//      state = Building
//      runPlumbingBuilding()
//
//    case (M.PumpingBuilt, Building ) ⇒
//      state = Built
//      ???
//
//
//
//
//
//
//
//    //Sketch instance built error, switch to Fail and shutdown plumbing
//    case (_: SketchInstanceFail, Constructing ) ⇒
//      state = Fail
//
//
//
//
//
//
//
//
////    case (SketchInstanceBuiltError(error))
//
//
//
//
//
//
//
//
//    //Sketch instance built, auto run or wait for start
//    case (_: SketchInstanceBuilt, Building ) ⇒
//      sketchData.autorun match{
//        case false ⇒
//          log.debug(s"[Building] Sketch built, autorun == false, wait for start.")
//          state = Built
//        case true ⇒
//          log.debug(s"[Building] Sketch built, autorun == true, satrting of plumping")
//          state = Starting
//          startPumping()}
//
//
//
//
//
//
//
//
//
//    //Check if all UI showed, shutdown plumbing
//    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Shutdown | Fail) ⇒
//      isAllUiShowed match{
//        case true  ⇒
//          state = Terminating
//          shutdownPlumbing()
//        case false ⇒
//          log.debug(s"[SketchControllerActor in Shutdown] Not all UI showed yet.")}









//    //Sketch instance built, shutdown plumbing
//    case (_: SketchInstanceBuilt, Shutdown | Fail) ⇒
//      state = Terminating
//      shutdownPlumbing()
//
//
//
//
//
//
//
//
//
//
//
////      isAllUiShowed match{
////      case true ⇒
////        log.debug("[DriveCreating] All UI showed, run sketch building.")
////
////      case false ⇒
////        log.debug(s"[DriveCreating] Not all UI showed yet.")}
////
//
//
//    //Check if all UI showed, and if so switch to Terminating destrtoy UI
//    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Shutdown) ⇒





//
//      isAllUiShowed match{
//        case true ⇒ isShutdown match{
//          case false ⇒
//            log.debug("[DriveCreating] All UI showed, run sketch building.")
//            state = Building
//            sketchRunBuilding()
//          case true ⇒
//            log.debug("[DriveCreating] All UI showed, but isShutdown == true, hide UI")
//            state = Terminating
//            terminateAllUi()}
//        case false ⇒
//          log.debug(s"[DriveCreating] Not all UI showed yet.")}
//


    //Sketch built, set Built state if no autorun or Starting else
    case (M.PumpingBuilt, Building) ⇒


//      isShutdown match{
//      case false ⇒
//
//      case true ⇒
//        log.debug(s"[Building] Sketch built, but isShutdown == true, switch to Destructing")
//        state = Destructing
////        destructSketch()
//
//    }
//    //If receive SketchBuiltError or SketchBuiltTimeout in Building state, switch state to BuildingFailed
//    case (_: SketchBuiltError | SketchBuiltTimeout | M.PumpingBuildingError, Building) ⇒
//      state = BuildingFailed
    //If RunBtn hit switch to Starting state
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒
      startPumping()
      state = Starting
//    //If plumbing started set state Working
//    case (M.PumpingStarted, Starting) ⇒ isShutdown match{
//      case false ⇒
//        log.debug(s"[Starting] Sketch started.")
//        state = Working
//      case true ⇒
//        log.debug(s"[Starting] Sketch started, but isShutdown == true, stopping sketch")
//        state = Stopping
//        stopPumping()}
//    //If StopSketchBtn hit, switch to Stopping
//    case (M.SketchUIActionTriggered(StopSketchBtn, _), Working) ⇒
//      state = Stopping
//    //If pumping stopped, deconstruct sketch
//    case (M.PumpingTerminated, Stopping) ⇒
//      isShutdown match{
//        case true ⇒
//          log.debug(s"[PumpingTerminated] isShutdown == true, continue shutdown.")
//          state = Destructing
//          destructSketch()
//        case false ⇒
//          log.debug(s"[PumpingTerminated] isShutdown == false, switch to Stopped.")
//          state = Stopped}
//    //If All UI terminated, response with SketchControllerTerminated t main controller and  stop a self.
//    case (M.SketchUITerminated | M.UserLoggingTerminated | M.VisualizationTerminated, Terminating) ⇒
//      isAllUiTerminated match{
//        case true ⇒
//          log.debug(s"[Terminating] All UI terminated, stop a self.")
//          terminateSelf()
//        case false ⇒
//          log.debug(s"[Terminating] Not all UI terminated yet.")}
//
  }
  /** Handling of actor termination*/
  def terminationHandling: PartialFunction[(ActorRef, SketchController.State), Unit] = {

    case m  ⇒ ???

  }
  /** Actor reaction on messages (not change the 'state' variable) */
  def reaction: PartialFunction[(Msg, SketchController.State), Unit] = {
    //From objects asks
//    case (M.GetSketchContext(sender), _) ⇒ sender ! getSketchContext
//    //UI showed/headed
//    case (M.SketchUIChanged(isShow), _) ⇒ sketchUiChanged(isShow)
//    case (M.UserLoggingUIChanged(isShow), _) ⇒ userLoggingUIChanged(isShow)
//    case (M.VisualizationUIChanged(isShow), _) ⇒ visualizationUIChanged(isShow)
//    //Sketch instance building
//    case (SketchInstanceBuilt(sketchInstance), st) ⇒ sketchInstanceBuilt(sketchInstance, st)
//    case (SketchInstanceBuiltError(error), Constructing) ⇒ sketchInstanceBuiltError(error)
//    case (SketchInstanceBuildTimeout, st) ⇒ sketchInstanceBuiltTimeout(st)








//    case (SketchBuilt(sketchInstance), Building) ⇒ sketchBuilt(sketchInstance)
//    case (M.PumpingBuilt, Building) ⇒ pumpingBuilt()
//    case (SketchBuiltError(error), Building) ⇒ sketchBuiltError(error)
////    case (M.PumpingBuildingError, Building) ⇒ pumpingBuildingError()
//    case (SketchBuilt(sketchInstance), BuildingFailed) ⇒ lateBuiltMessage()
////    case (M.PumpingBuilt, BuildingFailed) ⇒ lateBuiltMessage()
////    case (M.PumpingBuildingAbort, Building) ⇒ pumpingBuildingAbort()
//    case (SketchBuiltTimeout, state) ⇒ sketchBuiltTimeout(state)
//    case (M.PumpingStarted, Starting) ⇒ pumpingStarted()
////    case (M.PumpingStartingAbort, Starting) ⇒ pumpingStartingAbort()
////    case (M.PumpingTerminated, Stopping) ⇒ pumpingStopped()






    //UI actions
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒ hitRunBtn()
    case (M.SketchUIActionTriggered(ShowAllToolsUiBtn, _), Working) ⇒ showAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(HideAllToolsUiBtn, _), Working) ⇒ hideAllToolsUiBtnHit()
    case (M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, _), Working) ⇒ skipAllTimeoutTaskBtnHit()
    case (M.SketchUIActionTriggered(LogBtn, act: SketchUiElemState), _) ⇒ logBtnHit(act)
    case (M.SketchUIActionTriggered(VisualisationBtn, act: SketchUiElemState), _) ⇒ visualisationBtnHit(act)
    case (M.SketchUIActionTriggered(StopSketchBtn, _), Working) ⇒ stopPumping()
    case (M.SketchUIActionTriggered(CloseBtn, _), s) if s != Init ⇒ closeBtnHit()




    //UI terminated






//    case (M.SketchUITerminated, _) ⇒ sketchUITerminated()
//    case (M.UserLoggingTerminated, _) ⇒ userLoggingTerminated()
//    case (M.VisualizationTerminated, _) ⇒ visualizationTerminated()

  }


}
