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
import akka.pattern.ask
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import mathact.core.bricks.{WorkbenchLike, SketchContext}
import mathact.core.dummies.{TestSketchWithError, TestSketchWithBigTimeout, TestSketchWithSmallTimeout}
import mathact.core.model.enums.SketchUIElement._
import mathact.core.model.enums.SketchUiElemState._
import mathact.core.ActorTestSpec
import mathact.core.model.config._
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.messages.M
import mathact.core.sketch.infrastructure.controller.SketchControllerActor
import org.scalatest.Suite
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._


/** Workbench controller test
  * Created by CAB on 02.09.2016.
  */

class SketchControllerTest extends ActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val testMainConfig = new MainConfigLike{
      val config = ConfigFactory.load()
      val sketchBuildingTimeout = 5.second
      val plumbing = new PlumbingConfigLike{
        val pump = new PumpConfigLike{
          val askTimeout = Timeout(1.second) }
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
      autorun: Boolean,
      showUserLogUi: Boolean,
      showVisualisationUi: Boolean)
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
    //SketchControllerActor
    def newSketchController(sketch: SketchData): ActorRef = system.actorOf(Props(
      new SketchControllerActor(testMainConfig, sketch, testMainController.ref){
        val sketchUi = testSketchUi.ref
        val userLogging = testUserLogging.ref
        val visualization = testVisualization.ref
        val plumbing = testPlumbing.ref}),
      "SketchController_" + randomString())
    def newBuiltSketchController(): ActorRef = {
      val controller = newSketchController( newTestSketchData(
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.send(controller, M.StartSketchController)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, SketchContext]]
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testUserLogging.expectMsgType[M.LogInfo]
      testMainController.expectMsgType[M.SketchBuilt]
      controller}
    def newStartedSketchController(): ActorRef = {
      val controller = newBuiltSketchController()
      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testPlumbing.expectMsg(M.StartPlumbing)
      testPlumbing.send(controller, M.PlumbingStarted)
      testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testUserLogging.expectMsgType[M.LogInfo]
      controller}}
  //Testing
  "SketchControllerActor on start" should{
    "by SketchControllerStart, create sketch instance show UI, start plumbing with autorun on" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = true,
        showUserLogUi = true,
        showVisualisationUi = true))
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
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
      //Get context
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, SketchContext]]
      //Update user UI
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Update user UI
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemDisabled)
      //Run plumbing
      testPlumbing.expectMsg(M.StartPlumbing)
      testPlumbing.send(controller, M.PlumbingStarted)
      //Update user UI
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemEnabled,
        HideAllToolsUiBtn → ElemEnabled,
        SkipAllTimeoutTaskBtn → ElemEnabled,
        StopSketchBtn → ElemEnabled)
      //Log info
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,Sketch 'TestSketch1' successfully built.)
      val info2 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info2: " + info2) //LogInfo(None,Workbench,PlumbingActor started.)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      val statusStr5 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr5 " + statusStr5)
      //Run plumbing
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by SketchControllerStart, create sketch instance show UI, with autorun off" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Get context
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, SketchContext]]
      //Update user UI
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Update user UI
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemEnabled)
      //Log info
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt].className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      //Update status str
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      //Run plumbing
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by SketchControllerStart, response with error if not build in time" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithBigTimeout],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Wait for time out
      sleep(5.second)
      //Error log
      val error1 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchControllerActor] error1: " + error1)
      //Sketch UI update
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      //Sketch error
      testMainController.expectMsgType[M.SketchError]}
    "by SketchControllerStart, response with error if error on build" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithError],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Error log
      val error1 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchControllerActor] error1: " + error1)
      //Sketch UI update
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      //Sketch error
      testMainController.expectMsgType[M.SketchError]}
    "by SketchControllerStart, response with error if error on plumbing build" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = true,
        showUserLogUi = false,
        showVisualisationUi = false))
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Get context
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, SketchContext]]
      //Update user UI
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      ??? //testPlumbing.send(controller, M.PlumbingBuildingError)
      //Error log
      val error1 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchControllerActor] error1: " + error1)
      //Sketch UI update
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      //Sketch error
      testMainController.expectMsgType[M.SketchError]}
    "by GetSketchContext, create and return SketchContext" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      val askMainController = testAskMainController(controller)
      val askTimeout = 1.second
      //Start
      testMainController.send(controller, M.StartSketchController)
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Construct Workbench and do ask
      val workbench = new WorkbenchLike{
        val res: Either[Exception,SketchContext]  = Await.result(
          ask(
            askMainController,
            M.NewSketchContext(this, "sketch.class"))(askTimeout).mapTo[Either[Exception,SketchContext]],
          askTimeout)
        println("[SketchControllerTest.workbench] res: " + res)
        res.isRight shouldEqual true
        protected implicit val context: SketchContext = res.right.get}
      //UI update, log and built
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testUserLogging.expectMsgType[M.LogInfo]
      testMainController.expectMsgType[M.SketchBuilt]}
  }
  "SketchControllerActor in work" should{
    "by RunBtn hit, run sketch" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController()
      //Send start
      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
      //Run PlumbingActor
      testPlumbing.expectMsg(M.StartPlumbing)
      testPlumbing.send(controller, M.PlumbingStarted)
      //UI update
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemDisabled) //On StartPlumbing
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(    //On PlumbingStarted
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemEnabled,
        HideAllToolsUiBtn → ElemEnabled,
        SkipAllTimeoutTaskBtn → ElemEnabled,
        StopSketchBtn → ElemEnabled)
      //User log
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1)
      //Update status string
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      //Run plumbing
      testMainController.expectNoMsg(1.second)
      testSketchUi.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testVisualization.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by handle buttons hits in Working mode" in new TestCase {
      //Preparing
      val controller = newStartedSketchController()
      //Hit ShowAllToolsUiBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(ShowAllToolsUiBtn, Unit))
      testPlumbing.expectMsg(M.ShowAllToolUi)
      //Hit HideAllToolsUiBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(HideAllToolsUiBtn, Unit))
      testPlumbing.expectMsg(M.HideAllToolUi)
      //Hit SkipAllTimeoutTaskBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(SkipAllTimeoutTaskBtn, Unit))
      testPlumbing.expectMsg(M.SkipAllTimeoutTask)
      //Hit LogBtn
      testSketchUi.send(controller, M.SketchUIActionTriggered(LogBtn, ElemShow))
      testUserLogging.expectMsg(M.ShowUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
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
      val controller = newStartedSketchController()
      //Send stop
      testSketchUi.send(controller, M.SketchUIActionTriggered(StopSketchBtn, Unit))
      //Sopping of plumbing
      ??? //testPlumbing.expectMsg(M.StopAndTerminatePlumbing)
      ??? //testPlumbing.send(controller, M.PlumbingTerminated)
      //Log
      val info3 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info3: " + info3) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //UI update
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)}
  }
  "SketchControllerActor on shutdown" should{
    "stop in DriveCreating state" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = false,
        showUserLogUi = true,
        showVisualisationUi = true))
      testMainController.watch(controller)
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      //Show user logging UI
      testUserLogging.expectMsg(M.ShowUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      //Show visualization UI
      testVisualization.expectMsg(M.ShowVisualizationUI)
      testVisualization.send(controller, M.VisualizationUIChanged(isShow = true))
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      //Log
      val info2 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info2: " + info2)   //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //UI update
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn → ElemDisabled,
        VisualisationBtn → ElemDisabled)
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated]
      testMainController.expectTerminated(controller)}
    "stop in Building state" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.watch(controller)
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Get context
      testActor.send(controller, M.GetSketchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, SketchContext]]
      //Wait for controller switch to Building state
      sleep(1.second)
      //UI update
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      //Build plumbing
      testPlumbing.expectMsg(M.BuildPlumbing)
      testPlumbing.send(controller, M.PlumbingBuilt)
      //Update user UI
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemEnabled)
      //Log
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,Sketch 'TestSketch1' successfully built. Auto-run is off, hit 'play' button to start sketch.)
      //Wait for controller switch to Building state
      sleep(1.second)
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      val statusStr5 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr5 " + statusStr5)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn → ElemDisabled,
        VisualisationBtn → ElemDisabled)
      //Sketch built
      testMainController.expectMsgType[M.SketchBuilt]
      //Log
      val info2 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info2: " + info2) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      ??? // testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithSmallTimeout].getName
      val endData = testMainController.expectMsgType[M.SketchDone]
      endData.className shouldEqual className
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
    "stop in Built state" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController()
      testMainController.watch(controller)
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithSmallTimeout].getName
      val endData = testMainController.expectMsgType[M.SketchDone]
      endData.className shouldEqual classOf[TestSketchWithSmallTimeout].getName
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
    "stop in BuildingFailed state" in new TestCase {
      //Preparing
      val controller = newSketchController( newTestSketchData(
        clazz = classOf[TestSketchWithError],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.watch(controller)
      //Send start
      testMainController.send(controller, M.StartSketchController)
      //Show sketch UI
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn →  ElemShow,
        VisualisationBtn → ElemShow)
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      //Error log
      val error1 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchControllerActor] error1: " + error1) //LogError(None,Workbench,Some(java.lang.Exception: Oops!!),Exception on building of sketch.)
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn → ElemDisabled,
        VisualisationBtn → ElemDisabled)
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithError].getName
      val endData = testMainController.expectMsgType[M.SketchError]
      endData.className shouldEqual className
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
    "stop in Starting state" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController()
      testMainController.watch(controller)
      //Send Start
      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled)
      testPlumbing.expectMsg(M.StartPlumbing)
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //PlumbingActor started
      testPlumbing.send(controller, M.PlumbingStarted)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      val info2 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info2: " + info2) //LogInfo(None,Workbench,PlumbingActor started.)
      //Sopping of plumbing
      ??? //testPlumbing.expectMsg(M.StopAndTerminatePlumbing)
      ??? //testPlumbing.send(controller, M.PlumbingTerminated)
      //UI update
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      val statusStr5 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr5 " + statusStr5)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      //Log
      val info3 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info3: " + info3) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //UI update
      val statusStr6 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr6 " + statusStr6)
      val statusStr7 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr7 " + statusStr7)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn → ElemDisabled,
        VisualisationBtn → ElemDisabled)
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      val info4 = testUserLogging.expectMsg(M.TerminateUserLogging)
      println("[SketchControllerActor] info4: " + info4) //
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithSmallTimeout].getName
      val endData = testMainController.expectMsgType[M.SketchDone]
      endData.className shouldEqual className
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
    "stop in Working state" in new TestCase {
      //Preparing
      val controller = newStartedSketchController()
      testMainController.watch(controller)
      //Send stop
      testMainController.send(controller, M.ShutdownSketchController)
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //Sopping of plumbing
      ??? //testPlumbing.expectMsg(M.StopAndTerminatePlumbing)
      ??? //testPlumbing.send(controller, M.PlumbingTerminated)
      //UI update
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled)
      //Log
      val info3 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info3: " + info3) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      //UI update
      val statusStr4 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr4 " + statusStr4)
      val statusStr5 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr5 " + statusStr5)
      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
        RunBtn → ElemDisabled,
        ShowAllToolsUiBtn → ElemDisabled,
        HideAllToolsUiBtn → ElemDisabled,
        SkipAllTimeoutTaskBtn → ElemDisabled,
        StopSketchBtn → ElemDisabled,
        LogBtn → ElemDisabled,
        VisualisationBtn → ElemDisabled)
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      val info4 = testUserLogging.expectMsg(M.TerminateUserLogging)
      println("[SketchControllerActor] info4: " + info4) //
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithSmallTimeout].getName
      val endData = testMainController.expectMsgType[M.SketchDone]
      endData.className shouldEqual className
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
    "stop by hit of UI close button" in new TestCase {
      //Preparing
      val controller = newBuiltSketchController()
      testMainController.watch(controller)
      //Send close button hit
      testSketchUi.send(controller, M.SketchUIActionTriggered(CloseBtn, Unit))
      val info1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchControllerActor] info1: " + info1) //LogInfo(None,Workbench,The Shutdown signal received, sketch will terminated.)
      val statusStr1 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr1 " + statusStr1)
      val statusStr2 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr2 " + statusStr2)
      val statusStr3 = testSketchUi.expectMsgType[M.SetSketchUIStatusString]
      println("[SketchControllerTest] statusStr3 " + statusStr3)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      ??? //testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      ??? //testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      ??? //testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      val className = classOf[TestSketchWithSmallTimeout].getName
      val endData = testMainController.expectMsgType[M.SketchDone]
      endData.className shouldEqual className
      ??? //testMainController.expectMsgType[M.SketchControllerTerminated].className shouldEqual className
      testMainController.expectTerminated(controller)}
  }
}
