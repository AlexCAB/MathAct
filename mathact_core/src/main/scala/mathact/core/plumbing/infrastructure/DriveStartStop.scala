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

package mathact.core.plumbing.infrastructure

import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M
import mathact.core.bricks.{OnStop, OnStart}

import scala.concurrent.duration.Duration


/** Handling of staring and stopping
  * Created by CAB on 26.08.2016.
  */

private [mathact] trait DriveStartStop { _: Drive ⇒
  //Variables
  private var started = false
  private var stopped = false
  //Methods
  /** Rus staring task if defined */
  def doStarting(): Unit = pump.tool match{
    case task: OnStart ⇒
      log.debug("[DriveStartStop.doStarting] Try to run starting user function.")
      impeller ! M.RunTask[Unit](TaskKind.Start, -1, config.startFunctionTimeout, ()⇒{ task.doStart() })
    case _ ⇒
      log.debug("[DriveStartStop.doStarting] Starting user function not defined, nothing to do.")
      started = true}
  /** Starting task done, set of started
    * @param execTime - Duration */
  def startingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveStartStop.startingTaskDone] execTime: $execTime.")
    started = true}
  /** Starting task timeout, log to user console
    * @param execTime - Duration */
  def startingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveStartStop.startingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Starting function timeout on $execTime, keep waiting.")}
  /** Starting task failed, set of started, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def startingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveStartStop.startingTaskTimeout] execTime: $execTime, error: $error.")
    started = true
    userLogging ! M.LogError(Some(toolId), pump.toolName, Some(error), s"Starting function failed on $execTime.")}
  /** Rus stopping task if defined */
  def doStopping(): Unit = pump.tool match{
    case task: OnStop ⇒
      log.debug("[DriveStartStop.doStopping] Try to run stopping user function.")
      impeller ! M.RunTask[Unit](TaskKind.Stop, -1, config.stopFunctionTimeout, ()⇒{ task.doStop() })
    case _ ⇒
      log.debug("[DriveStartStop.doStopping] Stopping user function not defined, nothing to do.")
      stopped = true}
  /** Stopping task done, set of stopped
    * @param execTime - Duration */
  def stoppingTaskDone(execTime: Duration): Unit = {
    log.debug(s"[DriveStartStop.stoppingTaskDone] execTime: $execTime.")
    stopped = true}
  /** Stopping task timeout, log to user console
    * @param execTime - Duration */
  def stoppingTaskTimeout(execTime: Duration): Unit = {
    log.warning(s"[DriveStartStop.stoppingTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Stopping function timeout on $execTime, keep waiting.")}
  /** Stopping task failed, set of stopped, log to user console
    * @param execTime - Duration
    * @param error - Throwable */
  def stoppingTaskFailed(execTime: Duration, error: Throwable): Unit = {
    log.error(s"[DriveStartStop.stoppingTaskFailed] execTime: $execTime, error: $error.")
    stopped = true
    userLogging ! M.LogError(Some(toolId), pump.toolName, Some(error), s"Stopping function failed on $execTime.")}
  /** Check if starting user function is executed
    * @return - true if started */
  def isStarted: Boolean = started
  /** Check if stopping user function is executed
    * @return - true if stopped*/
  def isStopped: Boolean = started}
