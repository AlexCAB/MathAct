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

package mathact.core.sketch.infrastructure.controller

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import mathact.core.bricks.data.SketchData
import mathact.core.dummies.{TestSketchWithError, TestSketchWithSmallTimeout}
import mathact.core.model.enums.SketchUIElement._
import mathact.core.model.enums.SketchUiElemState._
import mathact.core.ActorTestSpec
import mathact.core.model.config._
import mathact.core.model.messages.M
import org.scalatest.Suite
import akka.util.Timeout

import scala.concurrent.duration._


/** Workbench controller test
  * Created by CAB on 02.09.2016.
  */

class SketchControllerTest extends ActorTestSpec {
  //Test model
  protected trait TestCase extends Suite{
    //Test config
    val testMainConfig = new MainConfigLike{
      val config = ConfigFactory.load()
      val sketchInstance = new SketchInstanceConfigLike{
        val commonConfig = config
        val sketchBuildingTimeout = 5.second
        val pumpConfig = new PumpConfigLike{
          val askTimeout = Timeout(1.second)}}
      val plumbing = new PlumbingConfigLike{
        val drive = new DriveConfigLike{
          val pushTimeoutCoefficient = 0
          val startFunctionTimeout = 1.second
          val messageProcessingTimeout = 1.second
          val stopFunctionTimeout = 1.second
          val impellerMaxQueueSize = 0
          val uiOperationTimeout = 1.second}}
      val mainUI = null
      val sketchUI = null
      val userLogging = null
      val visualization = null}
    //Test SketchData
    def newTestSketchData(
      clazz: Class[_] = classOf[TestSketchWithSmallTimeout],
      autorun: Boolean = false,
      showUserLogUi: Boolean = false,
      showVisualisationUi: Boolean = false)
    :SketchData = SketchData(
        clazz,
        className = clazz.getName,
        sketchName = Some("TestSketch1"),
        sketchDescription = Some("Testing sketch 1"),
        autorun,
        showUserLogUi,
        showVisualisationUi)
    //Helpers actors
    def testAskMainController(workbenchController: ActorRef) = system.actorOf(Props(
      new Actor{
        def receive = {
          case M.NewSketchContext(workbench, sketchClassName) ⇒
            println(
              s"[SketchControllerTest.testAskMainController] Send GetSketchContext, " +
              s"sender: $sender, workbench: $workbench")
            workbenchController ! M.GetSketchContext(sender)
          case m ⇒
            println(s"[SketchControllerTest.testAskMainController] Unknown msg: $m")}}),
      "TestAskMainController_" + randomString())
    lazy val testActor = TestProbe("testActor_" + randomString())
    lazy val testMainController = TestProbe("TestMainController_" + randomString())
    lazy val testSketchUi = TestProbe("TestSketchUi_" + randomString())
    lazy val testUserLogging = TestProbe("TestUserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    lazy val testPlumbing = TestProbe("TestPlumbing_" + randomString())
    lazy val testSketchInstance = TestProbe("TestSketchInstance_" + randomString())
    //SketchControllerActor
    def newSketchController(sketch: SketchData): ActorRef = system.actorOf(Props(
      new SketchControllerActor(testMainConfig, sketch, testMainController.ref){
        val sketchUi = testSketchUi.ref
        val userLogging = testUserLogging.ref
        val visualization = testVisualization.ref
        val plumbing = testPlumbing.ref
        val sketchInstance = testSketchInstance.ref}),
      "SketchController_" + randomString())
    def newBuiltSketchController(): ActorRef = {
      val controller = newSketchController( newTestSketchData())
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testUserLogging.expectMsgType[M.LogInfo]
      testMainController.expectMsgType[M.SketchBuilt]
      controller}
    def newStartedSketchController(): ActorRef = {
      val controller = newBuiltSketchController()
      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.StartPlumbing)
      testPlumbing.send(controller, M.PlumbingStarted)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      controller}}
  //Testing
  "SketchControllerActor on start" should{
    "by SketchControllerStart, create sketch instance show UI, start plumbing with auto run on" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = true,
        showUserLogUi = true,
        showVisualisationUi = true))
      //Send start
      testMainController.send(controller, M.LaunchSketch)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemDisabled,
        HideAllBlocksUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemHide,
        VisualisationBtn → ElemHide)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Show user logging UI
      testUserLogging.expectMsg(M.ShowUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(LogBtn → ElemHide)
      //Show visualization UI
      testVisualization.expectMsg(M.ShowVisualizationUI)
      testVisualization.send(controller, M.VisualizationUIChanged(isShow = true))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(VisualisationBtn → ElemHide)
      //Creating of sketch instance
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testSketchInstance.expectMsgType[M.BuildSketchContextFor].actor shouldEqual testActor.ref
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      //Update user UI
      val titleStr = testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      println("[SketchControllerTest] titleStr " + titleStr)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Run plumbing
      testPlumbing.expectMsg(M.StartPlumbing)
      testPlumbing.send(controller, M.PlumbingStarted)
      //Update user UI
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        StopSketchBtn → ElemEnabled,
        ShowAllBlocksUiBtn → ElemEnabled,
        HideAllBlocksUiBtn → ElemEnabled,
        SkipAllTimeoutTaskBtn → ElemEnabled)
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      //Log info
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,Sketch 'TestSketch1' successfully built.)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      //Expect no messages
      sleep(3.second)
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)
      testSketchInstance.expectNoMsg(1.second)}
    "by PlumbingNoDrivesFound from blumbing, desable UI and wait for close" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData())
      //Send start
      testMainController.send(controller, M.LaunchSketch)
      //Show UI
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Creating of sketch instance
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testSketchInstance.expectMsgType[M.BuildSketchContextFor].actor shouldEqual testActor.ref
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      //Update user UI
      testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      //Build plumbing with PlumbingNoDrivesFound response
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingNoDrivesFound)
      //Update user UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemDisabled,
        HideAllBlocksUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      //Expect no messages
      sleep(3.second)
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)
      testSketchInstance.expectNoMsg(1.second)}
    "by SketchControllerStart, create sketch instance show UI, with auto run off" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout]))
      //Send start
      testMainController.send(controller, M.LaunchSketch)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemDisabled,
        HideAllBlocksUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Creating of sketch instance
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testSketchInstance.expectMsgType[M.BuildSketchContextFor].actor shouldEqual testActor.ref
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      //Update user UI
      val titleStr = testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      println("[SketchControllerTest] titleStr " + titleStr)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Update user UI
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemEnabled)
      //Log info
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      //Expect no messages
      sleep(3.second)
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)
      testSketchInstance.expectNoMsg(1.second)}
    "by SketchInstanceError, only update status string" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithError]))
      //Send start
      testMainController.send(controller, M.LaunchSketch)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemDisabled,
        HideAllBlocksUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Creating of sketch instance
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testSketchInstance.expectMsgType[M.BuildSketchContextFor].actor shouldEqual testActor.ref
      testSketchInstance.send(controller, M.SketchInstanceError(new Exception("Oops!!!")))
      testMainController.expectMsgType[M.SketchFail].className shouldEqual classOf[TestSketchWithError].getName
      //Sketch UI update
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Expect no messages
      sleep(3.second)
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)
      testSketchInstance.expectNoMsg(1.second)}
  }
  "SketchControllerActor in work" should{
    "by RunBtn hit, run sketch" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController()
      //Send start
      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
      //Start Plumbing
      testPlumbing.expectMsg(M.StartPlumbing)
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testPlumbing.send(controller, M.PlumbingStarted)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(    //On PlumbingStarted
        RunBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemEnabled,
        HideAllBlocksUiBtn → ElemEnabled,
        SkipAllTimeoutTaskBtn → ElemEnabled,
        StopSketchBtn → ElemEnabled)
      //Update status string
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      //Expect no messages
      sleep(3.second)
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)
      testSketchInstance.expectNoMsg(1.second)}
    "by handle buttons hits in Working mode" in new TestCase {
      //Preparing
      val controller = newStartedSketchController()
      //Hit ShowAllBlocksUiBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(ShowAllBlocksUiBtn, Unit))
      testPlumbing.expectMsg(M.ShowAllBlockUi)
      //Hit HideAllBlocksUiBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(HideAllBlocksUiBtn, Unit))
      testPlumbing.expectMsg(M.HideAllBlockUi)
      //Hit SkipAllTimeoutTaskBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, Unit))
      testPlumbing.expectMsg(M.SkipAllTimeoutTask)
      //Hit LogBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(LogBtn, ElemShow))
      testUserLogging.expectMsg(M.ShowUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        LogBtn → ElemHide)
      testSketchUi.send(controller, M.SketchUIActionTriggered(LogBtn, ElemHide))
      testUserLogging.expectMsg(M.HideUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = false))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        LogBtn → ElemShow)
      //Hit VisualisationBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(VisualisationBtn, ElemShow))
      testVisualization.expectMsg(M.ShowVisualizationUI)
      testVisualization.send(controller, M.VisualizationUIChanged(isShow = true))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        VisualisationBtn → ElemHide)
      testSketchUi.send(controller, M.SketchUIActionTriggered(VisualisationBtn, ElemHide))
      testVisualization.expectMsg(M.HideVisualizationUI)
      testVisualization.send(controller, M.VisualizationUIChanged(isShow = false))
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        VisualisationBtn → ElemShow)}
    "by StopSketchBtn hit, stop sketch" in new TestCase {
      //Preparing
      val controller = newStartedSketchController() //In Working mode
      //Send stop
      testSketchUi.send(controller, M.SketchUIActionTriggered(StopSketchBtn, Unit))
      //Sopping of plumbing
      testPlumbing.expectMsg(M.StopPlumbing)
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testPlumbing.send(controller, M.PlumbingStopped)
      //UI update
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllBlocksUiBtn → ElemDisabled,
        HideAllBlocksUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
    //Expect no messages
    sleep(3.second)
    testMainController.expectNoMsg(1.second)
    testSketchUi.expectNoMsg(1.second)
    testUserLogging.expectNoMsg(1.second)
    testVisualization.expectNoMsg(1.second)
    testPlumbing.expectNoMsg(1.second)
    testSketchInstance.expectNoMsg(1.second)}
  }
  "SketchControllerActor on shutdown (hit close button)" should{
    "on hit close button in Init , terminate on Built" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData())
      testMainController.watch(controller)
      //Life cycle
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))      //<---
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Creating, terminate on Built" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData())
      testMainController.watch(controller)
      //Life cycle
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))      //<---
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Constructing, terminate on Built" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData())
      testMainController.watch(controller)
      //Life cycle
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))      //<---
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Building, terminate on Built" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData())
      testMainController.watch(controller)
      //Life cycle
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchInstance.send(controller, M.SketchInstanceReady(new TestSketchWithSmallTimeout))
      testSketchUi.expectMsgType[M.UpdateSketchUITitle]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))      //<---
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Working, terminate on Ended" in new TestCase {
      //Preparing
      val controller = newStartedSketchController() //In Working mode
      testMainController.watch(controller)
      //Hit close
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      //Stopping
      testPlumbing.expectMsg(M.StopPlumbing)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.send(controller, M.PlumbingStopped)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Stopping, terminate on Ended" in new TestCase {
      //Preparing
      val controller = newStartedSketchController() //In Working mode
      testMainController.watch(controller)
      //Stopping
      testSketchUi.send(controller, M.SketchUIActionTriggered(StopSketchBtn, Unit))
      testPlumbing.expectMsg(M.StopPlumbing)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      //Hit close
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      //Send PlumbingStopped
      testPlumbing.send(controller, M.PlumbingStopped)
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Built , terminate immediately" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController() //In Working mode
      testMainController.watch(controller)
      //Hit close
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Ended, terminate immediately" in new TestCase {
      //Preparing
      val controller = newStartedSketchController() //In Working mode
      testMainController.watch(controller)
      //Stopping
      testSketchUi.send(controller, M.SketchUIActionTriggered(StopSketchBtn, Unit))
      testPlumbing.expectMsg(M.StopPlumbing)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.send(controller, M.PlumbingStopped)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      //Hit close
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      //Expect termination
      testMainController.expectMsgType[M.SketchDone].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      testMainController.expectTerminated(controller)}
    "on hit close button in Built | Ended and mode Fail, return SketchError" in new TestCase {
      val controller = newSketchController( newTestSketchData())
      testMainController.watch(controller)
      val error1 = new Exception("Oops!!!")
      //Life cycle
      testMainController.send(controller, M.LaunchSketch)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testSketchInstance.expectMsg(M.CreateSketchInstance)
      testSketchInstance.send(controller, M.SketchInstanceError(error1))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testMainController.expectMsgType[M.SketchFail]
      //Hit close
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      //Expect termination
      val sketchError = testMainController.expectMsgType[M.SketchError]
      sketchError.className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      sketchError.errors.head shouldEqual error1
      testMainController.expectTerminated(controller)}
  }
}
