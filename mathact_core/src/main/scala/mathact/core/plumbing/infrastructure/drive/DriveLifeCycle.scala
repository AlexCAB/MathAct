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

private [mathact] trait DriveLifeCycle { _: DriveActor ⇒ import Drive._
  //Variables
  private var started = false
  private var stopped = false
  //Methods
  /** Adding of new outlet, called from object
    * Can be called only from tools constructor on sketch construction.
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
          log.debug(s"[DriveLifeCycle.addOutletAsk] Outlet: $pipe, added with ID: $id")
          Right((toolId, id))
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveLifeCycle.addOutletAsk] Outlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg =
        s"[DriveLifeCycle.addOutletAsk | toolName: ${pump.toolName}, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(toolId),
        pump.toolName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on tool construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Adding of new inlet, called from object
    * Can be called only from tools constructor on sketch construction.
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
          log.debug(s"[DriveLifeCycle.addInletAsk] Inlet: $pipe, added with ID: $id")
          Right((toolId, id))
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveLifeCycle.addInletAsk] Inlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg =
        s"[DriveLifeCycle.addInletAsk | toolName: ${pump.toolName}, pipe: $pipe, name: $name] " +
        s"Incorrect state $s, required DriveInit."
      log.error(msg)
      //User logging
      userLogging ! M.LogWarning(
        Some(toolId),
        pump.toolName,
        s"Pipe $pipe with name: ${name.getOrElse("---")} can be created on tool construction, current state $s")
      //Return exception
      Left(new IllegalStateException(msg))}
  /** Drive constructed */
  def constructDrive(): Unit = {
    log.debug(
      s"[DriveLifeCycle.constructDrive] Drive constructed, new pipes and connections will not accepted. " +
      s"Current: outlets: $outlets, inlets: $inlets, pendingConnections: $pendingConnections")
    plumbing ! M.DriveConstructed}
  /** Build ToolBuiltInfo and send to visualization actor */
  def buildingSuccess(): Unit = {
    log.debug(
      s"[DriveLifeCycle.postHandling @ Building] All pipes connected, send M.DriveBuilt, and switch to Working mode.")
    //Report to plumbing
    plumbing ! M.DriveBuilt
    //Log to user logger
    userLogging ! M.LogInfo(Some(toolId), pump.toolName, s"Tool successful built.")
    //Build
    val builtInfo = ToolBuiltInfo(
      toolId,
      pump.toolName,
      pump.toolImagePath,
      inlets = inlets
        .map{ case (inletId, inletData) ⇒ (inletId, InletConnectionsInfo(
          toolId,
          inletId,
          inletName = inletData.name,
          publishers = inletData.publishers
            .map{ case (_, pubData) ⇒ PublisherInfo(pubData.toolId,pubData.pipeId)}
            .toList))}
        .toMap,
      outlets = outlets
        .map{ case (outletId, outletData) ⇒ (outletId, OutletConnectionsInfo(
          toolId,
          outletId,
          outletName = outletData.name,
          subscribers = outletData.subscribers
            .map{ case (_, subData) ⇒ SubscriberInfo(subData.inlet.toolId, subData.inlet.pipeId)}
            .toList))}
        .toMap)
    //Send
    visualization ! M.ToolBuilt(builtInfo)}
  /** Rus staring task if defined */
  def doStarting(): Unit = pump.tool match{
    case task: OnStart ⇒
      log.debug("[DriveLifeCycle.doStarting] Try to run starting user function.")
      impeller ! M.RunTask[Unit](TaskKind.Start, -1, config.startFunctionTimeout, ()⇒{ task.doStart() })
    case _ ⇒
      log.debug("[DriveLifeCycle.doStarting] Starting user function not defined, nothing to do.")
      started = true}
  /** Starting task done, set of started
    * @param execTime - Duration */
  def startingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLifeCycle.startingTaskDone] execTime: $execTime.")
    started = true}
  /** Starting task timeout, log to user console
    * @param execTime - Duration */
  def startingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLifeCycle.startingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Starting function timeout on $execTime, keep waiting.")}
  /** Starting task failed, set of started, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def startingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLifeCycle.startingTaskTimeout] execTime: $execTime, error: $error.")
    started = true
    userLogging ! M.LogError(Some(toolId), pump.toolName, Seq(error), s"Starting function failed on $execTime.")}
  /** Starting success, logging to user log, report to plumbing */
  def startingSuccess(): Unit = {
    log.debug("[DriveLifeCycle.startingSuccess] Drive successful started.")
    userLogging ! M.LogInfo(Some(toolId), pump.toolName, s"Tool successful started.")
    plumbing ! M.DriveStarted}
  /** Check if starting user function is executed
    * @return - true if started */
  def isStarted: Boolean = started
  /** Rus stopping task if defined */
  def doStopping(): Unit = pump.tool match{
    case task: OnStop ⇒
      log.debug("[DriveLifeCycle.doStopping] Try to run stopping user function.")
      impeller ! M.RunTask[Unit](TaskKind.Stop, -1, config.stopFunctionTimeout, ()⇒{ task.doStop() })
    case _ ⇒
      log.debug("[DriveLifeCycle.doStopping] Stopping user function not defined, nothing to do.")
      stopped = true}
  /** Stopping task done, set of stopped
    * @param execTime - Duration */
  def stoppingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveLifeCycle.stoppingTaskDone] execTime: $execTime.")
    stopped = true}
  /** Stopping task timeout, log to user console
    * @param execTime - Duration */
  def stoppingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveLifeCycle.stoppingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Stopping function timeout on $execTime, keep waiting.")}
  /** Stopping task failed, set of stopped, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def stoppingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveLifeCycle.stoppingTaskFailed] execTime: $execTime, error: $error.")
    stopped = true
    userLogging ! M.LogError(Some(toolId), pump.toolName, Seq(error), s"Stopping function failed on $execTime.")}
  /** Stopping success, logging to user log, report to plumbing */
  def stoppingSuccess(): Unit = {
    log.debug("[DriveLifeCycle.stoppingSuccess] Drive successful stopped.")
    userLogging ! M.LogInfo(Some(toolId), pump.toolName, s"Tool successful stopped.")
    plumbing ! M.DriveStopped}
  /** Check if stopping user function is executed
    * @return - true if stopped*/
  def isStopped: Boolean = stopped
  /** Terminating of this drive, currently here only logging */
  def doTerminating(): Unit = {
    log.debug(s"[DriveLifeCycle.doTerminating] Start of terminating of drive.")}
  /** Drive error, log to user logging and report to plumbing
    * Sends M.DriveBuildingError to plumping, and terminate self */
  def driveError(message: String, error: Option[Throwable]): Unit = {
    log.error(s"[DriveLifeCycle.driveError] Log to user logging and report to plumbing, error: $error")
    //Log to user logger
    userLogging ! M.LogError(Some(toolId), pump.toolName, error.toSeq, message)
    //Report to plumbing
    plumbing ! M.DriveError(error.getOrElse(new Exception("[DriveLifeCycle.driveError] " + message)))}}