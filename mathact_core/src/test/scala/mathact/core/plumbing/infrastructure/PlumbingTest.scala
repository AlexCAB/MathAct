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
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike, PlumbingConfigLike}
import mathact.core.model.messages.M
import mathact.core.plumbing.{Fitting, PumpLike}
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of PlumbingActor actor
  * Created by CAB on 30.08.2016.
  */

class PlumbingTest extends ActorTestSpec{
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
    val testPlumbingConfig = new PlumbingConfigLike{
      val pump = new PumpConfigLike{
        val askTimeout = Timeout(1.second) }
      val drive = new DriveConfigLike{
        val pushTimeoutCoefficient = 0
        val startFunctionTimeout = 1.second
        val messageProcessingTimeout = 1.second
        val stopFunctionTimeout = 1.second
        val impellerMaxQueueSize = 0
        val uiOperationTimeout = 1.second}}
    //PlumbingActor
    object actors{
      lazy val plumbing = system.actorOf(Props(
        new PlumbingActor(testPlumbingConfig, testController.ref,  "TestSketch", testUserLogging.ref, testVisualization.ref){
          override def createDriveActor(toolPump: PumpLike): (ActorRef, Int)  = {
            val index = toolPump.asInstanceOf[TestPump].index
            val actor =List(testDrive1.ref, testDrive2.ref)(index)
            context.watch(actor)
            (actor, index + 1)}}),
        "Plumbing_" + randomString())
      lazy val plumbingWithDrives = {
        plumbing
        testController.send(plumbing, M.NewDrive(TestPump(0)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        testController.send(plumbing, M.NewDrive(TestPump(1)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        plumbing}
      lazy val builtPlumbing = {
        plumbingWithDrives
        testController.send(actors.plumbing, M.BuildPlumbing)
        testDrive1.expectMsg(M.ConstructDrive)
        testDrive1.send(actors.plumbing, M.DriveConstructed)
        testDrive2.expectMsg(M.ConstructDrive)
        testDrive2.send(actors.plumbing, M.DriveConstructed)
        testDrive1.expectMsg(M.BuildDrive)
        testDrive1.send(actors.plumbing, M.DriveBuilt)
        testDrive2.expectMsg(M.BuildDrive)
        testDrive2.send(actors.plumbing, M.DriveBuilt)
        testController.expectMsg(M.PlumbingBuilt)
        testUserLogging.expectMsgType[M.LogInfo]
        plumbingWithDrives}
      lazy val startedPlumbingWithDrives = {
        builtPlumbing
        testController.send(plumbing, M.StartPlumbing)
        testDrive1.expectMsg(M.StartDrive)
        testDrive1.send(plumbing, M.DriveStarted)
        testDrive2.expectMsg(M.StartDrive)
        testDrive2.send(plumbing, M.DriveStarted)
        testController.expectMsg(M.PlumbingStarted)
        testVisualization.expectMsg(M.AllToolBuilt)
        builtPlumbing}}}
  //Testing
  "PlumbingActor normal workflow" should{
    "by M.NewDrive, create and return new drive actor" in new TestCase {
      //Create first drive
      testController.send(actors.plumbing, M.NewDrive(TestPump(0)))
      val drive1 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive1.isRight shouldEqual true
      drive1.right.get shouldEqual testDrive1.ref
      //Create second drive
      testController.send(actors.plumbing, M.NewDrive(TestPump(1)))
      val drive2 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive2.isRight shouldEqual true
      drive2.right.get shouldEqual testDrive2.ref}
    "by BuildPlumbing, build all drives and response with PlumbingBuilt" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      //Send BuildPlumbing
      testController.send(actors.plumbing, M.BuildPlumbing)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Build drives
      testDrive1.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.plumbing, M.DriveBuilt)
      testDrive2.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.plumbing, M.DriveBuilt)
      //Built
      testController.expectMsg(M.PlumbingBuilt)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PlumbingTest] logInfo: " + logInfo)}
    "by M.StartPlumbing, build and start all drives, response M.PlumbingStarted" in new TestCase {
      //Preparing
      actors.builtPlumbing
      //Start
      testController.send(actors.plumbing, M.StartPlumbing)
      //Start drives
      testDrive1.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive1.send(actors.plumbing, M.DriveStarted)
      testDrive2.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive2.send(actors.plumbing, M.DriveStarted)
      //Built
      testController.expectMsg(M.PlumbingStarted)
      testVisualization.expectMsg(M.AllToolBuilt)}
    "by M.StopPlumbing, stop all drives, response M.PlumbingStopped" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Stop plumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Stopped
      testController.expectMsg(M.PlumbingStopped)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PlumbingTest] logInfo: " + logInfo)}
    "by M.PlumbingShutdown after stop, terminate all drives and then self" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Stop plumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      testController.expectMsg(M.PlumbingStopped)
      testUserLogging.expectMsgType[M.LogInfo]
      //Send ShutdownPlumbing
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "by M.SkipAllTimeoutTask, send SkipTimeoutTask to all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.SkipAllTimeoutTask)
      testDrive1.expectMsg(M.SkipTimeoutTask)
      testDrive2.expectMsg(M.SkipTimeoutTask)}
    "by M.ShowAllToolUi, send it ShowToolUi all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.ShowAllToolUi)
      testDrive1.expectMsg(M.ShowToolUi)
      testDrive2.expectMsg(M.ShowToolUi)}
    "by M.HideAllToolUi, send HideToolUi to all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.HideAllToolUi)
      testDrive1.expectMsg(M.HideToolUi)
      testDrive2.expectMsg(M.HideToolUi)}
  }
  "PlumbingActor shutdown" should{
    "shutdown in Init state" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      testController.watch(actors.plumbing)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Creating state" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      testController.watch(actors.plumbing)
      //Send BuildPlumbing
      testController.send(actors.plumbing, M.BuildPlumbing)
      //Construct first drive
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Construct second drive
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Building state" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      testController.watch(actors.plumbing)
      //Send BuildPlumbing
      testController.send(actors.plumbing, M.BuildPlumbing)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Build first drives
      testDrive1.expectMsg(M.BuildDrive)
      testDrive1.send(actors.plumbing, M.DriveBuilt)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Build second drives
      testDrive2.expectMsg(M.BuildDrive)
      testDrive2.send(actors.plumbing, M.DriveBuilt)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Built state" in new TestCase {
      //Preparing
      actors.builtPlumbing
      testController.watch(actors.plumbing)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Starting state" in new TestCase {
      //Preparing
      actors.builtPlumbing
      testController.watch(actors.plumbing)
      //Send StartPlumbing
      testController.send(actors.plumbing, M.StartPlumbing)
      //Start first drives
      testDrive1.expectMsg(M.StartDrive)
      testDrive1.send(actors.plumbing, M.DriveStarted)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Start second drives
      testDrive2.expectMsg(M.StartDrive)
      testDrive2.send(actors.plumbing, M.DriveStarted)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Work state" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Sopping state" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Send StopPlumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stopping first drive
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Stopping second drive
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
    "shutdown in Sopped state (normal)" in new TestCase {
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Send StopPlumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      testController.expectMsg(M.PlumbingStopped)
      //Send PlumbingShutdown
      testController.send(actors.plumbing, M.ShutdownPlumbing)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingShutdown and Terminated
      testController.expectMsg(M.PlumbingShutdown)
      testController.expectTerminated(actors.plumbing)}
  }
  "PlumbingActor failure" should{
    "failure in Building state" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      testController.watch(actors.plumbing)
      //Send BuildPlumbing
      testController.send(actors.plumbing, M.BuildPlumbing)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Build first drive (failed)
      testDrive1.expectMsg(M.BuildDrive)
      testDrive1.send(actors.plumbing, M.DriveError(testException1))
      testDrive1.send(actors.plumbing, M.DriveBuilt)
      //Build second drive
      testDrive2.expectMsg(M.BuildDrive)
      testDrive2.send(actors.plumbing, M.DriveBuilt)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingError and Terminated
      testController.expectMsgType[M.PlumbingError].errors shouldEqual Seq(testException1)
      testController.expectTerminated(actors.plumbing)}
    "failure in Starting state" in new TestCase {
      //Preparing
      actors.builtPlumbing
      testController.watch(actors.plumbing)
      //Send StartPlumbing
      testController.send(actors.plumbing, M.StartPlumbing)
      //Start first drives (failed)
      testDrive1.expectMsg(M.StartDrive)
      testDrive1.send(actors.plumbing, M.DriveError(testException1))
      testDrive1.send(actors.plumbing, M.DriveStarted)
      //Start second drives (failed)
      testDrive2.expectMsg(M.StartDrive)
      testDrive2.send(actors.plumbing, M.DriveError(testException2))
      testDrive2.send(actors.plumbing, M.DriveStarted)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingError and Terminated
      testController.expectMsgType[M.PlumbingError].errors.toSet shouldEqual Set(testException1, testException2)
      testController.expectTerminated(actors.plumbing)}
    "failure in Work state" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Send DriveError
      testDrive1.send(actors.plumbing, M.DriveError(testException1))
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingError and Terminated
      testController.expectMsgType[M.PlumbingError].errors shouldEqual Seq(testException1)
      testController.expectTerminated(actors.plumbing)}
    "failure in Sopping state" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(actors.plumbing)
      //Send StopPlumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stop drives
      testDrive1.expectMsg(M.StopDrive)
      testDrive1.send(actors.plumbing, M.DriveError(testException1))
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      testDrive2.send(actors.plumbing, M.DriveError(testException2))
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      testDrive1.testActor ! PoisonPill
      testDrive2.expectMsg(M.TerminateDrive)
      testDrive2.testActor ! PoisonPill
      //Expect PlumbingError and Terminated
      testController.expectMsgType[M.PlumbingError].errors.toSet shouldEqual Set(testException1, testException2)
      testController.expectTerminated(actors.plumbing)}
  }
}
