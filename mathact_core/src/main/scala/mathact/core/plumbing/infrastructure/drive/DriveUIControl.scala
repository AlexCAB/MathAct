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

import mathact.core.bricks.ui.UIEvent
import mathact.core.gui.ui.BlockUILike
import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M

import scala.concurrent.duration.FiniteDuration

/** Handling of UI control
  * Created by CAB on 31.08.2016.
  */

private[core] trait DriveUIControl { _: DriveActor ⇒
  /** Show block UI */
  def showBlockUi(): Unit = {

    ??? //TODO Переписать при разработке UI трайте (выполение в помощью импеллера не нужно, так как всёравно
        //TODO событие ставится в очередь потока UI)


//    pump.block match{
//    case task: BlockUILike ⇒
//      log.debug("[DriveUIControl.showBlockUi] Try to run show UI user function.")
//      impeller ! M.RunTask[Unit](TaskKind.ShowUI, -3, config.uiOperationTimeout, ()⇒{ task.doShowUI() })
//    case _ ⇒
//      log.debug("[DriveUIControl.showBlockUi] Show UI user function not defined, nothing to do.")}
  }
//  /** Show block UI task done
//    * @param execTime - FiniteDuration */
//  def showBlockUiTaskDone(execTime: FiniteDuration): Unit = {
//    log.debug(s"[DriveStartStop.showBlockUiTaskDone] execTime: $execTime.")}
//  /** Show block UI task timeout
//    * @param execTime - FiniteDuration */
//  def showBlockUiTaskTimeout(execTime: FiniteDuration): Unit = {
//    log.warning(s"[DriveStartStop.showBlockUiTaskTimeout]  execTime: $execTime.")
//    userLogging ! M.LogWarning(Some(blockId), pump.blockName, s"Show block UI function timeout on $execTime, keep waiting.")}
//  /** Show block UI task failed
//    * @param execTime - FiniteDuration
//    * @param error - Throwable */
//  def showBlockUiTaskFailed(execTime: FiniteDuration, error: Throwable): Unit = {
//    log.error(s"[DriveStartStop.showBlockUiTaskFailed] execTime: $execTime, error: $error.")
//    userLogging ! M.LogError(Some(blockId), pump.blockName, Seq(error), s"Show block UI function failed on $execTime.")}
  /** Hide block UI */
  def hideBlockUi(): Unit = {

    ??? //TODO Переписать при разработке UI трайте (выполение в помощью импеллера не нужно, так как всёравно
    //TODO событие ставится в очередь потока UI)



//    pump.block match{
//      case task: BlockUILike ⇒
//        log.debug("[DriveUIControl.hideBlockUi] Try to run hide UI user function.")
//        impeller ! M.RunTask[Unit](TaskKind.HideUI, -4, config.uiOperationTimeout, ()⇒{ task.doHideUI() })
//      case _ ⇒
//        log.debug("[DriveUIControl.hideBlockUi] Hide UI user function not defined, nothing to do.")}
  }
//  /** Hide block UI task done
//    * @param execTime - FiniteDuration */
//  def hideBlockUiTaskDone(execTime: FiniteDuration): Unit = {
//    log.debug(s"[DriveStartStop.hideBlockUiTaskDone] execTime: $execTime.")}
//  /** Hide block UI task timeout
//    * @param execTime - FiniteDuration */
//  def hideBlockUiTaskTimeout(execTime: FiniteDuration): Unit = {
//    log.warning(s"[DriveStartStop.hideBlockUiTaskTimeout]  execTime: $execTime.")
//    userLogging ! M.LogWarning(Some(blockId), pump.blockName, s"Hide block UI function timeout on $execTime, keep waiting.")}
//  /** Hide block UI task failed
//    * @param execTime - FiniteDuration
//    * @param error - Throwable */
//  def hideBlockUiTaskFailed(execTime: FiniteDuration, error: Throwable): Unit = {
//    log.error(s"[DriveStartStop.hideBlockUiTaskFailed] execTime: $execTime, error: $error.")
//    userLogging ! M.LogError(Some(blockId),pump.blockName, Seq(error), s"Hide block UI function failed on $execTime.")}


  /** User UI event, send to task to impeller
    * @param event - UIEvent */
  def userUIEvent(event: UIEvent): Unit = {
    log.debug(s"[DriveUIControl.userUIEvent] Build process event task and send to impeller, event: $event")
    //Build task
    val task = pump.block match{
      case blockUI: BlockUILike ⇒
        M.RunTask[Unit](TaskKind.UiEvent, -4, config.uiOperationTimeout, ()⇒{ blockUI.uiEvent(event) })
      case _ ⇒
        throw new IllegalArgumentException(
          "[DriveUIControl.userUIEvent] UIEvent can not be send to block which not implement BlockUILike ")}
    //Send task
    impeller ! task}
  /** Hide block UI task timeout
    * @param execTime - FiniteDuration */
  def blockUiEventTaskTimeout(execTime: FiniteDuration): Unit = {
    log.warning(s"[DriveStartStop.blockUiEventTaskTimeout]  execTime: $execTime.")
    userLogging ! M.LogWarning(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      s"Processing of UI events function timeout on $execTime, keep waiting.")}
  /** Hide block UI task failed
    * @param execTime - FiniteDuration
    * @param error - Throwable */
  def blockUiEventTaskFailed(execTime: FiniteDuration, error: Throwable): Unit = {
    log.error(error, s"[DriveStartStop.blockUiEventTaskFailed] execTime: $execTime.")
    userLogging ! M.LogError(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      Seq(error),
      s"Processing of UI events function failed on $execTime.")}






  /** User log info
    * @param message - String, user text */
  def userLogInfo(message: String): Unit = {
    log.debug("[DriveUIControl.userLogInfo] Re send info message to user log actor.")
    userLogging ! M.LogInfo(Some(blockId), blockName.getOrElse(blockClassName), message)}
  /** User log warn
    * @param message - String, user text */
  def userLogWarn(message: String): Unit = {
    log.debug("[DriveUIControl.userLogWarn]  Re send warn message to user log actor.")
    userLogging ! M.LogWarning(Some(blockId), blockName.getOrElse(blockClassName), message)}
  /** User log error
    * @param error - Option[Throwable], user exception
    * @param message - String, user text */
  def userLogError(error: Option[Throwable], message: String): Unit = {
    log.debug("[DriveUIControl.userLogError] Re send error message to user log actor.")
    userLogging ! M.LogError(Some(blockId), blockName.getOrElse(blockClassName), error.toSeq, message)}




  //TODO Add more

}
