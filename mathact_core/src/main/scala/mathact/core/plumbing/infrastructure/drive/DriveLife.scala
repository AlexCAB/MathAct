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

package mathact.core.plumbing.infrastructure.drive

import mathact.core.bricks.{OnStart, OnStop}
import mathact.core.model.data.visualisation._
import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.concurrent.duration.Duration


/** Drive actor Life Cycle
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveLife { _: DriveActor ⇒ import Drive._
  /** Adding of new outlet, called from object
    * Can be called only from blocks constructor on sketch construction.
    * @param pipe - Outlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addOutletAsk(pipe: OutPipe[_], name: Option[String], state: Drive.State): Either[Throwable,(Int,Int)] = state match {
    case Drive.State.Init ⇒
      //Check of already added
      outlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextIntId
          outlets += (id → OutletState(id, name, pipe))
          log.debug(s"[DriveLife.addOutletAsk] Outlet: $pipe, added with ID: $id")
          Right((blockId, id))
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveLife.addOutletAsk] Outlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg =
        s"[DriveLife.addOutletAsk | blockName: ${pump.blockName}, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(blockId),
        pump.blockName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on block construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Adding of new inlet, called from object
    * Can be called only from blocks constructor on sketch construction.
    * @param pipe - Inlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addInletAsk(pipe: InPipe[_], name: Option[String], state: Drive.State): Either[Throwable,(Int,Int)] = state match {
    case Drive.State.Init ⇒
      //Check if pipe already added
      inlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextIntId
          inlets += (id → InletState(id, name, pipe))
          log.debug(s"[DriveLife.addInletAsk] Inlet: $pipe, added with ID: $id")
          Right((blockId, id))
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveLife.addInletAsk] Inlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg =
        s"[DriveLife.addInletAsk | blockName: ${pump.blockName}, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(blockId),
        pump.blockName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on block construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Drive constructed */
  def constructDrive(): Unit = {
    log.debug(
      s"[DriveLife.constructDrive] Drive constructed, new pipes and connections will not accepted. " +
      s"Current: outlets: $outlets, inlets: $inlets")
    plumbing ! M.DriveConstructed}
  /** Build BlockBuiltInfo and send to visualization actor */
  def connectingSuccess(): Unit = {
    log.debug(
      s"[DriveLife.connectingSuccess] All pipes connected, send M.DriveConnected, LogInfo, BlockBuiltInfo")
    //Report to plumbing
    plumbing ! M.DriveConnected
    //Log to user logger
    userLogging ! M.LogInfo(Some(blockId), pump.blockName, s"Block successful built.")
    //Build
    val builtInfo = BlockBuiltInfo(
      blockId,
      pump.blockName,
      pump.blockImagePath,
      inlets = inlets
        .map{ case (inletId, inletData) ⇒ (inletId, InletConnectionsInfo(
          blockId,
          inletId,
          inletName = inletData.name,
          publishers = inletData.publishers
            .map{ case (_, pubData) ⇒ PublisherInfo(pubData.blockId,pubData.pipeId)}
            .toList))}
        .toMap,
      outlets = outlets
        .map{ case (outletId, outletData) ⇒ (outletId, OutletConnectionsInfo(
          blockId,
          outletId,
          outletName = outletData.name,
          subscribers = outletData.subscribers
            .map{ case (_, subData) ⇒ SubscriberInfo(subData.inlet.blockId, subData.inlet.pipeId)}
            .toList))}
        .toMap)
    //Send
    visualization ! M.BlockBuilt(builtInfo)}
  /** Rus staring task if defined, return isStarted */
  def doStarting(): Boolean = pump.block match{
    case task: OnStart ⇒
      log.debug("[DriveLife.doStarting] Try to run starting user function.")
      impeller ! M.RunTask[Unit](TaskKind.Start, -1, config.startFunctionTimeout, ()⇒{ task.doStart() })
      false
    case _ ⇒
      log.debug("[DriveLife.doStarting] Starting user function not defined, nothing to do.")
      userLogging ! M.LogInfo(Some(blockId), pump.blockName, s"No starting function, nothing to do.")
      plumbing ! M.DriveStarted
      true}
  /** Starting task done, set of started
    * @param execTime - Duration */
  def startingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLife.startingTaskDone] execTime: $execTime.")
    userLogging ! M.LogInfo(Some(blockId), pump.blockName, s"Block successful started.")
    plumbing ! M.DriveStarted}
  /** Starting task failed, set of started, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def startingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLife.startingTaskTimeout] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(
      Some(blockId),
      pump.blockName,
      Seq(error),
      s"Starting function failed on $execTime, continue work.")
    plumbing ! M.DriveStarted}
  /** Starting task timeout, log to user console
    * @param execTime - Duration */
  def startingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLife.startingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(blockId), pump.blockName, s"Starting function timeout on $execTime, keep waiting.")}
  /** Rus stopping task if defined, return isStopped */
  def doStopping(): Boolean = pump.block match{
    case task: OnStop ⇒
      log.debug("[DriveLife.doStopping] Try to run stopping user function.")
      impeller ! M.RunTask[Unit](TaskKind.Stop, -1, config.stopFunctionTimeout, ()⇒{ task.doStop() })
      false
    case _ ⇒
      log.debug("[DriveLife.doStopping] Stopping user function not defined, nothing to do.")
      userLogging ! M.LogInfo(Some(blockId), pump.blockName, s"No stopping function, nothing to do.")
      plumbing ! M.DriveStopped
      true}
  /** Stopping task done, set of stopped
    * @param execTime - Duration */
  def stoppingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLife.stoppingTaskDone] execTime: $execTime.")
    userLogging ! M.LogInfo(Some(blockId), pump.blockName, s"Block successful stopped.")
    plumbing ! M.DriveStopped}
  /** Stopping task failed, set of stopped, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def stoppingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLife.stoppingTaskFailed] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(Some(blockId), pump.blockName, Seq(error), s"Stopping function failed on $execTime.")
    plumbing ! M.DriveStopped}
  /** Stopping task timeout, log to user console
    * @param execTime - Duration */
  def stoppingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLife.stoppingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(blockId), pump.blockName, s"Stopping function timeout on $execTime, keep waiting.")}
  def driveTurnedOff(): Unit = {
    log.debug(s"[DriveLife.driveTurnedOff] Send DriveTurnedOff")
    plumbing ! M.DriveTurnedOff}}
