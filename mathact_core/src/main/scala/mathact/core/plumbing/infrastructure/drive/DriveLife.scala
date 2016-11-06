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

import mathact.core.model.data.verification._
import mathact.core.model.data.visualisation._
import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting.life.{OnStopLike, OnStartLike}
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}

import scala.concurrent.duration.Duration


/** Drive actor Life Cycle
  * Created by CAB on 22.08.2016.
  */

private[core] trait DriveLife { _: DriveActor ⇒ import Drive._
  /** Adding of new outlet, called from object
    * Can be called only from blocks constructor on sketch construction.
    * @param pipe - Outlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, inletId] */
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
        s"[DriveLife.addOutletAsk | blockName: $blockClassName, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(blockId),
        blockClassName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on block construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Adding of new inlet, called from object
    * Can be called only from blocks constructor on sketch construction.
    * @param pipe - Inlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, inletId] */
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
        s"[DriveLife.addInletAsk | blockName: $blockClassName, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(blockId),
        blockClassName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on block construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Drive constructed */
  def driveConstructed(): Unit = {
    log.debug(
      s"[DriveLife.driveConstructed] Drive constructed, new pipes and connections will not accepted. " +
      s"Current: outlets: $outlets, inlets: $inlets")
    //Report to plumbing
    plumbing ! M.DriveConstructed
    //Build and send BlockConstructedInfo
    visualization ! M.BlockConstructedInfo(BlockInfo(
      blockId,
      blockName.getOrElse(blockClassName),
      blockImagePath,
      inlets = inlets
        .map{ case (inletId, inletData) ⇒ InletInfo(
          blockId,
          blockName,
          inletId,
          inletName = inletData.name)}
        .toSeq,
      outlets = outlets
        .map{ case (outletId, outletData) ⇒ OutletInfo(
          blockId,
          blockName,
          outletId,
          outletName = outletData.name)}
        .toSeq))}
  /** Build BlockInfo and send to visualization actor */
  def connectingSuccess(): Unit = {
    log.debug(
      s"[DriveLife.connectingSuccess] All pipes connected, send M.DriveConnected, LogInfo, BlockInfo")
    //Report to plumbing
    plumbing ! M.DriveVerification( BlockVerificationData(
      blockId,
      inlets.values.toSeq.map{ inlet ⇒ InletVerificationData(
        inlet.inletId,
        inlet.publishers.values.map(d ⇒ PublisherVerificationData(d.blockId, d.outletId)).toSeq)},
      outlets.values.toSeq.map{ outlet ⇒ OutletVerificationData(
        outlet.outletId,
        outlet.subscribers.values.map(d ⇒ SubscriberVerificationData(d.blockId, d.inletId)).toSeq)}))
    plumbing ! M.DriveConnected
    //Log to user logger
    userLogging ! M.LogInfo(Some(blockId), blockName.getOrElse(blockClassName), s"Block successful built.")}
  /** Rus staring task if defined, return isStarted */
  def doStarting(): Boolean = pump.block match{
    case task: OnStartLike ⇒
      log.debug("[DriveLife.doStarting] Try to run starting user function.")
      impeller ! M.RunTask[Unit](
        TaskKind.Start,
        id = -1,
        config.startFunctionTimeout,
        skipOnTimeout = false,
        task = ()⇒{ task.doStart() })
      false
    case _ ⇒
      log.debug("[DriveLife.doStarting] Starting user function not defined, nothing to do.")
      userLogging ! M.LogInfo(
        Some(blockId),
        blockName.getOrElse(blockClassName),
        s"No starting function, nothing to do.")
      plumbing ! M.DriveStarted
      true}
  /** Starting task done, set of started
    * @param execTime - Duration */
  def startingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLife.startingTaskDone] execTime: $execTime.")
    userLogging ! M.LogInfo(Some(blockId), blockName.getOrElse(blockClassName), s"Block successful started.")
    plumbing ! M.DriveStarted}
  /** Starting task failed, set of started, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def startingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLife.startingTaskTimeout] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      Seq(error),
      s"Starting function failed on $execTime, continue work.")
    plumbing ! M.DriveStarted}
  /** Starting task timeout, log to user console
    * @param execTime - Duration */
  def startingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLife.startingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      s"Starting function timeout on $execTime, keep waiting.")}
  /** Rus stopping task if defined, return isStopped */
  def doStopping(): Boolean = pump.block match{
    case task: OnStopLike ⇒
      log.debug("[DriveLife.doStopping] Try to run stopping user function.")
      impeller ! M.RunTask[Unit](
        TaskKind.Stop,
        id = -2,
        config.stopFunctionTimeout,
        skipOnTimeout = false,
        task = ()⇒{ task.doStop() })
      false
    case _ ⇒
      log.debug("[DriveLife.doStopping] Stopping user function not defined, nothing to do.")
      userLogging ! M.LogInfo(
        Some(blockId),
        blockName.getOrElse(blockClassName),
        s"No stopping function, nothing to do.")
      true}
  /** Stopping task done, set of stopped
    * @param execTime - Duration */
  def stoppingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLife.stoppingTaskDone] execTime: $execTime.")
    userLogging ! M.LogInfo(Some(blockId), blockName.getOrElse(blockClassName), s"Block successful stopped.")}
  /** Stopping task failed, set of stopped, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def stoppingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLife.stoppingTaskFailed] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      Seq(error),
      s"Stopping function failed on $execTime.")}
  /** Stopping task timeout, log to user console
    * @param execTime - Duration */
  def stoppingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLife.stoppingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      s"Stopping function timeout on $execTime, keep waiting.")}
  /** Drive stopped */
  def driveStopped(): Unit = {
    log.debug("[DriveLife.driveStopped] Send DriveStopped to plumbing.")
    plumbing ! M.DriveStopped}
  /** Drive turned off */
  def driveTurnedOff(): Unit = {
    log.debug(s"[DriveLife.driveTurnedOff] Send DriveTurnedOff")
    plumbing ! M.DriveTurnedOff}}
