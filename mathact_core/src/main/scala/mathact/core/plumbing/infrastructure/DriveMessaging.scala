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

import akka.actor.ActorRef
import mathact.core.model.enums._
import mathact.core.model.messages.M
import scala.concurrent.duration.Duration


/** Handling of messages
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveMessaging { _: Drive ⇒
  //Functions
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
    inlet.currentTask match{
      case Some(_) ⇒
        inlet.currentTask = None
      case None ⇒
        log.error(s"[DriveMessaging.cleanCurrentTask] Not set currentTask, inlet: $inlet.")}}
  //TODO Для уменьшения количества отправленых DriveLoad, нужно использовать что то вроде ПИ
  //TODO регулятора с мёртвой зоной (чтобы DriveLoad рассылалась не по каждому измению размера очереди).
  //TODO Сейчас отправка DriveLoad на каждое измение размера очерели инлета.
  private def sendLoadMessage(inlet: InletState): Unit = {
    //Send load messages
    inlet.publishers.values.foreach{ publisher ⇒
      val load = inlet.taskQueue.size
      log.debug(s"[DriveMessaging.sendLoadMessage] Send DriveLoad($load), to publisher: $publisher.")
      publisher.toolDrive ! M.DriveLoad((self, inlet.inletId), publisher.pipeId, load)}}
  //Methods
  /** User model from self outlet, send to all outlet subscribers
    * @param outletId - Int, source ID
    * @param value -  Any, user model */
  def userDataAsk(outletId: Int, value: Any, state: ActorState): Either[Throwable, Option[Long]] = state match{
      case ActorState.Starting | ActorState.Working | ActorState.Stopping ⇒
        //Get of outlet
        outlets.get(outletId) match{
          case Some(outlet) ⇒
            //Distribution of UserMessage
            outlet.subscribers.values.foreach{ subscriber ⇒
              subscriber.inlet.toolDrive ! M.UserMessage(outletId, subscriber.inlet.pipeId, value)}
            //Push timeout
            log.debug(
              s"[DriveMessaging.userDataAsk] Data: $value, sent from outletId: $outletId to " +
              s"all subscribers ${outlet.subscribers} , pushTimeout: ${outlet.pushTimeout}")
            //Return pour timeout
            Right(outlet.pushTimeout)
          case None ⇒
            Left(new IllegalArgumentException(
              s"[DriveMessaging.userDataAsk] Outlet with outletId: $outletId, not exist."))}
      case s ⇒
        //Incorrect state
        val msg =
          s"[DriveMessaging.userDataAsk] User model can be processed only in Starting Working, " +
          s"or Stopping state, current state: $s"
        log.error(msg)
        Left(new IllegalStateException(msg))}
  /** User message from other outlet to self inlet, set to queue
    * @param outletId - Int, source ID
    * @param inletId - Int, drain ID
    * @param value - Any, user model */
  def userMessage(outletId: Int, inletId: Int, value: Any, state: ActorState): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      //Check state
      state match{
        case ActorState.Starting ⇒
          //Add task to the queue and reply with load message
          enqueueMessageTask(inlet, value)
          sendLoadMessage(inlet)
        case ActorState.Working | ActorState.Stopping ⇒
          //Put in queue, start processing and reply with DriveLoad
          enqueueOrRunMessageTask(inlet, value).foreach(inlet ⇒ sendLoadMessage(inlet))
        case s ⇒
          //Incorrect state
          log.error(s"[DriveMessaging.userMessage] Incorrect state: $s, required Starting, Working or Stopping")}
      case None ⇒
        //Incorrect inletId
        log.error(s"[DriveMessaging.userMessage] Inlet with inletId: $inletId, not exist.")}
  /** Starting of user messages processing */
  def startUserMessageProcessing(): Unit = {
    //Run for firs message
    log.error(s"[DriveMessaging.startUserMessageProcessing] Call runMessageTaskLoop().")
    runMessageTaskLoop().foreach(inlet ⇒ sendLoadMessage(inlet))}
  /** Message processing done, run next task
    * @param inletId - Int
    * @param execTime - Duration */
  def messageTaskDone(inletId: Int, execTime: Duration): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      log.debug(
        s"[DriveMessaging.messageTaskFailed] inlet: $inlet, execTime: $execTime, " +
        s"currentTask: ${inlet.currentTask}, taskQueue: ${inlet.taskQueue}.")
      //Remove current, run next task and send load message
      cleanCurrentTask(inlet)
      runNextMsgTask().foreach(inlet ⇒ sendLoadMessage(inlet))
    case None ⇒
      //Incorrect inletId
      log.error(s"[DriveMessaging.messageTaskDone] Inlet with inletId: $inletId, not exist.")}
  /** Message processing take to long time, send warning to user logger
    * @param inletId - Int
    * @param execTime - Duration */
  def messageTaskTimeout(inletId: Int, execTime: Duration): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      //Send log message
      log.warning(s"[DriveMessaging.messageTaskTimeout] inlet: $inlet, execTime: $execTime.")
      userLogging ! M.LogWarning(
        Some(toolId),
        pump.toolName,
        s"Message handling timeout for ${inlet.name.getOrElse("")} inlet, on '$execTime', keep waiting.")
    case None ⇒
      //Incorrect inletId
      log.error(s"[DriveMessaging.messageTaskTimeout] Inlet with inletId: $inletId, not exist.")}
  /** Message processing end with error, send error to user logger
    * @param inletId - Int
    * @param execTime - Duration
    * @param error - Throwable */
  def messageTaskFailed(inletId: Int, execTime: Duration, error: Throwable): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      log.error(
        s"[DriveMessaging.messageTaskFailed] inlet: $inlet, execTime: $execTime, error: $error, " +
        s"currentTask: ${inlet.currentTask}, taskQueue: ${inlet.taskQueue}.")
      //Remove current, run next task and send load message
      cleanCurrentTask(inlet)
      runNextMsgTask().foreach(inlet ⇒ sendLoadMessage(inlet))
      //Send log message
      userLogging ! M.LogError(
        Some(toolId),
        pump.toolName,
        Some(error),
        s"Message handling fail for ${inlet.name.getOrElse("")} inlet, on '$execTime'.")
    case None ⇒
      //Incorrect inletId
      log.error(s"[DriveMessaging.messageTaskFailed] Inlet with inletId: $inletId, not exist.")}
  /** Check if no messages to process
    * @return - true if so */
  def isAllMsgProcessed: Boolean =
    inlets.values.forall(inlet ⇒ inlet.taskQueue.isEmpty && inlet.currentTask.isEmpty)
  /** Inlet drive load, update back pressure time out for given drive.
    * @param subscriberId - (subscriber ActorRef, inlet ID Int), connected drive-subscriber
    * @param outletId - Int, connected outlet
    * @param inletQueueSize - Int */
  def driveLoad(subscriberId: (ActorRef, Int), outletId: Int, inletQueueSize: Int): Unit = outlets.get(outletId) match{
    case Some(outlet) ⇒ outlet.subscribers.get(subscriberId) match{
      case Some(subscriber) ⇒
        log.debug(
          s"[DriveMessaging.driveLoad] Outlet: $outlet, old pushTimeout: ${outlet.pushTimeout}, " +
          s" subscriber: $subscriber, old inletQueueSize: ${subscriber.inletQueueSize}")
        //Update subscriber inletQueueSize
        subscriber.inletQueueSize = inletQueueSize
        log.debug(s"[DriveMessaging.driveLoad] Subscriber: $subscriber, new inletQueueSize: $inletQueueSize")
        //Re-evaluate of pushTimeout
        outlet.pushTimeout = outlet.subscribers.values.map(_.inletQueueSize).max match{
          case mqs if mqs > 0 ⇒ Some(mqs * config.pushTimeoutCoefficient)
          case _ ⇒ None}
        log.debug(s"[DriveMessaging.driveLoad] Outlet: $outlet, new pushTimeout: ${outlet.pushTimeout}")
      case None ⇒
        //Incorrect subscriberId
        log.error(s"[DriveMessaging.driveLoad] Subscriber with subscriberId: $subscriberId, not exist.")}
    case None ⇒
      //Incorrect outletId
      log.error(s"[DriveMessaging.driveLoad] Outlet with outletId: $outletId, not exist.")}}
