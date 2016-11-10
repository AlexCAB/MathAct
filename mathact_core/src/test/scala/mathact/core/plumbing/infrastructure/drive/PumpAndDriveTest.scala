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

package mathact.core.plumbing.infrastructure.drive

import java.util.concurrent.ExecutionException

import akka.actor.{PoisonPill, Terminated, Actor, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.core._
import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.ui.UIEvent
import mathact.core.dummies.TestActor
import mathact.core.gui.ui.BlockUILike
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike}
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.data.pipes.{OutletData, InletData}
import mathact.core.model.enums.VisualisationLaval
import mathact.core.model.holders._
import mathact.core.model.messages.M
import mathact.core.plumbing.Pump
import mathact.core.plumbing.fitting.flows.{InflowLike, OutflowLike}
import mathact.core.plumbing.fitting.life.{OnStopLike, OnStartLike}
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.BlockLike
import org.scalatest.Suite

import scala.concurrent.duration._
import scala.util.Try


/** Testing of Pump with DriveActor actor
  * Created by CAB on 15.08.2016.
  */

class PumpAndDriveTest extends ActorTestSpec{
  //Test model
  trait TestCase extends Suite{
    //Helpers definitions
    case class DriveState(
      outlets: Map[Int, (OutPipe[_], Option[Long], Map[(DriveRef, Int), Int])], // (Outlet ID, (Outlet, subscribers(id, inletQueueSize))
      inlets: Map[Int, (InPipe[_], Int)],    // (Inlet ID, Outlet, taskQueueSize)
      pendingConnections: Map[Int, M.ConnectPipes],
      pendingMessages:  List[(Int, Any)],
      visualisationLaval: VisualisationLaval)
    class TestHandler extends OutflowLike[Double] with InflowLike[Double]{
      //Variables
      private var opPipe: Option[OutPipe[Double]] = None
      private var receivedValues = List[Double]()
      private var procTimeout: Option[Duration] = None
      private var procError: Option[Throwable] = None
      //Set pipe
      private[core] def injectOutPipe(pipe: OutPipe[Double]): Unit = {opPipe = Some(pipe)}
      //Receive user message
      private[core] def processValue(value: Any): Unit = synchronized{
        println(
          s"[TestHandler] Do drain, value: $value, procTimeout: $procTimeout, " +
          s"procError: $procError, receivedValues: $receivedValues")
        receivedValues :+= value.asInstanceOf[Double]
        procTimeout.foreach(d ⇒ Thread.sleep(d.toMillis))
        procError.foreach(e ⇒ throw e)}
      //Test methods
      def sendValue(value: Double): Unit = {
        println(s"[TestHandler] Send value, value: $value, opPipe: $opPipe")
        opPipe.foreach(_.pushUserData(value))}
      def setProcTimeout(d: Duration): Unit = synchronized{ procTimeout = Some(d) }
      def setProcError(err: Option[Throwable]): Unit = synchronized{ procError = err }
      def getReceivedValues: List[Double] = synchronized{ receivedValues }}
    class TeatActor extends Actor {
      def receive = {
        case "Hi!" ⇒ sender ! "Hey!"
        case m ⇒ println("[TeatActor] m: " + m)}}
    class TestBlock extends BlockLike with OnStartLike with OnStopLike{
      //Parameters
      def blockName = Some(testBlockName)
      def blockImagePath = None
      //Variable
      @volatile private var onStartCalled = false
      @volatile private var onStopCalled = false
      @volatile private var procTimeout: Option[Duration] = None
      @volatile private var procError: Option[Throwable] = None
      //Pump
      val pump: Pump = new Pump(testSketchContext, this){}
      //Pipes
      val testHandler = new TestHandler
      lazy val outlet = new OutPipe(testHandler, Some("testOutlet"), pump)
      lazy val inlet = new InPipe(testHandler, Some("testInlet"), pump)
      //On start and stop
      private[core]  def doStart(): Unit = {
        println("[PumpAndDriveTest.testBlock] onStart called.")
        onStartCalled = true
        procTimeout.foreach(d ⇒ sleep(d))
        procError.foreach(e ⇒ throw e)}
      private[core]  def doStop(): Unit = {
        println("[PumpAndDriveTest.testBlock] onStop called.")
        onStopCalled = true
        procTimeout.foreach(d ⇒ sleep(d))
        procError.foreach(e ⇒ throw e)}
      //Helpers methods
      def setProcTimeout(d: Duration): Unit = { procTimeout = Some(d) }
      def setProcError(err: Option[Throwable]): Unit = { procError = err }
      def isOnStartCalled: Boolean = onStartCalled
      def isOnStopCalled: Boolean = onStopCalled}
    class TestBlockWithUI extends TestBlock with BlockUILike{
      //Variable
      @volatile private var initFrameCalled = false
      @volatile private var createFrameCalled = false
      @volatile private var showFrameUICallsCounter = 0
      @volatile private var hideFrameCallsCounter = 0
      @volatile private var closeFrameCounter = 0
      @volatile private var lastLayout: Option[(Int, Double, Double)] = None
      @volatile private var uiEventList = List[UIEvent]()
      @volatile private var uiProcTimeout: Option[Duration] = None
      @volatile private var uiProcError: Option[Throwable] = None
      //Functions
      private def waitOrFail(): Unit =  {
        uiProcTimeout.foreach(d ⇒ sleep(d))
        uiProcError.foreach(e ⇒ throw e)}
      //UI internal API
      private[core] def uiInit(): Unit = {
        initFrameCalled = true
        waitOrFail()}
      private[core] def uiCreate(): Unit = {
        createFrameCalled = true
        waitOrFail()}
      private[core] def uiShow(): Unit = {
        showFrameUICallsCounter += 1
        waitOrFail()}
      private[core] def uiHide(): Unit = {
        hideFrameCallsCounter += 1
        waitOrFail()}
      private[core] def uiClose(): Unit = {
        closeFrameCounter += 1
        waitOrFail()}
      private[core] def uiLayout(windowId: Int, x: Double, y: Double): Unit = {
        lastLayout = Some(Tuple3(windowId, x, y))
        waitOrFail()}
      private[core] def uiEvent(event: UIEvent): Unit = {
        uiEventList +:= event
        waitOrFail()}
      //Helpers methods
      def setUiProcTimeout(d: Duration): Unit = { uiProcTimeout = Some(d) }
      def setUiProcError(err: Option[Throwable]): Unit = { uiProcError = err }
      def isInitFrameCalled: Boolean = initFrameCalled
      def getLastUiLayout: Option[(Int, Double, Double)] = lastLayout
      def isCreateFrameCalled: Boolean = createFrameCalled
      def isShowFrameUICalled: Boolean = showFrameUICallsCounter != 0
      def getShowFrameUINumberOfCalls: Int = showFrameUICallsCounter
      def isHideFrameCalled: Boolean = hideFrameCallsCounter != 0
      def getHideFrameUINumberOfCalls: Int = hideFrameCallsCounter
      def isCloseFrameCalled: Boolean = closeFrameCounter != 0
      def getCloseFrameNumberOfCalls: Int = closeFrameCounter
      def getLastUiEvent: Option[UIEvent] = uiEventList.headOption
      def getAllUiEvent: List[UIEvent] = uiEventList.reverse}
    //Helpers values
    val testBlockId = randomInt()
    val testBlockName = "TestBlockName" + randomString(10)
    //Test config
    val testDriveConfig = new DriveConfigLike{
      val pushTimeoutCoefficient = 10
      val startFunctionTimeout = 4.second
      val messageProcessingTimeout = 4.second
      val stopFunctionTimeout = 4.second
      val impellerMaxQueueSize = 3
      val uiOperationTimeout = 2.second
      val uiSlowdownCoefficient = 10}
    val testPumpConfig = new PumpConfigLike{ val askTimeout = Timeout(4.second) }
    //Helpers actors
    lazy val testActor = TestProbe("TestActor_" + randomString())
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    lazy val testLayout = TestProbe("Layout_" + randomString())
    lazy val testPlumbing = TestActor("TestPlumbing_" + randomString())((self, context) ⇒ {
      case M.NewDrive(blockPump) ⇒ Some{ Right{
        val drive = context.actorOf(Props(
          new DriveActor(
            testDriveConfig,
            testBlockId,
            blockPump,
            LayoutRef(testLayout.ref),
            PlumbingRef(self),
            UserLoggingRef(testUserLogging.ref),
            VisualizationRef(testVisualization.ref))
          {
            //Get actor state
            override def receive: PartialFunction[Any, Unit]  = {
              case GetDriveState ⇒ sender ! DriveState(
                outlets = outlets
                  .map{ case (id, d) ⇒
                    (id, (d.pipe, d.pushTimeout, d.subscribers.values.map(s ⇒ (s.id, s.inletQueueSize)).toMap))}
                  .toMap,
                inlets = inlets
                  .map{ case (id, d) ⇒
                    (id, (d.pipe, d.taskQueue.size))}
                  .toMap,
                getConnectionsPendingList,
                getMessagesPendingList,
                visualisationLaval)
              case m ⇒ super.receive.apply(m)}}),
          "Drive_" + randomString())
        println(
          s"[PumpAndDriveTest.testPlumbing.NewDrive] Created of drive for " +
          s"block: ${blockPump.block.blockName}, drive: $drive")
        DriveRef(drive)}}})
    //Test workbench context
    lazy val testSketchContext = new SketchContext(
      system,
      SketchControllerRef(testController.ref),
      UserLoggingRef(testUserLogging.ref),
      LayoutRef(testLayout.ref),
      PlumbingRef(testPlumbing.ref),
      testPumpConfig,
      ConfigFactory.load())
    {
      override val plumbing = PlumbingRef(testPlumbing.ref)}}
  //Test blocks
  trait SimpleBlocks extends TestCase{
    object blocks{
      lazy val testBlock = new TestBlock
      lazy val testDrive = testBlock.pump.drive.ref
      lazy val otherDrive = TestActor("TestOtherDriver_" + randomString())((self, _) ⇒ {
        case M.AddOutlet(pipe, _) ⇒ Some(Right((0, 1)))  // (block ID, pipe ID)
        case M.AddInlet(pipe, _) ⇒  Some(Right((0, 2)))  // (block ID, pipe ID)
        case M.UserData(outletId, _) ⇒  Some(Right(None))})
      lazy val otherBlock = new BlockLike{
        //Parameters
        def blockName = Some("OtherBlock")
        def blockImagePath = None
        //Pump
        val pump: Pump = new Pump(testSketchContext, this){
          override val drive = DriveRef(otherDrive.ref)}
        //Pipes
        val otherHandler = new TestHandler
        lazy val outlet = new OutPipe(otherHandler, Some("otherOutlet"), pump)
        lazy val inlet = new InPipe(otherHandler, Some("otherInlet"), pump)}
      lazy val builtBlock = {
        testBlock
        testPlumbing.send(testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        testPlumbing.send(testBlock.pump.drive.ref, M.ConnectingDrive)
        testPlumbing.expectMsgType[M.DriveVerification]
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.BlockConstructedInfo]
        testPlumbing.send(testBlock.pump.drive.ref, M.TurnOnDrive)
        testPlumbing.expectMsg(M.DriveTurnedOn)
        testBlock.isOnStartCalled shouldEqual false
        testBlock}
      lazy val startedBlock = {
        builtBlock
        testPlumbing.send(testDrive, M.StartDrive)
        testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
        testUserLogging.expectMsgType[M.LogInfo]
        builtBlock}
      lazy val connectedBlocks = {
        //Preparing
        val testOutlet = testBlock.outlet.asInstanceOf[OutPipe[Double]]
        val testInlet = testBlock.inlet.asInstanceOf[InPipe[Double]]
        val otherOutlet = otherBlock.outlet.asInstanceOf[OutPipe[Double]]
        val otherInlet = otherBlock.inlet.asInstanceOf[InPipe[Double]]
        //Connecting
        testBlock.inlet.plug(otherBlock.outlet)
        testBlock.outlet.attach(otherBlock.inlet)
        //Send ConstructDrive
        testPlumbing.send(testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        //Process connections
        testPlumbing.send(testDrive, M.ConnectingDrive)
        val conMsg =  otherDrive.expectNMsg(2)
        val addCon = conMsg.getOneWithType[M.AddConnection]
        val inletData = InletData(DriveRef(otherDrive.ref), blockId = 0, None, addCon.inlet.inletId, addCon.inlet.inletName)
        otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, addCon.outlet, inletData))
        val conTo = conMsg.getOneWithType[M.ConnectTo]
        val outletData = OutletData(
          conTo.outlet.pump.drive, conTo.outlet.blockId, Some("otherOutlet"), otherOutlet.outletId, otherOutlet.outletName)
        otherDrive.send(conTo.initiator.ref, M.PipesConnected(conTo.connectionId, conTo.initiator, outletData, conTo.inlet))
        testPlumbing.expectMsgType[M.DriveVerification]
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.BlockConstructedInfo]
        //Turning on
        testPlumbing.send(testDrive, M.TurnOnDrive)
        testPlumbing.expectMsg(M.DriveTurnedOn)
        //Starting
        testPlumbing.send(testDrive, M.StartDrive)
        testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
        testUserLogging.expectMsgType[M.LogInfo]
        //Outlets and inlets model
        (testOutlet, testInlet, otherOutlet, otherInlet)}}}
  trait BlocksWithUi extends TestCase{
    object blocks{
      lazy val testBlockWithUi = new TestBlockWithUI
      lazy val testDrive = testBlockWithUi.pump.drive.ref
      lazy val otherDrive = TestActor("TestOtherDriver_" + randomString())((self, _) ⇒ {
        case M.AddOutlet(pipe, _) ⇒ Some(Right((0, 1)))  // (block ID, pipe ID)
        case M.AddInlet(pipe, _) ⇒  Some(Right((0, 2)))  // (block ID, pipe ID)
        case M.UserData(outletId, _) ⇒  Some(Right(None))})
      lazy val otherBlock = new BlockLike{
        //Parameters
        def blockName = Some("OtherBlock")
        def blockImagePath = None
        //Pump
        val pump: Pump = new Pump(testSketchContext, this){
          override val drive = DriveRef(otherDrive.ref)}
        //Pipes
        val otherHandler = new TestHandler
        lazy val outlet = new OutPipe(otherHandler, Some("otherOutlet"), pump)
        lazy val inlet = new InPipe(otherHandler, Some("otherInlet"), pump)}
      lazy val builtBlockWithUi = {
        testBlockWithUi
        testPlumbing.send(testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        testPlumbing.send(testBlockWithUi.pump.drive.ref, M.ConnectingDrive)
        testPlumbing.expectMsgType[M.DriveVerification]
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.BlockConstructedInfo]
        testPlumbing.send(testBlockWithUi.pump.drive.ref, M.TurnOnDrive)
        testPlumbing.expectMsg(M.DriveTurnedOn)
        testBlockWithUi.isOnStartCalled shouldEqual false
        testBlockWithUi}
      lazy val startedBlockWithUi = {
        builtBlockWithUi
        testPlumbing.send(testDrive, M.StartDrive)
        testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
        testUserLogging.expectMsgType[M.LogInfo]
        builtBlockWithUi}}}
  //Testing
  "On starting" should{
    "adding of Outlet and Inlet" in new SimpleBlocks{
      import blocks._
      //Preparing
      val outletId = testBlock.outlet.asInstanceOf[OutPipe[Double]].outletId
      val inletId = testBlock.inlet.asInstanceOf[InPipe[Double]].inletId
      otherBlock.outlet
      otherBlock.inlet
       //Testing
      val driveState = testDrive.askForState[DriveState]
      driveState.outlets should have size 1
      driveState.inlets should have size 1
      driveState.outlets.keys should contain (outletId)
      driveState.inlets.keys should contain (inletId)}
    "before ConnectingDrive, add new connections to pending list" in new SimpleBlocks {
      import blocks._
      //Preparing
      val testOutlet1 = testBlock.outlet
      val testInlet1 = testBlock.inlet
      val otherOutlet1 = otherBlock.outlet
      val otherInlet1 =otherBlock.inlet
      //Connecting and disconnecting
      testOutlet1.attach(otherInlet1)
      testInlet1.plug(otherOutlet1)
      //Testing
      val pendingCon = testDrive.askForState[DriveState].pendingConnections
      pendingCon should have size 2}
    "Not accept new inlets/outlets after ConstructDrive message" in new SimpleBlocks {
      import blocks._
      //Preparing
      testBlock
      //Send ConstructDrive
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Try to create inlet
      Try{testBlock.inlet}.toOption shouldBe empty
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      //Try to create outlet
      Try{testBlock.outlet}.toOption shouldBe empty
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)}
    "Not accept new connection after ConstructDrive message" in new SimpleBlocks {
      import blocks._
      //Preparing
      val testOutlet1 = testBlock.outlet
      val testInlet1 = testBlock.inlet
      val otherOutlet1 = otherBlock.outlet
      val otherInlet1 = otherBlock.inlet
      //Send ConstructDrive
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting and disconnecting
      Try{testOutlet1.attach(otherInlet1)}.toOption shouldBe empty
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      Try{testInlet1.plug(otherOutlet1)}.toOption shouldBe empty
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)}
    "by ConnectingDrive, connect from pending list and reply with DriveConnected (for 'plug')" in new SimpleBlocks {
      import blocks._
      //Preparing
      val outlet = otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = testBlock.inlet.asInstanceOf[InPipe[Double]]
      //Connecting (test block have inlet)
      testBlock.inlet.plug(otherBlock.outlet)
      testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(testDrive, M.ConnectingDrive)
      //Test wiring
      val connectTo = otherDrive.expectMsgType[M.ConnectTo]
      println(s"[PumpAndDriveTest] connectTo: $connectTo")
      connectTo.initiator        shouldEqual DriveRef(testDrive)
      connectTo.outlet.outletId  shouldEqual outlet.outletId
      connectTo.inlet.inletId    shouldEqual inlet.inletId
      //Send M.PipesConnected and expect M.DriveConnected
      val outletData = OutletData(outlet.pump.drive, outlet.blockId, None, outlet.outletId, outlet.outletName)
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      otherDrive.send(
        connectTo.initiator.ref,
        M.PipesConnected(connectTo.connectionId, connectTo.initiator, outletData, inletData))
      val verData = testPlumbing.expectMsgType[M.DriveVerification].verificationData
      println(s"[PumpAndDriveTest] verData: $verData")
      verData.blockId    shouldEqual testBlockId
      verData.inlets    should have size 1
      verData.inlets.head.inletId    shouldEqual inlet.inletId
      verData.inlets.head.publishers should have size 1
      verData.inlets.head.publishers.head.blockId   shouldEqual outlet.blockId
      verData.inlets.head.publishers.head.outletId shouldEqual outlet.outletId
      verData.outlets shouldBe empty
      testPlumbing.expectMsg(M.DriveConnected)
      //Check BlockInfo
      val builtInfo = testVisualization.expectMsgType[M.BlockConstructedInfo].builtInfo
      println(s"[PumpAndDriveTest] builtInfo: $builtInfo")
      builtInfo.blockId    shouldEqual testBlockId
      builtInfo.blockName  shouldEqual testBlockName
      builtInfo.blockImagePath shouldEqual None
      builtInfo.inlets    should have size 1
      builtInfo.inlets.head.blockId     shouldEqual testBlockId
      builtInfo.inlets.head.blockName   shouldEqual Some(testBlockName)
      builtInfo.inlets.head.inletId    shouldEqual inlet.inletId
      builtInfo.inlets.head.inletName  shouldEqual inlet.inletName
      builtInfo.outlets shouldBe empty
      //Check pending list
      sleep(500.millis) //Wait for processing of PipesConnected by testBlock
      testDrive.askForState[DriveState].pendingConnections should have size 0}
    "by ConnectingDrive, connect from pending list and reply with DriveConnected (for 'attach')" in new SimpleBlocks {
      import blocks._
      //Preparing
      val outlet = testBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = otherBlock.inlet.asInstanceOf[InPipe[Double]]
      //Connecting (test block have outlet)
      testBlock.outlet.attach(otherBlock.inlet)
      testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(testDrive, M.ConnectingDrive)
      //Test wiring
      val addConnection = otherDrive.expectMsgType[M.AddConnection]
      addConnection.initiator       shouldEqual DriveRef(testDrive)
      addConnection.inlet.inletId   shouldEqual inlet.inletId
      addConnection.outlet.outletId shouldEqual outlet.outletId
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      otherDrive.send(
        testDrive,
        M.ConnectTo(addConnection.connectionId, addConnection.initiator, outlet, inletData))
      //Expect DriveVerification, DriveConnected
      val verData = testPlumbing.expectMsgType[M.DriveVerification].verificationData
      println(s"[PumpAndDriveTest] verData: $verData")
      verData.blockId     shouldEqual testBlockId
      verData.inlets      shouldBe empty
      verData.outlets     should have size 1
      verData.outlets.head.outletId    shouldEqual outlet.outletId
      verData.outlets.head.subscribers should have size 1
      verData.outlets.head.subscribers.head.blockId shouldEqual inlet.blockId
      verData.outlets.head.subscribers.head.inletId  shouldEqual inlet.inletId
      testPlumbing.expectMsg(M.DriveConnected)
      //Check BlockConstructedInfo
      val builtInfo = testVisualization.expectMsgType[M.BlockConstructedInfo].builtInfo
      println(s"[PumpAndDriveTest] builtInfo: $builtInfo")
      builtInfo.blockId    shouldEqual testBlockId
      builtInfo.blockName  shouldEqual testBlockName
      builtInfo.blockImagePath shouldEqual None
      builtInfo.inlets    shouldBe empty
      builtInfo.outlets should have size 1
      builtInfo.outlets.head.blockId     shouldEqual testBlockId
      builtInfo.outlets.head.blockName   shouldEqual Some(testBlockName)
      builtInfo.outlets.head.outletId    shouldEqual outlet.outletId
      builtInfo.outlets.head.outletName  shouldEqual outlet.outletName
      //Check pendingConnections
      sleep(500.millis) //Wait for processing of PipesConnected by testBlock
      testDrive.askForState[DriveState].pendingConnections should have size 0}
    "put user messages to pending list and send it after turned on" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (v1,v2,v3,v4,v5) = (randomDouble(), randomDouble(), randomDouble(), randomDouble(), randomDouble())
      val outlet = testBlock.outlet.asInstanceOf[OutPipe[Double]]  //Creating of test block with outlet
      val inlet = otherBlock.inlet.asInstanceOf[InPipe[Double]]
      testBlock.outlet.attach(otherBlock.inlet)
      testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send in Init
      testBlock.testHandler.sendValue(v1)
      //Construct
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send in Constructed
      testBlock.testHandler.sendValue(v2)
      //Connecting
      testPlumbing.send(testDrive, M.ConnectingDrive)
      val addCon = otherDrive.expectMsgType[M.AddConnection]
      //Send in Connecting
      testBlock.testHandler.sendValue(v3)
      //Connected
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet, inletData))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send in Connected
      testBlock.testHandler.sendValue(v4)
      //Check pending list
      val driveState = testDrive.askForState[DriveState]
      driveState.pendingMessages should have size 4
      driveState.pendingMessages
        .map(_._2.asInstanceOf[Double])
        .zip(List(v1,v2,v3,v4,v5))
        .foreach{ case (av, bv) ⇒ av shouldEqual bv }}
    "by TurnOnDrive, send all messages from pending list" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (v1,v2,v3) = (randomDouble(), randomDouble(), randomDouble())
      val outlet = testBlock.outlet.asInstanceOf[OutPipe[Double]]  //Creating of test block with outlet
      val inlet = otherBlock.inlet.asInstanceOf[InPipe[Double]]
      testBlock.outlet.attach(otherBlock.inlet)
      testDrive.askForState[DriveState].pendingConnections should have size 1
      //Construct
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting
      testPlumbing.send(testDrive, M.ConnectingDrive)
      val addCon = otherDrive.expectMsgType[M.AddConnection]
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet, inletData))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send to pending list
      testBlock.testHandler.sendValue(v1)
      testBlock.testHandler.sendValue(v2)
      testBlock.testHandler.sendValue(v3)
      //Turning on
      testPlumbing.send(testDrive, M.TurnOnDrive)
      //Expect user messages to be sent
      otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v1
      otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v2
      otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v3
      //Expect
      testPlumbing.expectMsg(M.DriveTurnedOn)}
    "by StartDrive, run user init function and reply with DriveStarted" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      //Test
      testPlumbing.send(testDrive, M.StartDrive)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      testBlock.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      testBlock.setProcTimeout(5.second)
      //Test
      testPlumbing.send(testDrive, M.StartDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      testBlock.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStarted" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      testBlock.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(testDrive, M.StartDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      testBlock.isOnStartCalled shouldEqual true}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      testBlock.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(testDrive, M.StartDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      testBlock.isOnStartCalled shouldEqual true}
    "on error of building do terminate" in new SimpleBlocks {
      import blocks._
      //Preparing
      testPlumbing.watch(testDrive)
      val testOutlet = testBlock.outlet.asInstanceOf[OutPipe[Double]]
      val otherOutlet = otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = otherBlock.inlet.asInstanceOf[InPipe[Double]]
      testBlock.outlet.attach(otherBlock.inlet)
      //Construct
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(testDrive, M.ConnectingDrive)
      //Test wiring with incorrect inletId in response
      val addCon = otherDrive.expectMsgType[M.AddConnection]
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, otherOutlet, inletData)) //Incorrect inletId
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual testDrive}
  }
  "On user message" should{
    "by call pour(value), send UserData, to all inlets of connected drives" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Call pour(value) for other block
      otherBlock.otherHandler.sendValue(value1)
      val userData = otherDrive.getProcessedMessages.getOneWithType[M.UserData[Double]]
      println("[PumpAndDriveTest] userData: " + userData)
      userData.outletId shouldEqual otherOutlet.outletId
      userData.value    shouldEqual value1
      //Call pour(value) test block
      testBlock.testHandler.sendValue(value2)
      val userMessage = otherDrive.expectMsgType[M.UserMessage[Double]]
      println("[PumpAndDriveTest] userMessage: " + userMessage)
      userMessage.outletId shouldEqual testOutlet.outletId
      userMessage.inletId  shouldEqual otherInlet.inletId
      userMessage.value    shouldEqual value2}
    "process UserMessage in Connected | TurnedOn | Starting | Working | Stopping | Stopped" in new SimpleBlocks {
      import blocks._
      //Preparing
      val outlet = otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = testBlock.inlet.asInstanceOf[InPipe[Double]]
      testBlock.inlet.plug(otherBlock.outlet)
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      testPlumbing.send(testDrive, M.ConnectingDrive)
      val connectTo = otherDrive.expectMsgType[M.ConnectTo]
      connectTo.initiator       shouldEqual DriveRef(testDrive)
      connectTo.outlet.outletId shouldEqual outlet.outletId
      connectTo.inlet.inletId   shouldEqual inlet.inletId
      val outletData = OutletData(outlet.pump.drive, outlet.blockId, None, outlet.outletId, outlet.outletName)
      otherDrive.send(
        connectTo.initiator.ref,
        M.PipesConnected(connectTo.connectionId,connectTo.initiator, outletData, connectTo.inlet))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Helpers
      def testMsgProcessing(): Unit = {
        //Preparing
        val value = randomDouble()
        //Sending (with no load message returned)
        otherDrive.send(testDrive, M.UserMessage(outlet.outletId, inlet.inletId, value))
        otherDrive.expectNoMsg(2.seconds)
        testBlock.testHandler.getReceivedValues.contains(value) shouldEqual true}
      //Testing in Connected
      testMsgProcessing()
      //Testing in TurnedOn
      testPlumbing.send(testDrive, M.TurnOnDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveTurnedOn)
      testMsgProcessing()
      sleep(1.second) //Wait for processing
      //Testing in Starting
      testPlumbing.send(testDrive, M.StartDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
      //Testing in Working
      testMsgProcessing()
      //Testing in Stopping
      testPlumbing.send(testDrive, M.StopDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStopped)
      //Testing in Stopped
      testMsgProcessing()}
    "by UserMessage, processing of messages with no load response" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      testBlock.testHandler.setProcTimeout(1.second)
      //Send first messages
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      otherDrive.expectNoMsg(2.seconds)
      testBlock.testHandler.getReceivedValues.size shouldEqual 1
      testBlock.testHandler.getReceivedValues.head shouldEqual value1
      //Send second messages
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      otherDrive.expectNoMsg(2.seconds)
      testBlock.testHandler.getReceivedValues.size shouldEqual 2
      testBlock.testHandler.getReceivedValues(1) shouldEqual value2}
    "by UserMessage, processing of messages with load response" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      testBlock.testHandler.setProcTimeout(2.second)
      //Send message
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      otherDrive.expectNoMsg(1.seconds)
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      //Load messages
      val driveLoad1 = otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.subscriberId shouldEqual Tuple2(DriveRef(testDrive), testInlet.inletId)
      driveLoad1.outletId shouldEqual otherOutlet.outletId
      driveLoad1.inletQueueSize shouldEqual 1
      sleep(1.second) //Wait for end processing of first message
      val driveLoad2 = otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.subscriberId shouldEqual Tuple2(DriveRef(testDrive), testInlet.inletId)
      driveLoad2.outletId shouldEqual otherOutlet.outletId
      driveLoad2.inletQueueSize shouldEqual 0
      //Check of message processing
      sleep(3.seconds) //Wait for second messages will processed
      testBlock.testHandler.getReceivedValues.size shouldEqual 2
      testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)}
    "by UserMessage, in case message processing time out send warning to user logger" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val value1 = randomDouble()
      testBlock.testHandler.setProcTimeout(5.second)
      //Send message
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      sleep(3.seconds) //Wait for messages long timeout
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      //Check of message processing
      sleep(1.second) //Wait for second messages will processed
      testBlock.testHandler.getReceivedValues.size shouldEqual 1
      testBlock.testHandler.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, in case message processing error send error to user logger" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Send and get error
      testBlock.testHandler.setProcError(Some(new Exception("Oops!!!")))
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.seconds) //Wait for second messages will processed
      testBlock.testHandler.getReceivedValues.size shouldEqual 1
      testBlock.testHandler.getReceivedValues shouldEqual List(value1)
      //Send and not get error
      testBlock.testHandler.setProcError(None)
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      sleep(1.seconds) //Wait for second messages will processed
      testBlock.testHandler.getReceivedValues.size shouldEqual 2
      testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)}
    "by DriveLoad, evaluate message handling timeout" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      val subId = (DriveRef(otherDrive.ref), otherInlet.inletId)
      val queueSize = randomInt(100, 1000)
      //Test for first message
      otherDrive.send(testDrive, M.DriveLoad(subId, testOutlet.outletId, queueSize))
      sleep(1.second) //Wait for processing
      testDrive.askForState[DriveState].outlets(testOutlet.outletId)._3(subId) shouldEqual queueSize
      val pushTimeout1 = testDrive.askForState[DriveState].outlets(testOutlet.outletId)._2
      pushTimeout1 shouldEqual  Some(queueSize * testDriveConfig.pushTimeoutCoefficient)
      //Test for second message
      otherDrive.send(testDrive, M.DriveLoad(subId, testOutlet.outletId, 0))
      sleep(1.second) //Wait for processing
      testDrive.askForState[DriveState].outlets(testOutlet.outletId)._3(subId) shouldEqual 0
      val pushTimeout2 = testDrive.askForState[DriveState].outlets(testOutlet.outletId)._2
      pushTimeout2 shouldEqual None}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      testBlock.testHandler.setProcTimeout(7.second)
      val value1 = randomDouble()
      //Send message
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      sleep(1.seconds) //Small timeout
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.second) //Wait for second messages will processed
      testBlock.testHandler.getReceivedValues.size shouldEqual 1
      testBlock.testHandler.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, terminate in case incorrect inlet ID" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (_, testInlet, otherOutlet, _) = connectedBlocks
      testPlumbing.watch(testDrive)
      //Send user messages
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, 123456789, randomDouble())) //Incorrect inlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual testDrive}
    "by DriveLoad, terminate in case incorrect outlet ID" in new SimpleBlocks {
      import blocks._
      //Preparing
      val (_, _, _, otherInlet) = connectedBlocks
      testPlumbing.watch(testDrive)
      val subId = (DriveRef(otherDrive.ref), otherInlet.inletId)
      val queueSize = randomInt(100, 1000)
      //Send DriveLoad
      otherDrive.send(testDrive, M.DriveLoad(subId, 123456789, queueSize)) //Incorrect outlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual testDrive}
  }
  "On stopping" should{
    "by StopDrive, run user stop function and reply with DriveStopped" in new SimpleBlocks {
      import blocks._
      //Preparing
      connectedBlocks
      //Test
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      testBlock.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new SimpleBlocks {
      import blocks._
      //Preparing
      connectedBlocks
      testBlock.setProcTimeout(5.second)
      //Test
      testPlumbing.send(testDrive, M.StopDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      testBlock.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskFailed send LogError to user logging actor and resp DriveStopped" in new SimpleBlocks {
      import blocks._
      //Preparing
      connectedBlocks
      testBlock.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(testDrive, M.StopDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      testBlock.isOnStopCalled shouldEqual true}
    "by TurnOffDrive, stop receive new user msgs, wait for empty queues, and resp DriveTurnedOff " in new SimpleBlocks {
      import blocks._
      //Preparing
      val value1 = randomDouble()
      val value2 = randomDouble()
      val (testOutlet, testInlet, otherOutlet, otherInlet) = connectedBlocks
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      testBlock.isOnStopCalled shouldEqual true
      testBlock.testHandler.setProcTimeout(3.second)
      testPlumbing.send(testDrive, M.DriveStopped)
      //Send two slow messages
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      val driveLoad1 = otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.inletQueueSize shouldEqual 1
      //Send TurnOffDrive
      testPlumbing.send(testDrive, M.TurnOffDrive)
      sleep(1.second) //Wait for will processed
      //Send two messages, which will not processed
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, -1))
      otherDrive.send(testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, -2))
      otherDrive.expectNoMsg(1.second)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      //The first slow message processed
      val driveLoad2 = otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.inletQueueSize shouldEqual 0
      testPlumbing.expectMsg(M.DriveTurnedOff)(6.seconds)
      //Test received
      testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)
      //Expect no more messages
      testUserLogging.expectNoMsg(3.second)
      otherDrive.expectNoMsg(3.second)
      testPlumbing.expectNoMsg(3.second)}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new SimpleBlocks {
      import blocks._
      //Preparing
      connectedBlocks
      testBlock.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(testDrive, M.StopDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      testBlock.isOnStopCalled shouldEqual true}
  }
  "In work" should{
    "by SetVisualisationLaval, update visualisation laval" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      val newVisualisationLaval = randomVisualisationLaval()
      //Send
      testPlumbing.send(testDrive, M.SetVisualisationLaval(newVisualisationLaval))
      //Check
      testDrive.askForState[DriveState].visualisationLaval shouldEqual newVisualisationLaval}
  }
  "For block with UI" should{
    "call uiInit() on building of drive" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(3.second)
      //Check
      testBlockWithUi.isInitFrameCalled shouldEqual true
      testBlockWithUi.isCreateFrameCalled shouldEqual false
      testBlockWithUi.isShowFrameUICalled shouldEqual false
      testBlockWithUi.isHideFrameCalled shouldEqual false
      testBlockWithUi.isCloseFrameCalled shouldEqual false}
    "log error if uiInit() timeout" in new BlocksWithUi {
      import blocks._
      //Preparing
      testBlockWithUi.setUiProcTimeout(3.second)
      //Building
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isInitFrameCalled shouldEqual true}
    "log error if uiInit() fail" in new BlocksWithUi {
      import blocks._
      //Preparing
      testBlockWithUi.setUiProcError(Some(new Exception("Oops!")))
      //Building
      testPlumbing.send(testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isInitFrameCalled shouldEqual true}
    "call uiCreate() on starting of drive" in new BlocksWithUi {
      import blocks._
      //Preparing
      startedBlockWithUi
      //Check
      testBlockWithUi.isInitFrameCalled shouldEqual true
      testBlockWithUi.isCreateFrameCalled shouldEqual true
      testBlockWithUi.isShowFrameUICalled shouldEqual false
      testBlockWithUi.isHideFrameCalled shouldEqual false
      testBlockWithUi.isCloseFrameCalled shouldEqual false}
    "log error if uiCreate() timeout" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(3.second)
      //Staring
      testPlumbing.send(testDrive, M.StartDrive)
      testPlumbing.expectMsg(M.DriveStarted)
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isCreateFrameCalled shouldEqual true
      testUserLogging.expectMsgType[M.LogInfo]}
    "log error if uiCreate() fail" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcError(Some(new Exception("Oops!")))
      //Staring
      testPlumbing.send(testDrive, M.StartDrive)
      testPlumbing.expectMsg(M.DriveStarted)
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isCreateFrameCalled shouldEqual true
      testUserLogging.expectMsgType[M.LogInfo]}
    "call uiLayout() on M.SetWindowPosition" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      val upMsg = M.SetWindowPosition(1, randomDouble(), randomDouble())
      //Send
      testLayout.send(testDrive, upMsg)
      testLayout.expectMsgType[M.WindowPositionUpdated].windowId shouldEqual 1
      //Check
      testBlockWithUi.getLastUiLayout shouldEqual Some(Tuple3(upMsg.windowId, upMsg.x, upMsg.y))}
    "log error if uiLayout() timeout" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(3.second)
      //Send
      testLayout.send(testDrive, M.SetWindowPosition(1, 0, 0))
      sleep(1.second)
      testLayout.expectMsgType[M.WindowPositionUpdated].windowId shouldEqual 1
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.getLastUiLayout shouldEqual Some(Tuple3(1, 0, 0))}
    "log error if uiLayout() fail" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcError(Some(new Exception("Oops!")))
      //Send
      testLayout.send(testDrive, M.SetWindowPosition(1, 0, 0))
      testLayout.expectMsgType[M.WindowPositionUpdated].windowId shouldEqual 1
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.getLastUiLayout shouldEqual Some(Tuple3(1, 0, 0))}
    "pass messages to LayoutActor on calling of pump methods" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      val windowId = randomInt()
      val state = WindowState(
        isShown = randomBoolean(),
        x = randomDouble(),
        y = randomDouble(),
        h = randomDouble(),
        w = randomDouble(),
        title = "Test block")
      val prefs = WindowPreference(
        prefX = randomOpt(randomDouble()),
        prefY = randomOpt(randomDouble()))
      //Test for registerWindow
      testBlockWithUi.pump.registerWindow(windowId, state, prefs)
      val registerMsg = testLayout.expectMsgType[M.RegisterWindow]
      registerMsg.windowId shouldEqual windowId
      registerMsg.state shouldEqual state
      registerMsg.prefs shouldEqual prefs
      //Test for windowUpdated
      testBlockWithUi.pump.windowUpdated(windowId, state)
      val updatedMsg = testLayout.expectMsgType[M.WindowUpdated]
      updatedMsg.windowId shouldEqual windowId
      updatedMsg.state shouldEqual state
      //Test for layoutWindow
      testBlockWithUi.pump.layoutWindow(windowId)
      val layoutMsg = testLayout.expectMsgType[M.LayoutWindow]
      layoutMsg.windowId shouldEqual windowId}
    "call uiShow() and uiHide() by M.ShowBlockUi and M.HideBlockUi" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      //Test for M.ShowBlockUi
      testPlumbing.send(testDrive, M.ShowBlockUi)
      sleep(1.second) //Wait for receiving
      testBlockWithUi.isShowFrameUICalled shouldEqual true
      //Test for M.HideBlockUi
      testPlumbing.send(testDrive, M.HideBlockUi)
      sleep(1.second) //Wait for receiving
      builtBlockWithUi.isHideFrameCalled shouldEqual true}
    "call uiShow() and uiHide() and log error on timeout" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(3.second)
      //Test for M.ShowBlockUi
      testPlumbing.send(testDrive, M.ShowBlockUi)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      testBlockWithUi.isShowFrameUICalled shouldEqual true
      //Test for M.HideBlockUi
      testPlumbing.send(testDrive, M.HideBlockUi)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      builtBlockWithUi.isHideFrameCalled shouldEqual true}
    "call uiShow() and uiHide() and log error on fail" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcError(Some(new Exception("Oops!")))
      //Test for M.ShowBlockUi
      testPlumbing.send(testDrive, M.ShowBlockUi)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      testBlockWithUi.isShowFrameUICalled shouldEqual true
      //Test for M.HideBlockUi
      testPlumbing.send(testDrive, M.HideBlockUi)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      builtBlockWithUi.isHideFrameCalled shouldEqual true}
    "not execute uiShow() and uiHide() twice" in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(1.second)
      //Test for M.ShowBlockUi
      testPlumbing.send(testDrive, M.ShowBlockUi)
      testPlumbing.send(testDrive, M.ShowBlockUi)
      testPlumbing.send(testDrive, M.ShowBlockUi)
      sleep(4.second)
      builtBlockWithUi.getShowFrameUINumberOfCalls shouldEqual 1
      //Test for M.HideBlockUi
      testPlumbing.send(testDrive, M.HideBlockUi)
      testPlumbing.send(testDrive, M.HideBlockUi)
      testPlumbing.send(testDrive, M.HideBlockUi)
      sleep(4.second)
      builtBlockWithUi.getHideFrameUINumberOfCalls shouldEqual 1
      //Test next for M.HideBlockUi
      testPlumbing.send(testDrive, M.ShowBlockUi)
      sleep(2.second)
      builtBlockWithUi.getShowFrameUINumberOfCalls shouldEqual 2
      //Test next for M.HideBlockUi
      testPlumbing.send(testDrive, M.HideBlockUi)
      sleep(2.second)
      builtBlockWithUi.getHideFrameUINumberOfCalls shouldEqual 2}
    "call uiClose() on drive stopping" in new BlocksWithUi {
      import blocks._
      //Preparing
      startedBlockWithUi
      //Stop
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      //Check
      testBlockWithUi.isCloseFrameCalled shouldEqual true}
    "log error if uiClose() timeout" in new BlocksWithUi {
      import blocks._
      //Preparing
      startedBlockWithUi.setUiProcTimeout(3.second)
      //Stopping
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      testUserLogging.expectMsgType[M.LogInfo]
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isCloseFrameCalled shouldEqual true}
    "log error if uiClose() fail" in new BlocksWithUi {
      import blocks._
      //Preparing
      startedBlockWithUi.setUiProcError(Some(new Exception("Oops!")))
      //Stopping
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      testUserLogging.expectMsgType[M.LogInfo]
      //Check
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testBlockWithUi.isCloseFrameCalled shouldEqual true}
    "pass UI event via impeller"  in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      //Test 1
      val e1 = new UIEvent{}
      testBlockWithUi.pump.sendUiEvent(e1)
      sleep(1.second) //Wait for receiving
      testBlockWithUi.getLastUiEvent shouldEqual Some(e1)
      //Test 2
      val e2 = new UIEvent{}
      testBlockWithUi.pump.sendUiEvent(e2)
      sleep(1.second) //Wait for receiving
      testBlockWithUi.getLastUiEvent shouldEqual Some(e2)}
    "if UI event handling failed, log error to user logger"  in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      //Test error
      val e1 = new UIEvent{}
      testBlockWithUi.setUiProcError(Some(new Exception("Oops!!! UI event fail.")))
      testBlockWithUi.pump.sendUiEvent(e1)
      sleep(1.second) //Wait for receiving
      testBlockWithUi.getLastUiEvent shouldEqual Some(e1)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      //Test normal
      val e2 = new UIEvent{}
      testBlockWithUi.setUiProcError(None)
      testBlockWithUi.pump.sendUiEvent(e2)
      sleep(1.second) //Wait for receiving
      testBlockWithUi.getLastUiEvent shouldEqual Some(e2)
    }
    "if UI event timeout, log warnings"  in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi
      testBlockWithUi.setUiProcTimeout(7.seconds)
      val e1 = new UIEvent{}
      //Test logging
      testBlockWithUi.pump.sendUiEvent(e1)
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)
      val logWarning3 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning3: " + logWarning3)
      //Test processing
      sleep(2.second) //Wait for receiving
      testBlockWithUi.getLastUiEvent shouldEqual Some(e1)}
    "put UI event to queue and slow down a sender thread"  in new BlocksWithUi {
      import blocks._
      //Preparing
      builtBlockWithUi.setUiProcTimeout(500.millis)
      val startTime = System.currentTimeMillis()
      val sendEvents = (0 to 20).map{ i ⇒
        val e = new UIEvent{}
        testBlockWithUi.pump.sendUiEvent(e)
        (System.currentTimeMillis() >= (i * testDriveConfig.uiSlowdownCoefficient + startTime)) shouldEqual true
        e}
      sleep(10.second) //Wait for all events will processed
      builtBlockWithUi.getAllUiEvent shouldEqual sendEvents.toList}
    "on termination if UI not closed call uiClose()" in new BlocksWithUi {
      import blocks._
      //Test
      testPlumbing.watch(testDrive)
      testBlockWithUi.getCloseFrameNumberOfCalls shouldEqual 0
      testDrive ! PoisonPill
      testPlumbing.expectMsgType[Terminated].actor shouldEqual testDrive
      sleep(1.second) //Wait for cleanup processing
      testBlockWithUi.getCloseFrameNumberOfCalls shouldEqual 1}
    "on termination not do double call of uiClose()" in new BlocksWithUi {
      import blocks._
      //Preparing
      startedBlockWithUi
      testPlumbing.watch(testDrive)
      //Stop
      testPlumbing.send(testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      //Check after stop
      testBlockWithUi.getCloseFrameNumberOfCalls shouldEqual 1
      //Terminate
      testDrive ! PoisonPill
      testPlumbing.expectMsgType[Terminated].actor shouldEqual testDrive
      sleep(1.second) //Wait for cleanup processing
      //Check after terminate
      testBlockWithUi.getCloseFrameNumberOfCalls shouldEqual 1}
  }
  "Service methods" should{
    "re send user logging to logger actor" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      val infoMsg = M.UserLogInfo(message = randomString())
      val warnMsg = M.UserLogWarn(message = randomString())
      val errorMsg = M.UserLogError(error = Some(new Exception("Oops!!!")), message = randomString())
      //Test log info
      testActor.send(testDrive, infoMsg)
      val infoMsg1 = testUserLogging.expectMsgType[M.LogInfo]
      infoMsg1.blockId   shouldEqual Some(testBlockId)
      infoMsg1.blockName shouldEqual testBlockName
      infoMsg1.message   shouldEqual infoMsg.message
      //Test log warn
      testActor.send(testDrive, warnMsg)
      val warnMsg1 = testUserLogging.expectMsgType[M.LogWarning]
      warnMsg1.blockId   shouldEqual Some(testBlockId)
      warnMsg1.blockName shouldEqual testBlockName
      warnMsg1.message   shouldEqual warnMsg.message
      //Test log error
      testActor.send(testDrive, errorMsg)
      val errMsg1 = testUserLogging.expectMsgType[M.LogError]
      errMsg1.blockId     shouldEqual Some(testBlockId)
      errMsg1.blockName   shouldEqual testBlockName
      errMsg1.message     shouldEqual errorMsg.message
      errMsg1.message     shouldEqual errorMsg.message
      errMsg1.errors.head shouldEqual errorMsg.error.get}
    "create new user actor" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      testActor
      //Try to create
      val ref = testBlock.pump.askForNewUserActor(Props( new TeatActor), None)
      testActor.send(ref, "Hi!")
      testActor.expectMsg("Hey!")}
    "if error on creating of user actor, throw exception in user code" in new SimpleBlocks {
      import blocks._
      //Preparing
      builtBlock
      testActor
      val actorName = randomString()
      //Create
      testBlock.pump.askForNewUserActor(Props( new TeatActor), Some(actorName))
      //Fail
      val isError =
        try{
          testBlock.pump.askForNewUserActor(Props( new TeatActor), Some(actorName))
          false}
        catch{ case e: ExecutionException ⇒
          true}
      isError shouldEqual true}
  }
}
