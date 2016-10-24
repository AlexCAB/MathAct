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
import mathact.core.bricks.blocks.BlockLike
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike, PlumbingConfigLike}
import mathact.core.model.data.pipes.OutletData
import mathact.core.model.data.verification.{BlockVerificationData, InletVerificationData}
import mathact.core.model.messages.M
import mathact.core.plumbing.infrastructure.controller.PlumbingActor
import mathact.core.plumbing.PumpLike
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of PlumbingActor actor
  * Created by CAB on 30.08.2016.
  */

class PlumbingTest extends ActorTestSpec{
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
      val block: BlockLike = null
      val blockName = "TestBlock" + index
      val blockImagePath = None}
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
      lazy val plumbing = newRootChildActorOf(Props(
        new PlumbingActor(testPlumbingConfig, testController.ref,  "TestSketch", testUserLogging.ref, testVisualization.ref){
          override def createDriveActor(blockId: Int, blockPump: PumpLike): ActorRef  = {
            val actor = List(testDrive1.ref, testDrive2.ref)(blockPump.asInstanceOf[TestPump].index)
            context.watch(actor)
            actor}}),
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
        testDrive1.expectMsg(M.ConnectingDrive)
        testDrive1.send(actors.plumbing, M.DriveConnected)
        testDrive2.expectMsg(M.ConnectingDrive)
        testDrive2.send(actors.plumbing, M.DriveConnected)
        testDrive1.expectMsg(M.TurnOnDrive)
        testDrive1.send(actors.plumbing, M.DriveTurnedOn)
        testDrive2.expectMsg(M.TurnOnDrive)
        testDrive2.send(actors.plumbing, M.DriveTurnedOn)
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
        testVisualization.expectMsg(M.AllBlockBuilt)
        builtPlumbing}}}
  //Testing
  "PlumbingActor" should{
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
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Connecting drives
      testDrive1.expectMsg(M.ConnectingDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveConnected)
      testDrive2.expectMsg(M.ConnectingDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveConnected)
      //Turning on drives
      testDrive1.expectMsg(M.TurnOnDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveTurnedOn)
      testDrive2.expectMsg(M.TurnOnDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveTurnedOn)
      //Built
      testController.expectMsg(M.PlumbingBuilt)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PlumbingTest] logInfo: " + logInfo)}
    "verify graph structure after all drive constructed, and terminate if this wrong" in new TestCase {
      //Preparing
      actors.plumbingWithDrives
      val incorrectVerificationData1 = BlockVerificationData(
        blockId = 1,
        inlets = Seq(InletVerificationData(
          inletId = 1,
          publishers = Seq(OutletData(
            blockId = 2,
            blockDrive = null,
            blockName = "",
            pipeId = 12345,   //Not exist outlet
            pipeName = None)))),
        outlets = Seq())
      val verificationData2 = BlockVerificationData(
        blockId = 2,
        inlets = Seq(),
        outlets = Seq())
      //Send BuildPlumbing
      testController.send(actors.plumbing, M.BuildPlumbing)
      testController.watch(actors.plumbing)
      //Construct drives
      testDrive1.expectMsg(M.ConstructDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveConstructed)
      testDrive2.expectMsg(M.ConstructDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveConstructed)
      //Connecting drives
      testDrive1.expectMsg(M.ConnectingDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveConnected)
      testDrive1.send(actors.plumbing, M.DriveVerification(incorrectVerificationData1))
      testDrive2.expectMsg(M.ConnectingDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveConnected)
      testDrive2.send(actors.plumbing, M.DriveVerification(verificationData2))
      //Expect termination
      testController.expectTerminated(actors.plumbing)}
    "by M.StartPlumbing, build and start all drives, response M.PlumbingStarted" in new TestCase {
      //Preparing
      actors.builtPlumbing
      //Start
      testController.send(actors.plumbing, M.StartPlumbing)
      //Start drives
      testDrive1.expectMsg(M.StartDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveStarted)
      testDrive2.expectMsg(M.StartDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveStarted)
      //Built
      testController.expectMsg(M.PlumbingStarted)
      testVisualization.expectMsg(M.AllBlockBuilt)}
    "by M.StopPlumbing, stop all drives, response M.PlumbingStopped" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Stop plumbing
      testController.send(actors.plumbing, M.StopPlumbing)
      //Stopping drives
      testDrive1.expectMsg(M.StopDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveStopped)
      testDrive2.expectMsg(M.StopDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveStopped)
      //Turning off
      testDrive1.expectMsg(M.TurnOffDrive)
      sleep(1.second)
      testDrive1.send(actors.plumbing, M.DriveTurnedOff)
      testDrive2.expectMsg(M.TurnOffDrive)
      sleep(1.second)
      testDrive2.send(actors.plumbing, M.DriveTurnedOff)
      //Stopped
      testController.expectMsg(M.PlumbingStopped)
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[PlumbingTest] logInfo: " + logInfo)}
    "by M.SkipAllTimeoutTask, send SkipTimeoutTask to all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.SkipAllTimeoutTask)
      testDrive1.expectMsg(M.SkipTimeoutTask)
      testDrive2.expectMsg(M.SkipTimeoutTask)}
    "by M.ShowAllBlockUi, send it ShowBlockUi all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.ShowAllBlockUi)
      testDrive1.expectMsg(M.ShowBlockUi)
      testDrive2.expectMsg(M.ShowBlockUi)}
    "by M.HideAllBlockUi, send HideBlockUi to all drives" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      //Test
      testController.send(actors.plumbing, M.HideAllBlockUi)
      testDrive1.expectMsg(M.HideBlockUi)
      testDrive2.expectMsg(M.HideBlockUi)}
    "if some drive terminated, do terminate self" in new TestCase {
      //Preparing
      actors.startedPlumbingWithDrives
      testController.watch(testDrive1.testActor)
      testController.watch(testDrive2.testActor)
      testController.watch(actors.plumbing)
      //Terminate
      testDrive1.testActor ! PoisonPill
      testController.expectTerminated(testDrive1.testActor)
      testController.expectTerminated(actors.plumbing)
    }
  }
}
