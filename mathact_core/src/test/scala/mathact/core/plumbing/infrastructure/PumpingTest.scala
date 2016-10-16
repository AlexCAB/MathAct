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

import akka.actor.{PoisonPill, ActorRef, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import mathact.core.ActorTestSpec
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike, PumpingConfigLike}
import mathact.core.model.messages.M
import mathact.core.plumbing.{Fitting, PumpLike}
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of PumpingActor actor
  * Created by CAB on 30.08.2016.
  */

class PumpingTest extends ActorTestSpec{
  //Test model
  trait TestCase extends Suite{
    //Values
    val testException1 = new Exception("Oops111")
    val testException2 = new Exception("Oops222")
    //Test controller and logger
    lazy val testController = TestProbe("TestSketchController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    //Test drives
    lazy val testDrive1 = TestProbe("TestDrive1_" + randomString())
    lazy val testDrive2 = TestProbe("TestDrive2_" + randomString())
    //Test objects
    case class TestPump(index: Int) extends PumpLike {
      val tool: Fitting = null
      val toolName = "TestTool" + index
      val toolImagePath = None}
    val testPumpingConfig = new PumpingConfigLike{
      val pump = new PumpConfigLike{
        val askTimeout = Timeout(1.second) }
      val drive = new DriveConfigLike{
        val pushTimeoutCoefficient = 0
        val startFunctionTimeout = 1.second
        val messageProcessingTimeout = 1.second
        val stopFunctionTimeout = 1.second
        val impellerMaxQueueSize = 0
        val uiOperationTimeout = 1.second}}
    //PumpingActor
    object actors{
      lazy val pumping = system.actorOf(Props(
        new PumpingActor(testPumpingConfig, testController.ref,  "TestSketch", testUserLogging.ref, testVisualization.ref){
          override def createDriveActor(toolPump: PumpLike): (ActorRef, Int)  = {
            val index = toolPump.asInstanceOf[TestPump].index
            val actor =List(testDrive1.ref, testDrive2.ref)(index)
            context.watch(actor)
            (actor, index + 1)}}),
        "Pumping_" + randomString())
      lazy val pumpingWithDrives = {
        pumping
        testController.send(pumping, M.NewDrive(TestPump(0)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        testController.send(pumping, M.NewDrive(TestPump(1)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        pumping}
      lazy val builtPumping = {
        pumpingWithDrives
        testController.send(actors.pumping, M.BuildPumping)
        testDrive1.expectMsg(M.ConstructDrive)
        testDrive1.send(actors.pumping, M.DriveConstructed)
        testDrive2.expectMsg(M.ConstructDrive)
        testDrive2.send(actors.pumping, M.DriveConstructed)
        testDrive1.expectMsg(M.BuildDrive)
        testDrive1.send(actors.pumping, M.DriveBuilt)
        testDrive2.expectMsg(M.BuildDrive)
        testDrive2.send(actors.pumping, M.DriveBuilt)
        testController.expectMsg(M.PumpingBuilt)
        testUserLogging.expectMsgType[M.LogInfo]
        pumpingWithDrives}
      lazy val startedPumpingWithDrives = {
        builtPumping
        testController.send(pumping, M.StartPumping)
        testDrive1.expectMsg(M.StartDrive)
        testDrive1.send(pumping, M.DriveStarted)
        testDrive2.expectMsg(M.StartDrive)
        testDrive2.send(pumping, M.DriveStarted)
        testController.expectMsg(M.PumpingStarted)
        testVisualization.expectMsg(M.AllToolBuilt)
        builtPumping}}}
  //Testing
  "PumpingActor normal workflow" should{
    "by M.NewDrive, create and return new drive actor" in new TestCase {
      //Create first drive
      testController.send(actors.pumping, M.NewDrive(TestPump(0)))
      val drive1 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive1.isRight shouldEqual true
      drive1.right.get shouldEqual testDrive1.ref
      //Create second drive
      testController.send(actors.pumping, M.NewDrive(TestPump(1)))
      val drive2 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive2.isRight shouldEqual true
      drive2.right.get shouldEqual testDrive2.ref}
    "by BuildPumping, build all drives and response with PumpingBuilt" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      //Send BuildPumping
      testController.send(actors.pumping, M.BuildPumping)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.pumping, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.pumping, M.DriveConstructed)
      //Build drives
      testDrive1.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.pumping, M.DriveBuilt)
      testDrive2.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.pumping, M.DriveBuilt)
      //Built
      testController.expectMsg(M.PumpingBuilt)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpingTest] logInfo: " + logInfo)}
    "by M.StartPumping, build and start all drives, response M.PumpingStarted" in new TestCase {
      //Preparing
      actors.builtPumping
      //Start
      testController.send(actors.pumping, M.StartPumping)
      //Start drives
      testDrive1.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive1.send(actors.pumping, M.DriveStarted)
      testDrive2.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive2.send(actors.pumping, M.DriveStarted)
      //Built
      testController.expectMsg(M.PumpingStarted)
      testVisualization.expectMsg(M.AllToolBuilt)}
    "by M.StopPumping, stop all drives, response M.PumpingStopped" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      //Stop pumping
      testController.send(actors.pumping, M.StopPumping)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Stopped
      testController.expectMsg(M.PumpingStopped)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpingTest] logInfo: " + logInfo)}
    "by M.PumpingShutdown after stop, terminate all drives and then self" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Stop pumping
      testController.send(actors.pumping, M.StopPumping)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      testController.expectMsg(M.PumpingStopped)
      testUserLogging.expectMsgType[M.LogInfo]
      //Send ShutdownPumping
      testController.send(actors.pumping, M.ShutdownPumping)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "by M.SkipAllTimeoutTask, send SkipTimeoutTask to all drives" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      //Test
      testController.send(actors.pumping, M.SkipAllTimeoutTask)
      testDrive1.expectMsg(M.SkipTimeoutTask)
      testDrive2.expectMsg(M.SkipTimeoutTask)}
    "by M.ShowAllToolUi, send it ShowToolUi all drives" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      //Test
      testController.send(actors.pumping, M.ShowAllToolUi)
      testDrive1.expectMsg(M.ShowToolUi)
      testDrive2.expectMsg(M.ShowToolUi)}
    "by M.HideAllToolUi, send HideToolUi to all drives" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      //Test
      testController.send(actors.pumping, M.HideAllToolUi)
      testDrive1.expectMsg(M.HideToolUi)
      testDrive2.expectMsg(M.HideToolUi)}
  }
  "PumpingActor shutdown" should{
    "shutdown in Init state" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumping)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Creating state" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumping)
      //Send BuildPumping
      testController.send(actors.pumping, M.BuildPumping)
      //Construct first drive
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.pumping, M.DriveConstructed)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Construct second drive
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.pumping, M.DriveConstructed)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Building state" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumping)
      //Send BuildPumping
      testController.send(actors.pumping, M.BuildPumping)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.pumping, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.pumping, M.DriveConstructed)
      //Build first drives
      testDrive1.expectMsg(M.BuildDrive)
      testDrive1.send(actors.pumping, M.DriveBuilt)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Build second drives
      testDrive2.expectMsg(M.BuildDrive)
      testDrive2.send(actors.pumping, M.DriveBuilt)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Built state" in new TestCase {
      //Preparing
      actors.builtPumping
      testController.watch(actors.pumping)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Starting state" in new TestCase {
      //Preparing
      actors.builtPumping
      testController.watch(actors.pumping)
      //Send StartPumping
      testController.send(actors.pumping, M.StartPumping)
      //Start first drives
      testDrive1.expectMsg(M.StartDrive)
      testDrive1.send(actors.pumping, M.DriveStarted)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Start second drives
      testDrive2.expectMsg(M.StartDrive)
      testDrive2.send(actors.pumping, M.DriveStarted)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Work state" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Sopping state" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Send StopPumping
      testController.send(actors.pumping, M.StopPumping)
      //Stopping first drive
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Stopping second drive
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
    "shutdown in Sopped state (normal)" in new TestCase {
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Send StopPumping
      testController.send(actors.pumping, M.StopPumping)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      testController.expectMsg(M.PumpingStopped)
      //Send PumpingShutdown
      testController.send(actors.pumping, M.ShutdownPumping)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingShutdown and Terminated
      testController.expectMsg(M.PumpingShutdown)
      testController.expectTerminated(actors.pumping)}
  }
  "PumpingActor failure" should{
    "failure in Building state" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumping)
      //Send BuildPumping
      testController.send(actors.pumping, M.BuildPumping)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.pumping, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.pumping, M.DriveConstructed)
      //Build first drive (failed)
      testDrive1.expectMsg(M.BuildDrive)
      testDrive1.send(actors.pumping, M.DriveError(testException1))
      testDrive1.send(actors.pumping, M.DriveBuilt)
      //Build second drive
      testDrive2.expectMsg(M.BuildDrive)
      testDrive2.send(actors.pumping, M.DriveBuilt)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingError and Terminated
      testController.expectMsgType[M.PumpingError].errors shouldEqual Seq(testException1)
      testController.expectTerminated(actors.pumping)}
    "failure in Starting state" in new TestCase {
      //Preparing
      actors.builtPumping
      testController.watch(actors.pumping)
      //Send StartPumping
      testController.send(actors.pumping, M.StartPumping)
      //Start first drives (failed)
      testDrive1.expectMsg(M.StartDrive)
      testDrive1.send(actors.pumping, M.DriveError(testException1))
      testDrive1.send(actors.pumping, M.DriveStarted)
      //Start second drives (failed)
      testDrive2.expectMsg(M.StartDrive)
      testDrive2.send(actors.pumping, M.DriveError(testException2))
      testDrive2.send(actors.pumping, M.DriveStarted)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingError and Terminated
      testController.expectMsgType[M.PumpingError].errors.toSet shouldEqual Set(testException1, testException2)
      testController.expectTerminated(actors.pumping)}
    "failure in Work state" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Send DriveError
      testDrive1.send(actors.pumping, M.DriveError(testException1))
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingError and Terminated
      testController.expectMsgType[M.PumpingError].errors shouldEqual Seq(testException1)
      testController.expectTerminated(actors.pumping)}
    "failure in Sopping state" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.pumping)
      //Send StopPumping
      testController.send(actors.pumping, M.StopPumping)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.pumping, M.DriveError(testException1))
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.pumping, M.DriveError(testException2))
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PumpingError and Terminated
      testController.expectMsgType[M.PumpingError].errors.toSet shouldEqual Set(testException1, testException2)
      testController.expectTerminated(actors.pumping)}
  }
}
