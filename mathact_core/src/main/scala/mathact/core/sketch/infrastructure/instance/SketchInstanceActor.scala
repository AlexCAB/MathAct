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

package mathact.core.sketch.infrastructure.instance

import java.util.concurrent.{TimeoutException, ExecutionException}

import mathact.core.WorkerBase
import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.data.SketchData
import mathact.core.model.config.SketchInstanceConfigLike
import mathact.core.model.enums.BlockType
import mathact.core.model.holders._
import mathact.core.model.messages.M
import mathact.core.sketch.blocks.WorkbenchLike

import scala.concurrent.Future


/** Sketch instance actor
  * Created by CAB on 17.10.2016.
  */

private[core] class SketchInstanceActor(
  config: SketchInstanceConfigLike,
  sketchData: SketchData,
  controller: SketchControllerRef,
  userLogging: UserLoggingRef,
  plumbing: PlumbingRef,
  layout: LayoutRef)
extends WorkerBase{ import SketchInstance._
  //Variables
  var isBuildingRan = false
  var isSketchContextBuilt = false
  var isBuildingDone = false
  var isBuildingTimeout = false
  var startBuildingTime = 0L
  //Functions
  /** Sketch run building, called after all UI shown */
  def constructSketchInstance(): Unit = isBuildingRan match{
    case false ⇒
      //Run building
      log.debug(
        s"[SketchInstanceActor.sketchRunBuilding] Try to create sketch instance, " +
          s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
      //Run building timeout
      context.system.scheduler.scheduleOnce(
        config.sketchBuildingTimeout,
        self,
        SketchInstanceBuildTimeout)
      //Set is build and start built time
      isBuildingRan = true
      startBuildingTime = System.currentTimeMillis
      //Build sketch
      Future{sketchData.clazz.newInstance()}
        .map{ s ⇒ self ! SketchInstanceBuilt(s.asInstanceOf[WorkbenchLike])}
        .recover{
          case t: ExecutionException ⇒ self ! SketchInstanceBuiltError(t.getCause)
          case t: Throwable ⇒ self ! SketchInstanceBuiltError(t)}
    case true ⇒
      //Already build log error
      log.error(
        s"[SketchInstanceActor.sketchRunBuilding] Sketch instance already build.")}
  /** Get workbench context, create and return of BlockContext
    * @return - Either[Exception, BlockContext] */
  def buildSketchContext(): Either[Exception, BlockContext] = isSketchContextBuilt match{
    case false ⇒
      log.debug(s"[SketchInstanceActor.getSketchContext] Build BlockContext")
      val response = Right{ new BlockContext(
        BlockType.Workbench,
        context.system,
        controller,
        userLogging,
        layout,
        plumbing,
        config.pumpConfig,
        config.commonConfig)}
      isSketchContextBuilt = true
      response
    case true⇒
      val err = new IllegalStateException(s"[SketchInstanceActor.getSketchContext] Context already created.")
      log.error(err, s"[SketchInstanceActor.getSketchContext] Error on creating.")
      Left(err)}
  /** Sketch instance successfully built
    * @param workbench - WorkbenchLike */
  def sketchInstanceBuilt(workbench: WorkbenchLike): Unit = {
    //Set done
    isBuildingDone = true
    //Calc build time
    val time = System.currentTimeMillis - startBuildingTime
    //Check if sketch context built and if no timeout
    (isSketchContextBuilt, isBuildingTimeout) match{
      case (true, false) ⇒
        log.debug(s"[SketchInstanceActor.sketchInstanceBuilt] time: $time, workbench: $workbench.")
        //Log to user logging
        userLogging ! M.LogInfo(None, "SketchInstance", s"Sketch instance successfully built in $time mills.")
        //Report to controller
        controller ! M.SketchInstanceReady(workbench)
      case (false, _) ⇒
        log.error(s"[SketchInstanceActor.sketchInstanceBuilt] Building failed, BlockContext is not built, time: $time.")
        //Log to user logging
        userLogging ! M.LogError(None, "SketchInstance", Seq(), "BlockContext is not built in init of sketch instance.")
        //Send SketchInstanceFail
        controller ! M.SketchInstanceError(new IllegalStateException(
          s"[SketchInstanceActor.sketchInstanceBuilt] BlockContext is not built, time: $time."))
      case (_, true) ⇒
        log.error(s"[SketchInstanceActor.sketchInstanceBuilt] Built after timeout, do nothing, time: $time.")
        //Log to user logging
        userLogging ! M.LogError(
          None,
          "SketchInstance",
          Seq(),
          s"Built after timeout (${config.sketchBuildingTimeout}), building time: $time.")}}
  /** Error during sketch instance building
    * @param error - Throwable */
  def sketchInstanceBuiltError(error: Throwable): Unit = {
    //Set done
    isBuildingDone = true
    //Calc build time
    val time = System.currentTimeMillis - startBuildingTime
    //Log
    log.error(
      error,
      s"[SketchInstanceActor.sketchInstanceBuiltError] Error on creating Sketch extends Workbench instance, " +
      s"time: $time, isBuildingTimeout: $isBuildingTimeout")
    //Build message and log to user logging
    val msg = error match{
      case err: NoSuchMethodException ⇒ s"NoSuchMethodException, check if sketch class is not inner."
      case err ⇒ s"Exception on building of sketch instance, building time: $time mills."}
    userLogging ! M.LogError(None, "SketchInstance", Seq(error), msg)
    //Send SketchInstanceFail if no timeout
    if(! isBuildingTimeout) controller ! M.SketchInstanceError(error)}
  /** Sketch instance not build in required time */
  def sketchInstanceBuiltTimeout(): Unit = isBuildingDone match{
    case false ⇒
      log.error(
        s"[SketchInstanceActor.sketchInstanceBuiltTimeout] Building failed, sketch not built " +
          s"in ${config.sketchBuildingTimeout}.")
      //Set timeout
      isBuildingTimeout = true
      //Log to user logging
      userLogging ! M.LogError(
        None,
        "SketchInstance",
        Seq(),
        s"Timeout, sketch instance not built in ${config.sketchBuildingTimeout}.")
      //Send SketchInstanceFail
      controller ! M.SketchInstanceError(new TimeoutException(
        s"[SketchInstanceActor.sketchInstanceBuiltTimeout] Sketch not built in ${config.sketchBuildingTimeout}"))
    case true ⇒
      log.debug(s"[SketchInstanceActor.sketchInstanceBuiltTimeout] Building done, do nothing.")}
  //Messages handling
  def reaction = {
     //Try to create instance
     case M.CreateSketchInstance ⇒ constructSketchInstance()
     //Build sketch context for given actor
     case M.BuildSketchContextFor(actor) ⇒ actor ! buildSketchContext()
     //Sketch instance built
     case SketchInstanceBuilt(instance) ⇒ sketchInstanceBuilt(instance)
     //Sketch instance built error
     case SketchInstanceBuiltError(error) ⇒ sketchInstanceBuiltError(error)
     //Sketch instance built timeout
     case SketchInstanceBuildTimeout ⇒ sketchInstanceBuiltTimeout()}
  //Cleanup
  def cleanup(): Unit = { }}
