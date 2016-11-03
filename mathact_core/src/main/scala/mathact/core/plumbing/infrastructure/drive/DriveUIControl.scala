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
import mathact.core.gui.ui.{BlockUIEvents, BlockUIControl}
import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M

import scala.concurrent.duration.FiniteDuration


/** Handling of UI control
  * Created by CAB on 31.08.2016.
  */

private[core] trait DriveUIControl { _: DriveActor ⇒
  //Functions
  private def executeIfBlockHaveUi(proc: BlockUIControl⇒Unit): Unit = pump.block match{
    case blockUi: BlockUIControl ⇒
      log.debug(s"[DriveUIControl.executeIfBlockHaveUi] Try to call proc.")
      proc(blockUi)
    case _ ⇒
      log.debug(s"[DriveUIControl.executeIfBlockHaveUi] Block have no UI, nothing to do.")}
  //Methods
  /** Create block UI */
  def updateBlockUiPosition(id: Int, x: Double, y: Double): Unit = pump.block match{
    case blockUi: BlockUIControl ⇒
      log.debug(s"[DriveUIControl.updateBlockUiPosition] Call uiLayout() of block, id: $id, x: $x, y: $y")
      blockUi.uiLayout(id, x, y)
    case _ ⇒
      throw new IllegalArgumentException(
        s"[DriveUIControl.updateBlockUiPosition] uiLayout() can not be called on block which " +
        s"not implement BlockUIControl. Id: $id, x: $x, y: $y")}
  /** Create block UI */
  def initBlockUi(): Unit = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.initBlockUi] Call uiCreate() of block.")
    blockUi.uiInit()}
  /** Create block UI */
  def createBlockUi(): Unit = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.createBlockUi] Call uiCreate() of block.")
    blockUi.uiCreate()}
  /** Show block UI */
  def showBlockUi(): Unit = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.showBlockUi] Call uiShow() of block.")
    blockUi.uiShow()}
  /** Hide block UI */
  def hideBlockUi(): Unit = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.showBlockUi] Call uiHide() of block.")
    blockUi.uiHide()}
  /** Closing of block window on end of work */
  def closeBlockUi(): Unit = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.closeBlockUi] Call uiClose() of block.")
    blockUi.uiClose()}
  /** User UI event, send to task to impeller
    * @param event - UIEvent */
  def blockUiEvent(event: UIEvent): Unit = {
    log.debug(s"[DriveUIControl.blockUiEvent] Build process event task and send to impeller, event: $event")
    //Build task
    val task = pump.block match{
      case blockUi: BlockUIEvents ⇒
        M.RunTask[Unit](TaskKind.UiEvent, -4, config.uiOperationTimeout, ()⇒{ blockUi.uiEvent(event) })
      case _ ⇒
        throw new IllegalArgumentException(
          "[DriveUIControl.blockUiEvent] UIEvent can not be send to block which not implement BlockUIEvents ")}
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
      s"Processing of UI events function failed on $execTime.")}}
