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

//import mathact.core.bricks.UIControl
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
//    case task: UIControl ⇒
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
//      case task: UIControl ⇒
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

  //TODO Add more

}
