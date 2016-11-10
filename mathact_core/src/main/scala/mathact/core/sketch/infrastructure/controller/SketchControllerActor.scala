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

import mathact.core.ControllerBase
import mathact.core.bricks.data.SketchData
import mathact.core.model.config.MainConfigLike
import mathact.core.model.enums._
import mathact.core.model.holders._
import mathact.core.model.messages.{M, Msg}


/** Sketch controller
  * Created by CAB on 21.05.2016.
  */

private[core] abstract class SketchControllerActor(
  val config: MainConfigLike,
  val sketchData: SketchData,
  val mainController: MainControllerRef)
extends ControllerBase((SketchController.State.Init, SketchController.Mode.Work))
with SketchControllerUIControl with SketchControllerLife
with SketchControllerUIActions{ import SketchController._, State._, Mode._, SketchUIElement._
  //Values
  val sketchName = sketchData.sketchName.getOrElse(sketchData.className)
  //Sub actors (abstract fields defined here to capture this actor context)
  val sketchUi: SketchUIRef
  val userLogging: UserLoggingRef
  val visualization: VisualizationRef
  val layout: LayoutRef
  val plumbing: PlumbingRef
  val sketchInstance: SketchInstanceRef
  //Message handling
  def reaction: PartialFunction[(Msg, (State, Mode)), (State, Mode)] = {
    //On SketchControllerStart show all UI
    case (M.LaunchSketch, (Init, mode)) ⇒
      startSketchController()
      showAllSketchUi()
      (Creating, mode)
    //Check if all UI showed, and if so switch to Building and create Workbench instance
    case (M.SketchUIChanged(isShow), (Creating, mode)) ⇒
      sketchUiChanged(isShow)
      isAllUiShowed match{
        case true ⇒
          constructSketchInstance()
          (Constructing, mode)
        case false ⇒
          state}
    case (M.UserLoggingUIChanged(isShow), (Creating, mode)) ⇒
      userLoggingUIChanged(isShow)
      isAllUiShowed match{
        case true ⇒
          constructSketchInstance()
          (Constructing, mode)
        case false ⇒
          state}
    case (M.VisualizationUIChanged(isShow), (Creating, mode)) ⇒
      visualizationUIChanged(isShow)
      isAllUiShowed match{
        case true ⇒
          constructSketchInstance()
          (Constructing, mode)
        case false ⇒
          state}
    //Sketch instance built, run plumbing building
    case (M.SketchInstanceReady(instance), (Constructing, mode)) ⇒
      sketchInstanceReady(instance)
      runPlumbingBuilding()
      (Building, mode)
    //Sketch instance error, shutdown plumbing
    case (M.SketchInstanceError(error), (Constructing, mode)) ⇒
      sketchInstanceError(error)
      (Ended, Fail)
    //Plumbing built in Work, start plumbing if auto run or wait for user command
    case (M.PlumbingBuilt, (Building, Work)) ⇒ sketchData.autorun match{
      case true ⇒
        autoStartPlumbing()
        (Starting, Work)
      case false ⇒
        plumbingBuilt()
        (Built, Work)}
    //Plumbing built in Shutdown, terminate sketch
    case (M.PlumbingBuilt, (Building, Shutdown)) ⇒
      terminateSelf(Shutdown)
      (Ended, Shutdown)
    //Plumbing no drives found, switch to Ended
    case (M.PlumbingNoDrivesFound, (Building, mode)) ⇒
      plumbingNoDrivesFound()
      (Ended, mode)
    //If RunBtn hit switch to Starting state
    case (M.SketchUIActionTriggered(RunBtn, _), (Built, Work)) ⇒
      startPlumbing()
      (Starting, Work)
    //If plumbing started in Work, set state Working
    case (M.PlumbingStarted,  (Starting, Work)) ⇒
      plumbingStarted()
      (Working, Work)
    //If plumbing started in Shutdown, stop plumbing
    case (M.PlumbingStarted,  (Starting, Shutdown)) ⇒
      stopPlumbing()
      (Stopping, Shutdown)
    //If StopSketchBtn hit, switch to Stopping
    case (M.SketchUIActionTriggered(StopSketchBtn, _), (Working, Work)) ⇒
      stopPlumbing()
      (Stopping, Work)
    //If plumbing stopped in Work, switch to Ended, wait for shutdown
    case (M.PlumbingStopped, (Stopping, Work)) ⇒
      plumbingStopped()
      (Ended, Work)
    //If plumbing stopped in Shutdown, terminate sketch
    case (M.PlumbingStopped, (Stopping, Shutdown)) ⇒
      terminateSelf(Shutdown)
      (Ended, Shutdown)
    //On hit close start shutdown in Creating, Constructing, Building, Starting or Stopping only update status
    case (M.SketchUIActionTriggered(CloseBtn, _), (Init | Creating | Constructing | Building | Starting | Stopping, Work)) ⇒
      closeHitInNotInterruptState()
      (state._1, Shutdown)
    //On hit close start shutdown in Working, stop sketch
    case (M.SketchUIActionTriggered(CloseBtn, _), (Working, Work)) ⇒
      closeHitInNotInterruptState()
      stopPlumbing()
      (Stopping, Shutdown)
    //On hit close start terminating in Built, Ended or Failed, terminate sketch
    case (M.SketchUIActionTriggered(CloseBtn, _), (Built | Ended, Work | Fail)) ⇒
      terminateSelf(state._2)
      (Ended, state._2)
    //Get sketch context, from objects asks
    case (M.GetSketchContext(actor), _) ⇒
      sketchInstance ! M.BuildSketchContextFor(actor)
      state
    //UI showed/hided
    case (M.SketchUIChanged(isShow), _) ⇒
      sketchUiChanged(isShow)
      state
    case (M.UserLoggingUIChanged(isShow), _) ⇒
      userLoggingUIChanged(isShow)
      state
    case (M.VisualizationUIChanged(isShow), _) ⇒
      visualizationUIChanged(isShow)
      state
    //UI actions
    case (M.SketchUIActionTriggered(LayoutFillBtn, _), (Working, _)) ⇒
      layoutFillBtnHit()
      state
    case (M.SketchUIActionTriggered(LayoutStairsBtn, _), (Working, _)) ⇒
      layoutStairsBtnHit()
      state
    case (M.SketchUIActionTriggered(ShowAllBlocksUiBtn, _), (Working, _)) ⇒
      showAllBlocksUiBtnHit()
      state
    case (M.SketchUIActionTriggered(HideAllBlocksUiBtn, _), (Working, _)) ⇒
      hideAllBlocksUiBtnHit()
      state
    case (M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, _), (Working, _)) ⇒
      skipAllTimeoutTaskBtnHit()
      state
    case (M.SketchUIActionTriggered(LogBtn, act: SketchUiElemState), _) ⇒
      logBtnHit(act)
      state
    case (M.SketchUIActionTriggered(VisualisationBtn, act: SketchUiElemState), _) ⇒
      visualisationBtnHit(act)
      state}
  //Cleanup
  //TODO Очистка ресурсов, (пока вероятно зедсь ничего не будет, но проверить)
  def cleanup(): Unit = {}}
