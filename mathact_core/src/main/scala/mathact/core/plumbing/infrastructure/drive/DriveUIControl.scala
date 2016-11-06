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


  //TODO 1.Добавить промуск соомжений показать/скрыть если сейчас это выполнятеся.
  //TODO 2.Добавить очередь чообщений от UI и алгоритм оратного давления для них.



  //Parameters
  private val opTimeout = config.uiOperationTimeout
  //Functions
  private def executeIfBlockHaveUi(proc: BlockUILike⇒Unit): Boolean = pump.block match{ //Return: isBlockHaveUi
    case blockUi: BlockUILike ⇒
      log.debug(s"[DriveUIControl.executeIfBlockHaveUi] Try to call proc.")
      proc(blockUi)
      true
    case _ ⇒
      log.debug(s"[DriveUIControl.executeIfBlockHaveUi] Block have no UI, nothing to do.")
      false}
  private def logErrorIfItIs(opError: Option[Throwable], message: String): Unit = opError.foreach{ error ⇒
    log.error(error, s"[DriveUIControl.logErrorIfItIs] Error on some in UI control methods.")
    userLogging ! M.LogError(Some(blockId), blockName.getOrElse(blockClassName), Seq(error), message)}
  //Methods
  /** Init block UI */
  def initBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.initBlockUi] Run uiCreate() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiInit, -12, opTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiInit() })}
  /** Block UI initialized, log error if is */
  def blockUiInitialized(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiInitialized] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI initialisation, execution time: $execTime.")}
  /** Create block UI */
  def createBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.createBlockUi] Run uiCreate() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiCreate, -13, opTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiCreate() })}
  /** Block UI created, log error if is */
  def blockUiCreated(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiCreated] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI creation, execution time: $execTime.")}
  /** Show block UI */
  def showBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.showBlockUi] Run uiShow() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiShow, -14, opTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiShow() })}
  /** Block UI shown, log error if is */
  def blockUiShown(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiShown] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI showing, execution time: $execTime.")}
  /** Hide block UI */
  def hideBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.showBlockUi] Run uiHide() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiHide, -15, opTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiHide() })}
  /** Block UI hidden, log error if is */
  def blockUiHidden(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiHidden] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI hidden, execution time: $execTime.")}
  /** Update block UI */
  def updateBlockUiPosition(windowId: Int, x: Double, y: Double): Unit = pump.block match{
    case blockUi: BlockUILike ⇒
      log.debug(
        s"[DriveUIControl.updateBlockUiPosition] Run uiLayout() task for block, windowId: $windowId, x: $x, y: $y")
      val proc = ()⇒{ blockUi.uiLayout(windowId, x, y) }
      impeller ! M.RunTask[Unit](TaskKind.UiLayout, windowId, opTimeout, skipOnTimeout = true, proc)
    case _ ⇒
      throw new IllegalArgumentException(
        s"[DriveUIControl.updateBlockUiPosition] uiLayout() can not be called on block which " +
          s"not implement BlockUIControl. Id: $windowId, x: $x, y: $y")}
  /** Block UI hidden, log error if is, send WindowPositionUpdated*/
  def blockUiPositionUpdate(windowId: Int, error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(
      s"[DriveUIControl.blockUiPositionUpdate] Send WindowPositionUpdated and log error if it is, error: $error")
    layout ! M.WindowPositionUpdated(windowId)
    logErrorIfItIs(error, s"Error on block UI hidden, execution time: $execTime.")}
  /** User UI event, send to task to impeller
    * @param event - UIEvent */
  def blockUiEvent(event: UIEvent): Unit = {
    log.debug(s"[DriveUIControl.blockUiEvent] Build process event task and send to impeller, event: $event")
    //Build task
    val task = pump.block match{
      case blockUi: BlockUILike ⇒
        M.RunTask[Unit](TaskKind.UiEvent, -17, opTimeout, skipOnTimeout = false, ()⇒{ blockUi.uiEvent(event) })
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
      s"Processing of UI events function failed on $execTime.")}
  /** Closing of block window on end of work */
  def closeBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.closeBlockUi] Run uiClose() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiClose, -16, opTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiClose() })}
  /** Block UI closed, log error if is */
  def blockUiClosed(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiClosed] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI closing, execution time: $execTime.")}}
