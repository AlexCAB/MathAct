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

private [mathact] class ImpellerActor(drive: ActorRef, maxQueueSize: Int) extends WorkerBase{ import Impeller._
  //Variables
  var taskCounter = 0L
  val taskQueue = mutable.Queue[M.RunTask[Any]]()
  var currentTask: Option[(Long, TaskKind, Int, Long, Boolean)] = None // (task number, task kind, task ID, start time, have timeout)
  //Functions
  def runNextTask(kind: TaskKind, id: Int, timeout: FiniteDuration, task: ()⇒Any): Unit = {
    //Set current task
    val taskNumber = {taskCounter += 1; taskCounter}
    log.debug(s"[ImpellerActor.runNextTask] Try to run task, taskNumber: $taskNumber, kind: $kind, ID: $id")
    currentTask = Some((taskNumber, kind, id, System.currentTimeMillis, false))
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
      runNextTask(nextTask.kind, nextTask.id, nextTask.timeout, nextTask.task)
    case _ ⇒
      //Task queue is empty
      log.debug(s"[ImpellerActor.dequeueAndRunNextTask] Task queue is empty, nothing to do.")}
  //Messages handling
  def reaction = {
    //Starting of task in separate thread and start of task timeout
    case M.RunTask(kind, id, timeout, task) if sender == drive ⇒ currentTask match{
      case None ⇒
        log.debug(s"[ImpellerActor.RunTask] No current task, run immediately, kind: $kind, ID: $id")
        runNextTask(kind, id, timeout, task)
      case Some((curNum, curKind, curId, startTime, isTimeout)) if taskQueue.size < maxQueueSize ⇒
        log.debug(
          s"[ImpellerActor.RunTask] Current task run, enqueue new one, curNum: $curNum, curKind: $curKind, ID: $curId, " +
          s"startTime: $startTime, isTimeout: $isTimeout, kind: $kind, ID: $id, timeout: $timeout, " +
          s"maxQueueSize: $maxQueueSize")
        taskQueue.enqueue(M.RunTask(kind, id, timeout, task))
      case _ ⇒
        val msg =
          s"[ImpellerActor.RunTask] Can't enqueue new task $kind kind with ID $id, " +
          s"since maxQueueSize ($maxQueueSize) is achieved."
        log.error(msg)
        drive ! M.TaskFailed(kind, id, 0.millis, new Exception(msg))}
    //Remove current task
    case M.SkipCurrentTask if sender == drive ⇒ currentTask match{
      case Some((curNum, curKind, curId, startTime, _)) ⇒
        val msg = s"[ImpellerActor.SkipCurrentTask] Current task will skip, number: $curNum, kind: $curKind, DI: $curId"
        log.warning(msg)
        currentTask = None
        drive ! M.TaskFailed(curKind, curId, (System.currentTimeMillis - startTime).millis, new Exception(msg))
        dequeueAndRunNextTask()
      case None ⇒
        log.debug("[ImpellerActor.SkipCurrentTask] Nothing to skip.")}
    //Remove current task if time out happens
    case M.SkipAllTimeoutTask if sender == drive ⇒ currentTask match{
      case Some((curNum, curKind, curId, startTime, isTimeout)) if isTimeout ⇒
        val msg = s"[ImpellerActor.SkipAllTimeoutTask] Current task will skip, number: $curNum, kind: $curKind, DI: $curId"
        log.warning(msg)
        currentTask = None
        drive ! M.TaskFailed(curKind, curId, (System.currentTimeMillis - startTime).millis, new Exception(msg))
        dequeueAndRunNextTask()
      case curTask ⇒
        log.debug(s"[ImpellerActor.SkipAllTimeoutTask] Current task empty, nothing to skip.")}
    //Task timeout, send time out and restart timer
    case TaskTimeout(taskNumber, timeout) ⇒ currentTask match{
      case Some((`taskNumber`, kind, id, startTime, _)) ⇒
        log.debug(
          s"[ImpellerActor.TaskTimeout] Task timeout, kind: $kind, id: $id, taskNumber: $taskNumber, after $timeout wait.")
        currentTask = Some((taskNumber, kind, id, startTime, true))  //Set task have timeout
        drive ! M.TaskTimeout(kind, id, (System.currentTimeMillis - startTime).millis)
        context.system.scheduler.scheduleOnce(timeout, self, TaskTimeout(taskNumber, timeout))
      case _ ⇒
        log.debug(
          s"[ImpellerActor.TaskTimeout] Task done or skip, stop timer, taskNumber: $taskNumber, " +
          s"timeout: $timeout, currentTask: $currentTask")}
    //Task done, send report to driver
    case TaskSuccess(taskNumber, res) ⇒ currentTask match{
      case Some((curNum, kind, id, startTime, isTimeout)) ⇒
        val execTime = (System.currentTimeMillis - startTime).millis
        log.debug(
          s"[ImpellerActor.TaskSuccess] Task done, number: $curNum, kind: $kind, id: $id, res: $res, " +
            s"execTime: $execTime, startTime: $startTime, isTimeout: $isTimeout" )
        currentTask = None
        drive ! M.TaskDone(kind, id, execTime, res)
        dequeueAndRunNextTask()
      case None ⇒
        log.warning(
          s"[ImpellerActor.TaskSuccess] Completed not a current task (probably current been skipped), " +
            s"taskNumber: $taskNumber.")}
    //Task failed, send report to driver
    case TaskFailure(taskNumber, err) ⇒ currentTask match{
      case Some((curNum, curKind, curId, startTime, _)) ⇒
        val execTime = (System.currentTimeMillis - startTime).millis
        log.debug(
          s"[ImpellerActor.TaskFailure] Task fail, number: $curNum, kind: $curKind, ID: $curId, " +
          s"err: $err, execTime: $execTime")
        currentTask = None
        drive ! M.TaskFailed(curKind, curId, execTime, err)
        dequeueAndRunNextTask()
      case None ⇒
        log.warning(
          s"[ImpellerActor.TaskFailure] Failed not a current task (probably current been skipped), " +
          s"taskNumber: $taskNumber.")}}
  //Cleanup
  def cleanup(): Unit = {}}
