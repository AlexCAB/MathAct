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

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import mathact.core.ActorTestSpec
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike, PumpingConfigLike}
import mathact.core.model.messages.M
import mathact.core.plumbing.{Fitting, PumpLike}
import org.scalatest.Suite
import scala.concurrent.duration._


/** Testing of Pumping actor
  * Created by CAB on 30.08.2016.
  */

class PumpingTest extends ActorTestSpec{
  //Test model
  trait TestCase extends Suite{
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
    //Pumping
    object actors{
      lazy val pumping = system.actorOf(Props(
        new Pumping(testPumpingConfig, testController.ref,  "TestSketch", testUserLogging.ref, testVisualization.ref){
          override def createDriveActor(toolPump: PumpLike): (ActorRef, Int)  = {
            val index = toolPump.asInstanceOf[TestPump].index
            (List(testDrive1.ref, testDrive2.ref)(index),index + 1)}}),
        "Pumping_" + randomString())
      lazy val pumpingWithDrives = {
        testController.send(pumping, M.NewDrive(TestPump(0)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        testController.send(pumping, M.NewDrive(TestPump(1)))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        pumping}
      lazy val startedPumpingWithDrives = {
        pumpingWithDrives
        testController.send(pumping, M.StartPumping)
        testDrive1.expectMsg(M.BuildDrive)
        testDrive1.send(pumping, M.DriveBuilt)
        testDrive2.expectMsg(M.BuildDrive)
        testDrive2.send(pumping, M.DriveBuilt)
        testDrive1.expectMsg(M.StartDrive)
        testDrive1.send(pumping, M.DriveStarted)
        testDrive2.expectMsg(M.StartDrive)
        testDrive2.send(pumping, M.DriveStarted)
        testController.expectMsg(M.PumpingStarted)
        testVisualization.expectMsg(M.AllToolBuilt)
        pumpingWithDrives}}}
  //Testing
  "Pumping actor" should{
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
    "by M.StartPumping, build and start all drives, response M.PumpingStarted" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      //Start
      testController.send(actors.pumping, M.StartPumping)
      //Build drives
      testDrive1.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.pumping, M.DriveBuilt)
      testDrive2.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.pumping, M.DriveBuilt)
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
    "by M.StopPumping, stop and terminate all drives, response M.PumpingStopped and terminate" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.startedPumpingWithDrives)
      //Stop pumping
      testController.send(actors.pumping, M.StopPumping)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive1.send(actors.pumping, M.DriveTerminated)
      testDrive2.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive2.send(actors.pumping, M.DriveTerminated)
      //Built
      testController.expectMsg(M.PumpingStopped)
      //Terminate
      testController.expectTerminated(actors.startedPumpingWithDrives)}
    "by M.StopPumping at building, terminate all drives  at the end of building" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumpingWithDrives)
      //Start
      testController.send(actors.pumping, M.StartPumping)
      //Build 1 drives
      testDrive1.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.pumping, M.DriveBuilt)
      //Stop pumping
      testController.send(actors.pumping, M.StopPumping)
      //Build 1 drives
      testDrive2.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.pumping, M.DriveBuilt)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive1.send(actors.pumping, M.DriveTerminated)
      testDrive2.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive2.send(actors.pumping, M.DriveTerminated)
      //Built
      testController.expectMsg(M.PumpingStopped)
      //Terminate
      testController.expectTerminated(actors.pumpingWithDrives)}
    "by M.StopPumping at starting, stop and terminate all drives at the end of starting" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      testController.watch(actors.pumpingWithDrives)
      //Start
      testController.send(actors.pumping, M.StartPumping)
      //Build drives
      testDrive1.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.pumping, M.DriveBuilt)
      testDrive2.expectMsg(M.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.pumping, M.DriveBuilt)
      //Start 1 drives
      testDrive1.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive1.send(actors.pumping, M.DriveStarted)
      //Stop pumping
      testController.send(actors.pumping, M.StopPumping)
      //Start 2 drives
      testDrive2.expectMsg(M.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive2.send(actors.pumping, M.DriveStarted)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive1.send(actors.pumping, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive2.send(actors.pumping, M.DriveStopped)
      //Terminating drives
      testDrive1.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive1.send(actors.pumping, M.DriveTerminated)
      testDrive2.expectMsg(M.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive2.send(actors.pumping, M.DriveTerminated)
      //Built
      testController.expectMsg(M.PumpingStopped)
      //Terminate
      testController.expectTerminated(actors.pumpingWithDrives)}
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
}
