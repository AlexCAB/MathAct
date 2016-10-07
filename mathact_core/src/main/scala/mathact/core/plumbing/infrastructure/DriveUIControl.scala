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
import mathact.core.bricks.UIControl

import scala.concurrent.duration.FiniteDuration

/** Handling of UI control
  * Created by CAB on 31.08.2016.
  */

private [mathact] trait DriveUIControl { _: Drive ⇒
  /** Show tool UI */
  def showToolUi(): Unit = pump.tool match{
    case task: UIControl ⇒
      log.debug("[DriveUIControl.showToolUi] Try to run show UI user function.")
      impeller ! M.RunTask[Unit](TaskKind.ShowUI, -3, config.uiOperationTimeout, ()⇒{ task.doShowUI() })
    case _ ⇒
      log.debug("[DriveUIControl.showToolUi] Show UI user function not defined, nothing to do.")}
  /** Show tool UI task done
    * @param execTime - FiniteDuration */
  def showToolUiTaskDone(execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveStartStop.showToolUiTaskDone] execTime: $execTime.")}
  /** Show tool UI task timeout
    * @param execTime - FiniteDuration */
  def showToolUiTaskTimeout(execTime: FiniteDuration): Unit = {
    log.warning(s"[DriveStartStop.showToolUiTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Show tool UI function timeout on $execTime, keep waiting.")}
  /** Show tool UI task failed
    * @param execTime - FiniteDuration
    * @param error - Throwable */
  def showToolUiTaskFailed(execTime: FiniteDuration, error: Throwable): Unit = {
    log.error(s"[DriveStartStop.showToolUiTaskFailed] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(Some(toolId), pump.toolName, Some(error), s"Show tool UI function failed on $execTime.")}
  /** Hide tool UI */
  def hideToolUi(): Unit = pump.tool match{
    case task: UIControl ⇒
      log.debug("[DriveUIControl.hideToolUi] Try to run hide UI user function.")
      impeller ! M.RunTask[Unit](TaskKind.HideUI, -4, config.uiOperationTimeout, ()⇒{ task.doHideUI() })
    case _ ⇒
      log.debug("[DriveUIControl.hideToolUi] Hide UI user function not defined, nothing to do.")}
  /** Hide tool UI task done
    * @param execTime - FiniteDuration */
  def hideToolUiTaskDone(execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveStartStop.hideToolUiTaskDone] execTime: $execTime.")}
  /** Hide tool UI task timeout
    * @param execTime - FiniteDuration */
  def hideToolUiTaskTimeout(execTime: FiniteDuration): Unit = {
    log.warning(s"[DriveStartStop.hideToolUiTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(Some(toolId), pump.toolName, s"Hide tool UI function timeout on $execTime, keep waiting.")}
  /** Hide tool UI task failed
    * @param execTime - FiniteDuration
    * @param error - Throwable */
  def hideToolUiTaskFailed(execTime: FiniteDuration, error: Throwable): Unit = {
    log.error(s"[DriveStartStop.hideToolUiTaskFailed] execTime: $execTime, error: $error.")
    userLogging ! M.LogError(Some(toolId),pump.toolName, Some(error), s"Hide tool UI function failed on $execTime.")}

  //TODO Add more

}
