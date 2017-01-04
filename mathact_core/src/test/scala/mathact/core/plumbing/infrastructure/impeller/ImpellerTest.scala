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

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.ActorTestSpec
import mathact.core.model.messages.M
import org.scalatest.{Matchers, Suite, WordSpecLike}

import scala.concurrent.duration._


/** Testing of ImpellerActor actor
  * Created by CAB on 11.08.2016.
  */

class ImpellerTest extends ActorTestSpec with WordSpecLike with Matchers{
  //Test model
  trait TestCase extends Suite{
    //Helpers infrastructure
    val testDriver = TestProbe("TestDrive_" + randomString())
    //ImpellerActor actor
    val impeller = system.actorOf(
      Props(new ImpellerActor(testDriver.ref, maxQueueSize = 2)),
      "Impeller_" + randomString())
    //Test tasks0
    val sleepTask = (req: (FiniteDuration, String))⇒()⇒{
      println(s"[ImpellerTest.sleepTask] Run test task with ${req._1} sleep and '${req._2}' result.")
      sleep(req._1)
      req._2}
    val errorTask = (req: (FiniteDuration, Throwable))⇒()⇒{
      println(s"[ImpellerTest.errorTask] Error after ${req._1} sleep.")
      sleep({req._1})
      throw req._2}}
  //Testing
  "ImpellerActor" should{
    "run task on RunTask and model with TaskDone on end of execution" in new TestCase {
      //Run 3 tasks
      (1 to 3).foreach{ i ⇒
        //Start task
        val result = randomString()
        val runMsg = M.RunTask(
          randomTaskKind(),
          randomInt(),
          10.seconds,
          skipOnTimeout = false,
          sleepTask((2.seconds, result)))
        testDriver.send(impeller, runMsg)
        //Wait for end
        sleep(1.seconds) //Wait for processing
        val doneMsg = testDriver.expectMsgType[M.TaskDone]
        println(s"[ImpellerTest] doneMsg: $doneMsg")
        doneMsg.id shouldEqual runMsg.id
        doneMsg.kind shouldEqual runMsg.kind
        doneMsg.taskRes shouldEqual result}}
    "run only one task at the time" in new TestCase {
      //Preparing
      val firstTask = M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        sleepTask((5.seconds, randomString())))
      val secondTask = M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        sleepTask((5.seconds, randomString())))
      //Start two
      testDriver.send(impeller, firstTask)
      testDriver.send(impeller, secondTask)
      //Await for end of first
      testDriver.expectNoMsg(4.seconds)
      val taskDone1 =  testDriver.expectMsgType[M.TaskDone]
      taskDone1.kind shouldEqual firstTask.kind
      taskDone1.id shouldEqual firstTask.id
      //Await for end of second
      testDriver.expectNoMsg(4.seconds)
      val taskDone2 =  testDriver.expectMsgType[M.TaskDone]
      taskDone2.kind shouldEqual secondTask.kind
      taskDone2.id shouldEqual secondTask.id}
    "send TaskFailed on exceedances of max queue size" in new TestCase {
      //Preparing
      def newTasks(num: Int) = (1 to num).map{ _ ⇒ M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        sleepTask((2.seconds, randomString())))}
      val successTasks = newTasks(3)
      val failureTasks = newTasks(2)
      //Start tasks
      successTasks.foreach(task ⇒ testDriver.send(impeller, task))
      failureTasks.foreach(task ⇒ testDriver.send(impeller, task))
      //Expect error for last three tasks
      (1 to 2).map(_ ⇒ testDriver.expectMsgType[M.TaskFailed].id).toSet shouldEqual failureTasks.map(_.id).toSet
      //Expect done for first two tasks
      (1 to 3).map(_ ⇒ testDriver.expectMsgType[M.TaskDone].id).toSet shouldEqual successTasks.map(_.id).toSet}
    "run task on RunTask and send TaskTimeout with timeout interval" in new TestCase {
      //Start task
      val result = randomString()
      val runMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        4.seconds,
        skipOnTimeout = false,
        sleepTask((10.seconds, result)))
      testDriver.send(impeller, runMsg)
      //Test first timeout message
      sleep(4.seconds) //Wait for processing
      val timeoutMsg1 = testDriver.expectMsgType[M.TaskTimeout]
      timeoutMsg1.id shouldEqual runMsg.id
      timeoutMsg1.kind shouldEqual runMsg.kind
      timeoutMsg1.timeFromStart >= 4.seconds shouldEqual true
      //Test second timeout message
      sleep(4.seconds) //Wait for processing
      val timeoutMsg2 = testDriver.expectMsgType[M.TaskTimeout]
      timeoutMsg2.id shouldEqual runMsg.id
      timeoutMsg2.kind shouldEqual runMsg.kind
      timeoutMsg2.timeFromStart >= 8.seconds shouldEqual true
      //Test done message
      sleep(2.seconds) //Wait for processing
      val doneMsg = testDriver.expectMsgType[M.TaskDone]
      doneMsg.id shouldEqual runMsg.id
      doneMsg.kind shouldEqual runMsg.kind
      doneMsg.taskRes shouldEqual result}
    "run task on RunTask and send TaskFailed if task end with error" in new TestCase {
      //Start task
      val error = new Exception("[ImpellerTest] Ooops!")
      val runMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        errorTask((4.seconds, error)))
      testDriver.send(impeller, runMsg)
      //Test done message
      sleep(3.seconds) //Wait for processing
      val failedMsg = testDriver.expectMsgType[M.TaskFailed]
      failedMsg.id shouldEqual runMsg.id
      failedMsg.kind shouldEqual runMsg.kind
      failedMsg.error shouldEqual error}
    "run task on RunTask, terminate it by SkipCurrentTask and send TaskFailed" in new TestCase {
      //Start to terminate task
      val toTermTaskMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        sleepTask((20.seconds, randomString())))
      testDriver.send(impeller, toTermTaskMsg)
      //Terminate
      testDriver.send(impeller, M.SkipCurrentTask)
      val failedMsg = testDriver.expectMsgType[M.TaskFailed]
      failedMsg.id shouldEqual toTermTaskMsg.id
      failedMsg.kind shouldEqual toTermTaskMsg.kind
      println(s"[ImpellerTest] failedMsg: $failedMsg")
      //Start new task
      val newTaskMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        10.seconds,
        skipOnTimeout = false,
        sleepTask((4.seconds, randomString())))
      testDriver.send(impeller, newTaskMsg)
      //Normal compilation
      sleep(3.seconds) //Wait for processing
      val doneMsg = testDriver.expectMsgType[M.TaskDone]
      doneMsg.id shouldEqual newTaskMsg.id
      doneMsg.kind shouldEqual newTaskMsg.kind}
    "by SkipAllTimeoutTask, skip current task if timeout happens" in new TestCase {
      //Start task
      val result = randomString()
      val runMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        4.seconds,
        skipOnTimeout = false,
        sleepTask((10.seconds, result)))
      testDriver.send(impeller, runMsg)
      sleep(1.second) //Wait some time
      //Not skip
      testDriver.send(impeller, M.SkipAllTimeoutTask)
      testDriver.expectNoMsg(2.second)
      //Time out
      val timeoutMsg1 = testDriver.expectMsgType[M.TaskTimeout]
      timeoutMsg1.id shouldEqual runMsg.id
      //Skip
      testDriver.send(impeller, M.SkipAllTimeoutTask)
      val failedMsg = testDriver.expectMsgType[M.TaskFailed]
      failedMsg.id shouldEqual runMsg.id
      println(s"[ImpellerTest] failedMsg: $failedMsg")}
    "if skipOnTimeout flag set, on time out skip task and return error" in new TestCase {
      //Start task
      val runMsg = M.RunTask(
        randomTaskKind(),
        randomInt(),
        4.seconds,
        skipOnTimeout = true,
        sleepTask((10.seconds, "---")))
      testDriver.send(impeller, runMsg)
      sleep(3.second) //Wait some time
      //Expect fail
      val failedMsg = testDriver.expectMsgType[M.TaskFailed]
      failedMsg.id shouldEqual runMsg.id
      println(s"[ImpellerTest] failedMsg: $failedMsg")}
    "end right task on done of fail"  in new TestCase {
      //Tasks
      def sleepTaskMsg(id: Int, sleepTime: FiniteDuration) = M.RunTask(
        randomTaskKind(),
        id,
        timeout = 2.seconds,
        skipOnTimeout = true,
        sleepTask((sleepTime, "---")))
      def failTaskMsg(id: Int, sleepTime: FiniteDuration) = M.RunTask(
        randomTaskKind(),
        id,
        timeout = 2.seconds,
        skipOnTimeout = true,
        errorTask((sleepTime, new Exception("Oops!!!"))))
      //Test for done
      testDriver.send(impeller, sleepTaskMsg(1, 3.seconds))
      testDriver.expectMsgType[M.TaskFailed].id shouldEqual 1
      testDriver.send(impeller, failTaskMsg(2, 1.seconds))
      testDriver.expectMsgType[M.TaskFailed].id shouldEqual 2
      //Test for fail
      testDriver.send(impeller, failTaskMsg(1, 3.seconds))
      testDriver.expectMsgType[M.TaskFailed].id shouldEqual 1
      testDriver.send(impeller, sleepTaskMsg(2, 1.seconds))
      testDriver.expectMsgType[M.TaskDone].id shouldEqual 2}
  }
}