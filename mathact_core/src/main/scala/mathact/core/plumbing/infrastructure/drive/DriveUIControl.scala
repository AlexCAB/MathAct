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

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}


/** Handling of UI control
  * Created by CAB on 31.08.2016.
  */

private[core] trait DriveUIControl { _: DriveActor ⇒
  //Parameters
  val uiOpTimeout = config.uiOperationTimeout
  //Variables
  private var isShowingInProgress = false
  private var isHidingInProgress = false
  private val uiEventTaskQueue = mutable.Queue[M.RunTask[_]]()
  private var currentUiEventTask: Option[M.RunTask[_]] = None
  private var isUIClosed = false
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
  def runNextUiEventTask(): Unit = currentUiEventTask match{
    case None ⇒ uiEventTaskQueue.nonEmpty match{
      case true ⇒
        val taskMsg = uiEventTaskQueue.dequeue()
        log.debug(s"[DriveUIControl.runNextUiEventTask] Run next task: $taskMsg")
        currentUiEventTask = Some(taskMsg)
        impeller ! taskMsg
      case false ⇒
        log.debug(s"[DriveUIControl.runNextUiEventTask] Task queue is empty, nothing to do.")}
    case Some(curTask) ⇒
      log.debug(s"[DriveUIControl.runNextUiEventTask] Do nothing, current not processed $curTask")}
  private def evalSlowdownTimeout(queueSize: Int): Option[Long] = {
    val timeout = queueSize match{
      case 0 ⇒ None
      case n ⇒ Some(n.toLong * config.uiSlowdownCoefficient)}
    log.debug(s"[DriveUIControl.evalSlowdownTimeout] Evaluated timeout: $timeout")
    timeout}
  //Methods
  /** Init block UI */
  def initBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.initBlockUi] Run uiCreate() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiInit, -12, uiOpTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiInit() })}
  /** Block UI initialized, log error if is */
  def blockUiInitialized(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiInitialized] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI initialisation, execution time: $execTime.")}
  /** Create block UI */
  def createBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.createBlockUi] Run uiCreate() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiCreate, -13, uiOpTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiCreate() })}
  /** Block UI created, log error if is */
  def blockUiCreated(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiCreated] Log error if it is, error: $error")
    logErrorIfItIs(error, s"Error on block UI creation, execution time: $execTime.")}
  /** Show block UI */
  def showBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒ isShowingInProgress match{
    case false ⇒
      log.debug(s"[DriveUIControl.showBlockUi] Run uiShow() task for block.")
      isShowingInProgress = true
      impeller ! M.RunTask[Unit](TaskKind.UiShow, -14, uiOpTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiShow() })
    case true ⇒
      log.warning(s"[DriveUIControl.showBlockUi] Last show block UI task not processed yet, this will skipped.")}}
  /** Block UI shown, log error if is */
  def blockUiShown(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiShown] Log error if it is, error: $error")
    isShowingInProgress = false
    logErrorIfItIs(error, s"Error on block UI showing, execution time: $execTime.")}
  /** Hide block UI */
  def hideBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒ isHidingInProgress match{
    case false ⇒
      log.debug(s"[DriveUIControl.hideBlockUi] Run uiHide() task for block.")
      isHidingInProgress = true
      impeller ! M.RunTask[Unit](TaskKind.UiHide, -15, uiOpTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiHide() })
    case true ⇒
      log.warning(s"[DriveUIControl.hideBlockUi] Last hide block UI task not processed yet, this will skipped.")}}
  /** Block UI hidden, log error if is */
  def blockUiHidden(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiHidden] Log error if it is, error: $error")
    isHidingInProgress = false
    logErrorIfItIs(error, s"Error on block UI hidden, execution time: $execTime.")}
  /** Update block UI */
  def updateBlockUiPosition(windowId: Int, x: Double, y: Double): Unit = pump.block match{
    case blockUi: BlockUILike ⇒
      log.debug(
        s"[DriveUIControl.updateBlockUiPosition] Run uiLayout() task for block, windowId: $windowId, x: $x, y: $y")
      val proc = ()⇒{ blockUi.uiLayout(windowId, x, y) }
      impeller ! M.RunTask[Unit](TaskKind.UiLayout, windowId, uiOpTimeout, skipOnTimeout = true, proc)
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
  def blockUiEvent(event: UIEvent): Either[Throwable, Option[Long]] = {
    log.debug(s"[DriveUIControl.blockUiEvent] Build process event task and send to impeller, event: $event")
    //Check if block have UI
    pump.block match{
      case blockUi: BlockUILike ⇒
        //Build task
        val taskMsg = M.RunTask[Unit](
          TaskKind.UiEvent,
          id = -17,
          uiOpTimeout,
          skipOnTimeout = false,
          task = ()⇒{ blockUi.uiEvent(event) })
        //Enqueue new task and try to run
        uiEventTaskQueue.enqueue(taskMsg)
        runNextUiEventTask()
        Right(evalSlowdownTimeout(uiEventTaskQueue.size))
      case _ ⇒
         Left( new IllegalArgumentException(
          "[DriveUIControl.blockUiEvent] UIEvent can not be send to block which not implement BlockUIEvents "))}}
  /** User UI event, successful processed, remove current and start next if exist */
  def blockUiEventTaskDone(execTime: FiniteDuration): Unit = {
    log.debug(
      s"[DriveUIControl.blockUiEventTaskDone] Successful in $execTime, remove current and start next if exist, " +
      s"current task: $currentUiEventTask")
    currentUiEventTask = None
    runNextUiEventTask()}
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
    log.error(
      error,
      s"[DriveStartStop.blockUiEventTaskFailed] execTime: $execTime, currentUiEventTask: $currentUiEventTask.")
    //Log error
    userLogging ! M.LogError(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      Seq(error),
      s"Processing of UI events function failed on $execTime.")
    //Remove current and start next if exist
    currentUiEventTask = None
    runNextUiEventTask()}
  /** Closing of block window on end of work */
  def closeBlockUi(): Boolean = executeIfBlockHaveUi{ blockUi ⇒
    log.debug(s"[DriveUIControl.closeBlockUi] Run uiClose() task for block.")
    impeller ! M.RunTask[Unit](TaskKind.UiClose, -16, uiOpTimeout, skipOnTimeout = true, ()⇒{ blockUi.uiClose() })}
  /** Block UI closed, log error if is */
  def blockUiClosed(error: Option[Throwable], execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveUIControl.blockUiClosed] Log error if it is, error: $error")
    isUIClosed = error.isEmpty
    logErrorIfItIs(error, s"Error on block UI closing, execution time: $execTime.")}
  /** If UI not closed try to close */
  def blockUiCleanup(): Unit = executeIfBlockHaveUi{ blockUi ⇒ isUIClosed match{
    case false ⇒
      log.warning(s"[DriveUIControl.blockUiCleanup] UI not closed. try to close.")
      Future{blockUi.uiClose()}.onComplete{
        case Success(_) ⇒
          log.debug(s"[DriveUIControl.blockUiCleanup] UI successfully closed.")
        case Failure(e) ⇒
          log.error(e, s"[DriveUIControl.blockUiCleanup] UI closing failed.")}
    case true ⇒
      log.debug(s"[DriveUIControl.blockUiCleanup] UI already closed, nothing to do.")}}}
