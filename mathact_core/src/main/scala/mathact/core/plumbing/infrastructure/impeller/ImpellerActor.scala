/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package mathact.core.plumbing.infrastructure.impeller

import akka.actor.ActorRef
import mathact.core.WorkerBase
import mathact.core.model.enums.TaskKind
import mathact.core.model.messages.M

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


/** User code processor
  * Created by CAB on 15.05.2016.
  */

private[core] class ImpellerActor(drive: ActorRef, maxQueueSize: Int) extends WorkerBase{ import Impeller._
  //Variables
  var taskCounter = 0L
  val taskQueue = mutable.Queue[M.RunTask[Any]]()
  var currentTask: Option[TaskState] = None
  //Functions
  def runNextTask(kind: TaskKind, id: Int, timeout: FiniteDuration, skipOnTimeout: Boolean, task: ()⇒Any): Unit = {
    //Set current task
    val taskNumber = {taskCounter += 1; taskCounter}
    log.debug(s"[ImpellerActor.runNextTask] Try to run task, taskNumber: $taskNumber, kind: $kind, ID: $id")
    currentTask = Some(TaskState(taskNumber, kind, id, skipOnTimeout, System.currentTimeMillis, isTimeout = false))
    //Run time out
    context.system.scheduler.scheduleOnce(timeout, self, TaskTimeout(taskNumber, timeout))
    //Run task
    Future{task()}.onComplete{
      case Success(res) ⇒ self ! TaskSuccess(taskNumber, res)
      case Failure(err) ⇒ self ! TaskFailure(taskNumber, err)}}
  def dequeueAndRunNextTask(): Unit = taskQueue.size match{
    case s if s > 0 ⇒
      //Run next task
      val nextTask = taskQueue.dequeue()
      log.debug(s"[ImpellerActor.dequeueAndRunNextTask] Next task to run: $nextTask")
      runNextTask(nextTask.kind, nextTask.id, nextTask.timeout, nextTask.skipOnTimeout, nextTask.task)
    case _ ⇒
      //Task queue is empty
      log.debug(s"[ImpellerActor.dequeueAndRunNextTask] Task queue is empty, nothing to do.")}
  def skipCurrentTask(): Unit = currentTask match{
    case Some(state) ⇒
      val msg = s"[ImpellerActor.skipCurrentTask] Current task will skip, state: $state"
      log.warning(msg)
      currentTask = None
      val executionTime = (System.currentTimeMillis - state.startTime).millis
      drive ! M.TaskFailed(state.kind, state.taskId, executionTime, new Exception(msg))
    case curTask ⇒
      log.debug(s"[ImpellerActor.skipCurrentTask] Current task empty, nothing to skip.")}
  //Messages handling
  def reaction = {
    //Starting of task in separate thread and start of task timeout
    case M.RunTask(kind, id, timeout, skipOnTimeout, task) if sender == drive ⇒ currentTask match{
      case None ⇒
        log.debug(s"[ImpellerActor.RunTask] No current task, run immediately, kind: $kind, ID: $id")
        runNextTask(kind, id, timeout, skipOnTimeout, task)
      case Some(state) if taskQueue.size < maxQueueSize ⇒
        log.debug(
          s"[ImpellerActor.RunTask] Current task run state: $state, enqueue new one, " +
          s"kind: $kind, ID: $id, timeout: $timeout, maxQueueSize: $maxQueueSize")
        taskQueue.enqueue(M.RunTask(kind, id, timeout, skipOnTimeout, task))
      case _ ⇒
        val msg =
          s"[ImpellerActor.RunTask] Can't enqueue new task $kind kind with ID $id, " +
          s"since maxQueueSize ($maxQueueSize) is achieved."
        log.error(msg)
        drive ! M.TaskFailed(kind, id, 0.millis, new Exception(msg))}
    //Remove current task
    case M.SkipCurrentTask if sender == drive ⇒ currentTask match{
      case Some(state) ⇒
        log.debug(s"[ImpellerActor.SkipCurrentTask] Task state: $state.")
        skipCurrentTask()
        dequeueAndRunNextTask()
      case None ⇒
        log.debug("[ImpellerActor.SkipCurrentTask] Nothing to skip.")}
    //Remove current task if time out happens
    case M.SkipAllTimeoutTask if sender == drive ⇒ currentTask match{
      case Some(state) if state.isTimeout ⇒
        log.debug(s"[ImpellerActor.SkipAllTimeoutTask] Task state: $state.")
        skipCurrentTask()
        dequeueAndRunNextTask()
      case curTask ⇒
        log.debug(s"[ImpellerActor.SkipAllTimeoutTask] Current task empty, nothing to skip.")}
    //Task timeout, send time out and restart timer
    case TaskTimeout(taskNumber, timeout) ⇒ currentTask match{
      case Some(state) if state.taskNumber == taskNumber && (! state.skipOnTimeout) ⇒
        log.debug(
          s"[ImpellerActor.TaskTimeout] Task timeout, skipOnTimeout = false, state: $state, after $timeout wait.")
        currentTask = Some(state.copy(isTimeout = true))  //Set task have timeout
        val executionTime = (System.currentTimeMillis - state.startTime).millis
        drive ! M.TaskTimeout(state.kind, state.taskId, executionTime, state.skipOnTimeout)
        context.system.scheduler.scheduleOnce(timeout, self, TaskTimeout(taskNumber, timeout))
      case Some(state) if state.taskNumber == taskNumber && state.skipOnTimeout ⇒
        log.debug(
          s"[ImpellerActor.TaskTimeout] Task timeout, skipOnTimeout = true, state: $state, after $timeout wait.")
        skipCurrentTask()
        dequeueAndRunNextTask()
      case _ ⇒
        log.debug(
          s"[ImpellerActor.TaskTimeout] Task done or skip, stop timer, taskNumber: $taskNumber, " +
          s"timeout: $timeout, currentTask: $currentTask")}
    //Task done, send report to driver
    case TaskSuccess(taskNumber, res) ⇒ currentTask match{
      case Some(state) if state.taskNumber == taskNumber ⇒
        val execTime = (System.currentTimeMillis - state.startTime).millis
        log.debug(
          s"[ImpellerActor.TaskSuccess] Task done taskNumber: $taskNumber, state: $state, " +
          s"res: $res, execTime: $execTime" )
        currentTask = None
        drive ! M.TaskDone(state.kind, state.taskId, execTime, res)
        dequeueAndRunNextTask()
      case _ ⇒
        log.warning(
          s"[ImpellerActor.TaskSuccess] Completed not a current task (probably current been skipped), " +
          s"taskNumber: $taskNumber.")}
    //Task failed, send report to driver
    case TaskFailure(taskNumber, err) ⇒ currentTask match{
      case Some(state) if state.taskNumber == taskNumber ⇒
        val execTime = (System.currentTimeMillis - state.startTime).millis
        log.debug(
          s"[ImpellerActor.TaskFailure] Task fail, taskNumber: $taskNumber, state: $state, " +
          s"err: $err, execTime: $execTime")
        currentTask = None
        drive ! M.TaskFailed(state.kind, state.taskId, execTime, err)
        dequeueAndRunNextTask()
      case _ ⇒
        log.warning(
          s"[ImpellerActor.TaskFailure] Failed not a current task (probably current been skipped), " +
          s"taskNumber: $taskNumber.")}}
  //Cleanup
  def cleanup(): Unit = {}}
