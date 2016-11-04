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

import mathact.core.model.enums._
import mathact.core.model.holders.DriveRef
import mathact.core.model.messages.M
import scala.collection.mutable.{ListBuffer ⇒ MutList}

import scala.concurrent.duration.Duration


/** Handling of messages
  * Created by CAB on 22.08.2016.
  */

//TODO Для уменьшения количества отправленых DriveLoad, нужно использовать что то вроде ПИ (в calcPushTimeout)
//TODO регулятора с мёртвой зоной (чтобы DriveLoad рассылалась не по каждому измению размера очереди).
//TODO Сейчас отправка DriveLoad на каждое измение размера очерели инлета.
private[core] trait DriveMessaging { _: DriveActor ⇒ import Drive._
  //Variables
  private val pendingUserMessages = MutList[(Int, Any)]()   //(inletId, value)
  //Functions
  private def calcPushTimeout(queueSize: Int): Option[Long] = {
    queueSize match{
      case mqs if mqs > 0 ⇒ Some(mqs * config.pushTimeoutCoefficient)
      case _ ⇒ None}}
  def sendUserMessage(outlet: OutletState, value: Any): Unit = {
    log.debug(
      s"[DriveMessaging.sendUserMessage] Data: $value, will send to outlet: $outlet")
    outlet.subscribers.values.foreach{ subscriber ⇒
      subscriber.blockDrive ! M.UserMessage(outlet.outletId, subscriber.inletId, value)}}
  private def runForInlet(inletId: Int)(proc: InletState⇒Unit): Unit = {
    //Check data
    assume(
      inlets.contains(inletId),
      s"[DriveMessaging.runForInlet] inlets not contain inletId: $inletId, inlets: $inlets")
    //Run proc
    proc(inlets(inletId))}
  private def buildTask(inlet: InletState, value: Any): M.RunTask[Unit] = M.RunTask(
    kind = TaskKind.Massage,
    id = inlet.inletId,
    timeout = config.messageProcessingTimeout,
    task = ()⇒{inlet.pipe.processValue(value)})
  private def enqueueMessageTask(inlet: InletState, value: Any): Unit = {
    val newRunTask = buildTask(inlet, value)
    inlet.taskQueue.enqueue(newRunTask)
    log.debug(s"[DriveMessaging.enqueueMessageTask] Task added to the queue, task: $newRunTask, queue: ${inlet.taskQueue}")}
  private def runMsgTask(inlet: InletState, task: M.RunTask[_]): Unit = {
    inlet.currentTask = Some(task)
    impeller ! task
    log.debug(s"[DriveMessaging.runMsgTask] Task runs: $task, from inlet: $inlet")}
  private def runNextMsgTask(): Option[InletState] = { //Return: inlet for which tusk runs (to call sendLoadMessage)
    //Search for inlet with max queue size
    val maxQueueInlet = inlets.values match{
      case ins if ins.isEmpty ⇒ None
      case ins ⇒ ins.maxBy(_.taskQueue.size) match{
        case msi if msi.taskQueue.isEmpty ⇒ None
        case msi ⇒ Some(msi)}}
    //Run task
    maxQueueInlet match{
      case Some(inlet) ⇒
        runMsgTask(inlet, inlet.taskQueue.dequeue())
        Some(inlet)
      case None ⇒
       log.debug(s"[DriveMessaging.runNextMsgTask] No more tasks to run.")
       None}}
  private def enqueueOrRunMessageTask(inlet: InletState, value: Any): Option[InletState] = { //Return: inlet for which queue changed (to call sendLoadMessage)
    //Check if not run already
    inlets.values.exists(_.currentTask.nonEmpty) match{
      case false ⇒
        val task = buildTask(inlet, value)
        log.debug(s"[DriveMessaging.enqueueOrRunMessageTask] No other task runs, run this with no enqueue, task: $task")
        runMsgTask(inlet, task)
        None   //Queue not changed,  no need to call sendLoadMessage
      case true ⇒
        log.debug(s"[DriveMessaging.enqueueOrRunMessageTask] Some other task runs, add this to queue.")
        enqueueMessageTask(inlet, value)
        Some(inlet)}}
  private def runMessageTaskLoop(): Option[InletState] = { //Return: inlet for which tusk runs (to call sendLoadMessage)
    //Check if not run already
    inlets.values.exists(_.currentTask.nonEmpty) match{
      case false ⇒
        log.debug(s"[DriveMessaging.runMessageTaskLoop] Run for first message.")
        runNextMsgTask()
      case true ⇒
        log.debug(s"[DriveMessaging.runMessageTaskLoop] Message task loop already runs.")
        None}}
  private def cleanCurrentTask(inlet: InletState): Unit = {
    log.debug(s"[DriveMessaging.cleanCurrentTask] Executed task: ${inlet.currentTask}.")
    //Check currentTask
    assume(inlet.currentTask.nonEmpty, s"[DriveMessaging.cleanCurrentTask] currentTask is empty, inlet: $inlet.")
    //Clean
    inlet.currentTask = None}
  private def sendLoadMessage(inlet: InletState): Unit = {
    //Send load messages
    inlet.publishers.values.foreach{ publisher ⇒
      val load = inlet.taskQueue.size
      log.debug(s"[DriveMessaging.sendLoadMessage] Send DriveLoad($load), to publisher: $publisher.")
      publisher.blockDrive ! M.DriveLoad((DriveRef(self), inlet.inletId), publisher.outletId, load)}}
  //Methods
  /** User message from self outlet, send to all outlet subscribers
    * @param outletId - Int, source ID
    * @param value -  Any, user message */
  def userDataAsk(outletId: Int, value: Any, state: Drive.State): Either[Throwable, Option[Long]] = state match{
      case State.Init | State.Constructed | State.Connecting | State.Connected ⇒
        //Put message to pending list
        pendingUserMessages += (outletId → value)
        //Eval push timeout
        val pushTimeout = calcPushTimeout(pendingUserMessages.size)
        //Log
        log.debug(
          s"[DriveMessaging.userDataAsk] In state: $state data: $value, for inletId: $outletId " +
          s"was put in pending list: $pendingUserMessages, pushTimeout: $pushTimeout")
        //Return
        Right(pushTimeout)
      case State.TurnedOn |State.Starting | State.Working | State.Stopping | State.Stopped ⇒
        //Get of outlet
        outlets.get(outletId) match{
          case Some(outlet) ⇒
            //Distribution of UserMessage
            sendUserMessage(outlet, value)
            //Push timeout
            log.debug(
              s"[DriveMessaging.userDataAsk] Data: $value, sent from inletId: $outletId to " +
              s"all subscribers ${outlet.subscribers} , pushTimeout: ${outlet.pushTimeout}")
            //Return pour timeout
            Right(outlet.pushTimeout)
          case None ⇒
            //Incorrect inletId
            val msg = s"[DriveMessaging.userDataAsk] Outlet with inletId: $outletId, not exist."
            log.error(msg)
            Left(new IllegalArgumentException(msg))}
      case s ⇒
        //Incorrect state
        val msg =
          s"[DriveMessaging.userDataAsk] User message can be processed only in Starting, Working, " +
          s"Stopping states, current state: $s"
        log.error(msg)
        Left(new IllegalStateException(msg))}
  /** Send pending messages, called after dive turned on*/
  def sendPendingMessages(): Unit = {
    log.debug(s"[DriveMessaging.sendPendingMessages] Pending list: $pendingUserMessages")
    pendingUserMessages.foreach{ case (outletId, value) ⇒
      //Check outletId
      assume(
        outlets.contains(outletId),
        s"[DriveMessaging.userDataAsk] outlets not contain outletId: $outletId, outlets: $outlets")
      //Send
      sendUserMessage(outlets(outletId), value)}
    pendingUserMessages.clear()}
  /** User message from other outlet to self inlet, set to queue
    * @param outletId - Int, source ID
    * @param inletId - Int, drain ID
    * @param value - Any, user message */
  def userMessage(outletId: Int, inletId: Int, value: Any, state: State): Unit = state match{
    case State.Connected | State.TurnedOn | State.Starting | State.Working | State.Stopping | State.Stopped ⇒
      //Process message
      runForInlet(inletId){ inlet ⇒
        log.debug(s"[DriveMessaging.userMessage] inletId: $outletId, inlet: $inlet, value: $value")
        enqueueOrRunMessageTask(inlet, value).foreach(inlet ⇒ sendLoadMessage(inlet))}
    case _ ⇒
      //Incorrect state
      log.error(
        s"[DriveMessaging.userMessage] Incorrect state $state, inletId: $outletId, inlet: $inletId, value: $value.")
      val outletName =  s"№$outletId"
      val inletName = inlets
        .get(inletId).flatMap(_.name).getOrElse(s"№$inletId")
      userLogging ! M.LogError(
        Some(blockId),
        blockName.getOrElse(blockClassName),
        Seq(),
        s"Message $value from $outletName to $inletName not processed in state $state")}
  /** Starting of user messages processing */
  def startUserMessageProcessing(): Unit = {
    //Run for firs message
    log.debug(s"[DriveMessaging.startUserMessageProcessing] Call runMessageTaskLoop(), and send DriveTurnedOn")
    //Run
    runMessageTaskLoop().foreach(inlet ⇒ sendLoadMessage(inlet))
    //Report
    plumbing ! M.DriveTurnedOn}
  /** Message processing done, run next task
    * @param inletId - Int
    * @param execTime - Duration */
  def messageTaskDone(inletId: Int, execTime: Duration): Unit = runForInlet(inletId){ inlet ⇒
    log.debug(
      s"[DriveMessaging.messageTaskFailed] inlet: $inlet, execTime: $execTime, " +
      s"currentTask: ${inlet.currentTask}, taskQueue: ${inlet.taskQueue}.")
    //Remove current, run next task and send load message
    cleanCurrentTask(inlet)
    runNextMsgTask().foreach(inlet ⇒ sendLoadMessage(inlet))}
  /** Message processing take to long time, send warning to user logger
    * @param inletId - Int
    * @param execTime - Duration */
  def messageTaskTimeout(inletId: Int, execTime: Duration): Unit = runForInlet(inletId){ inlet ⇒
    //Send log message
    log.warning(s"[DriveMessaging.messageTaskTimeout] inlet: $inlet, execTime: $execTime.")
    userLogging ! M.LogWarning(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      s"Message handling timeout for ${inlet.name.getOrElse("")} inlet, on '$execTime', keep waiting.")}
  /** Message processing end with error, send error to user logger
    * @param inletId - Int
    * @param execTime - Duration
    * @param error - Throwable */
  def messageTaskFailed(inletId: Int, execTime: Duration, error: Throwable): Unit = runForInlet(inletId){ inlet ⇒
    log.error(
      error,
      s"[DriveMessaging.messageTaskFailed] inlet: $inlet, execTime: $execTime, " +
      s"currentTask: ${inlet.currentTask}, taskQueue: ${inlet.taskQueue}.")
    //Remove current, run next task and send load message
    cleanCurrentTask(inlet)
    runNextMsgTask().foreach(inlet ⇒ sendLoadMessage(inlet))
    //Send log message
    userLogging ! M.LogError(
      Some(blockId),
      blockName.getOrElse(blockClassName),
      Seq(error),
      s"Message handling fail for ${inlet.name.getOrElse("")} inlet, on '$execTime'.")}
  /** Check if no messages to process
    * @return - true if so */
  def isAllMsgProcessed: Boolean = {
    inlets.values.forall(inlet ⇒ inlet.taskQueue.isEmpty && inlet.currentTask.isEmpty) match{
      case true ⇒
        log.debug("[DriveMessaging.isAllMsgProcessed] All user messages processed.")
        true
      case false ⇒
        log.debug(s"[DriveMessaging.isAllMsgProcessed] Not all user messages processed, inlets: $inlets")
        false}}
  /** Inlet drive load, update back pressure time out for given drive.
    * @param subscriberId - (subscriber ActorRef, inlet ID Int), connected drive-subscriber
    * @param outletId - Int, connected outlet
    * @param inletQueueSize - Int */
  def driveLoad(subscriberId: (DriveRef, Int), outletId: Int, inletQueueSize: Int): Unit = {
    //Check data
    assume(
      outlets.contains(outletId),
      s"[DriveMessaging.driveLoad] outlets not contain outletId: $outletId, outlets: $outlets")
    assume(
      outlets(outletId).subscribers.contains(subscriberId),
      s"[DriveMessaging.driveLoad] subscribers not contain subscriberId: $subscriberId, outlet: ${outlets(outletId)}")
    //Update subscriber inletQueueSize
    val outlet = outlets(outletId)
    val subscriber = outlet.subscribers(subscriberId)
    log.debug(
      s"[DriveMessaging.driveLoad] Outlet: $outlet, old pushTimeout: ${outlet.pushTimeout}, " +
        s" subscriber: $subscriber, old inletQueueSize: ${subscriber.inletQueueSize}")
    subscriber.inletQueueSize = inletQueueSize
    log.debug(s"[DriveMessaging.driveLoad] Subscriber: $subscriber, new inletQueueSize: $inletQueueSize")
    //Re-evaluate of pushTimeout
    outlet.pushTimeout = calcPushTimeout(outlet.subscribers.values.map(_.inletQueueSize).max)
    log.debug(s"[DriveMessaging.driveLoad] Outlet: $outlet, new pushTimeout: ${outlet.pushTimeout}")}
  /** Get of pending list, used in test
    * @return - List[(Int, Any)] */
  def getMessagesPendingList: List[(Int, Any)] = pendingUserMessages.toList}
