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

import akka.actor.{Terminated, Actor, ActorRef, Props}
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
      outlets: Map[Int, (OutPipe[_], Option[Long], Map[(ActorRef, Int), Int])], // (Outlet ID, (Outlet, subscribers(id, inletQueueSize))
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
      private[core]  def injectOutPipe(pipe: OutPipe[Double]): Unit = {opPipe = Some(pipe)}
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
      val uiOperationTimeout = 4.second}
    val testPumpConfig = new PumpConfigLike{ val askTimeout = Timeout(4.second) }
    //Helpers actors
    lazy val testActor = TestProbe("TestActor_" + randomString())
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    lazy val testPlumbing = TestActor("TestPlumbing_" + randomString())((self, context) ⇒ {
      case M.NewDrive(blockPump) ⇒ Some{ Right{
        val drive = context.actorOf(Props(
          new DriveActor(
            testDriveConfig,
            testBlockId,
            blockPump,
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
        drive}}})
    lazy val testLayout = TestProbe("Layout_" + randomString())
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
      override val plumbing = PlumbingRef(testPlumbing.ref)}
    //Test blocks
    object blocks{
      lazy val testBlock = new BlockLike with OnStartLike with OnStopLike with BlockUILike{
        //Parameters
        def blockName = Some(testBlockName)
        def blockImagePath = None
        //Variable
        @volatile private var onStartCalled = false
        @volatile private var onStopCalled = false
        @volatile private var initFrameCalled = false
        @volatile private var createFrameCalled = false
        @volatile private var showFrameUICalled = false
        @volatile private var hideFrameCalled = false
        @volatile private var closeFrameCalled = false
        @volatile private var lastLayout: Option[(Int, Double, Double)] = None
        @volatile private var lastUiEvent: Option[UIEvent] = None
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
        //UI internal API
        private[core] def uiInit(): Unit = { initFrameCalled = true }
        private[core] def uiCreate(): Unit = { createFrameCalled = true }
        private[core] def uiShow(): Unit = { showFrameUICalled = true }
        private[core] def uiHide(): Unit = { hideFrameCalled = true }
        private[core] def uiClose(): Unit = { closeFrameCalled = true }
        private[core] def uiLayout(windowId: Int, x: Double, y: Double): Unit = {
          lastLayout = Some(Tuple3(windowId, x, y)) }
        private[core] def uiEvent(event: UIEvent): Unit = {
          lastUiEvent = Some(event)
          procError.foreach(e ⇒ throw e)}
        //Helpers methods
        def setProcTimeout(d: Duration): Unit = { procTimeout = Some(d) }
        def setProcError(err: Option[Throwable]): Unit = { procError = err }
        def isOnStartCalled: Boolean = onStartCalled
        def isOnStopCalled: Boolean = onStopCalled
        def isInitFrameCalled: Boolean = initFrameCalled
        def getLastUiLayout: Option[(Int, Double, Double)] = lastLayout
        def isCreateFrameCalled: Boolean = createFrameCalled
        def isShowFrameUICalled: Boolean = showFrameUICalled
        def isHideFrameCalled: Boolean = hideFrameCalled
        def isCloseFrameCalled: Boolean = closeFrameCalled
        def getLastUiEvent: Option[UIEvent] = lastUiEvent}
      lazy val testDrive = testBlock.pump.drive
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
          override val drive = otherDrive.ref}
        //Pipes
        val otherHandler = new TestHandler
        lazy val outlet = new OutPipe(otherHandler, Some("otherOutlet"), pump)
        lazy val inlet = new InPipe(otherHandler, Some("otherInlet"), pump)}
      lazy val builtBlock = {
        testBlock
        testPlumbing.send(blocks.testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        testPlumbing.send(testBlock.pump.drive, M.ConnectingDrive)
        testPlumbing.expectMsgType[M.DriveVerification]
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.BlockConstructedInfo]
        testPlumbing.send(testBlock.pump.drive, M.TurnOnDrive)
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
        testPlumbing.send(blocks.testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        //Process connections
        testPlumbing.send(testDrive, M.ConnectingDrive)
        val conMsg =  otherDrive.expectNMsg(2)
        val addCon = conMsg.getOneWithType[M.AddConnection]
        val inletData = InletData(otherDrive.ref, blockId = 0, None, addCon.inlet.inletId, addCon.inlet.inletName)
        otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, addCon.outlet, inletData))
        val conTo = conMsg.getOneWithType[M.ConnectTo]
        val outletData = OutletData(
          conTo.outlet.pump.drive, conTo.outlet.blockId, Some("otherOutlet"), otherOutlet.outletId, otherOutlet.outletName)
        otherDrive.send(conTo.initiator, M.PipesConnected(conTo.connectionId, conTo.initiator, outletData, conTo.inlet))
        testPlumbing.expectMsgType[M.DriveVerification]
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.BlockConstructedInfo]
        //Turning on
        testPlumbing.send(blocks.testDrive, M.TurnOnDrive)
        testPlumbing.expectMsg(M.DriveTurnedOn)
        //Starting
        testPlumbing.send(testDrive, M.StartDrive)
        testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
        testUserLogging.expectMsgType[M.LogInfo]
        //Outlets and inlets model
        (testOutlet, testInlet, otherOutlet, otherInlet)}}}
  //Testing
  "On starting" should{
    "adding of Outlet and Inlet" in new TestCase {
      //Preparing
      val outletId = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].outletId
      val inletId = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]].inletId
      blocks.otherBlock.outlet
      blocks.otherBlock.inlet
       //Testing
      val driveState = blocks.testDrive.askForState[DriveState]
      driveState.outlets should have size 1
      driveState.inlets should have size 1
      driveState.outlets.keys should contain (outletId)
      driveState.inlets.keys should contain (inletId)}
    "before ConnectingDrive, add new connections to pending list" in new TestCase {
      //Preparing
      val testOutlet1 = blocks.testBlock.outlet
      val testInlet1 = blocks.testBlock.inlet
      val otherOutlet1 = blocks.otherBlock.outlet
      val otherInlet1 =blocks.otherBlock.inlet
      //Connecting and disconnecting
      testOutlet1.attach(otherInlet1)
      testInlet1.plug(otherOutlet1)
      //Testing
      val pendingCon = blocks.testDrive.askForState[DriveState].pendingConnections
      pendingCon should have size 2}
    "Not accept new inlets/outlets after ConstructDrive message" in new TestCase {
      //Preparing
      blocks.testBlock
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Try to create inlet
      Try{blocks.testBlock.inlet}.toOption shouldBe empty
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      //Try to create outlet
      Try{blocks.testBlock.outlet}.toOption shouldBe empty
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)}
    "Not accept new connection after ConstructDrive message" in new TestCase {
      //Preparing
      val testOutlet1 = blocks.testBlock.outlet
      val testInlet1 = blocks.testBlock.inlet
      val otherOutlet1 = blocks.otherBlock.outlet
      val otherInlet1 = blocks.otherBlock.inlet
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting and disconnecting
      Try{testOutlet1.attach(otherInlet1)}.toOption shouldBe empty
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      Try{testInlet1.plug(otherOutlet1)}.toOption shouldBe empty
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)}
    "by ConnectingDrive, create connections from pending list and reply with DriveConnected (for 'plug')" in new TestCase {
      //Preparing
      val outlet = blocks.otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]]
      //Connecting (test block have inlet)
      blocks.testBlock.inlet.plug(blocks.otherBlock.outlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test wiring
      val connectTo = blocks.otherDrive.expectMsgType[M.ConnectTo]
      println(s"[PumpAndDriveTest] connectTo: $connectTo")
      connectTo.initiator        shouldEqual blocks.testDrive
      connectTo.outlet.outletId  shouldEqual outlet.outletId
      connectTo.inlet.inletId    shouldEqual inlet.inletId
      //Send M.PipesConnected and expect M.DriveConnected
      val outletData = OutletData(outlet.pump.drive, outlet.blockId, None, outlet.outletId, outlet.outletName)
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      blocks.otherDrive.send(
        connectTo.initiator,
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
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "by ConnectingDrive, create connections from pending list and reply with DriveConnected (for 'attach')" in new TestCase {
      //Preparing
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]]
      //Connecting (test block have outlet)
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test wiring
      val addConnection = blocks.otherDrive.expectMsgType[M.AddConnection]
      addConnection.initiator       shouldEqual blocks.testDrive
      addConnection.inlet.inletId   shouldEqual inlet.inletId
      addConnection.outlet.outletId shouldEqual outlet.outletId
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      blocks.otherDrive.send(
        blocks.testDrive,
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
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "put user messages to pending list and send it after turned on" in new TestCase {
      //Preparing
      val (v1,v2,v3,v4,v5) = (randomDouble(), randomDouble(), randomDouble(), randomDouble(), randomDouble())
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]]  //Creating of test block with outlet
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]]
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send in Init
      blocks.testBlock.testHandler.sendValue(v1)
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send in Constructed
      blocks.testBlock.testHandler.sendValue(v2)
      //Connecting
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      //Send in Connecting
      blocks.testBlock.testHandler.sendValue(v3)
      //Connected
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet, inletData))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send in Connected
      blocks.testBlock.testHandler.sendValue(v4)
      //Check pending list
      val driveState = blocks.testDrive.askForState[DriveState]
      driveState.pendingMessages should have size 4
      driveState.pendingMessages
        .map(_._2.asInstanceOf[Double])
        .zip(List(v1,v2,v3,v4,v5))
        .foreach{ case (av, bv) ⇒ av shouldEqual bv }}
    "by TurnOnDrive, send all messages from pending list" in new TestCase {
      //Preparing
      val (v1,v2,v3) = (randomDouble(), randomDouble(), randomDouble())
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]]  //Creating of test block with outlet
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]]
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet, inletData))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send to pending list
      blocks.testBlock.testHandler.sendValue(v1)
      blocks.testBlock.testHandler.sendValue(v2)
      blocks.testBlock.testHandler.sendValue(v3)
      //Turning on
      testPlumbing.send(blocks.testDrive, M.TurnOnDrive)
      //Expect user messages to be sent
      blocks.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v1
      blocks.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v2
      blocks.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v3
      //Expect
      testPlumbing.expectMsg(M.DriveTurnedOn)}
    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
      //Preparing
      blocks.builtBlock
      //Test
      testPlumbing.send(blocks.testDrive, M.StartDrive)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      blocks.testBlock.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
      //Preparing
      blocks.builtBlock
      blocks.testBlock.setProcTimeout(5.second)
      //Test
      testPlumbing.send(blocks.testDrive, M.StartDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      blocks.testBlock.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStarted" in new TestCase {
      //Preparing
      blocks.builtBlock
      blocks.testBlock.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(blocks.testDrive, M.StartDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      blocks.testBlock.isOnStartCalled shouldEqual true}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      blocks.builtBlock
      blocks.testBlock.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(blocks.testDrive, M.StartDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      blocks.testBlock.isOnStartCalled shouldEqual true}
    "on error of building do terminate" in new TestCase {
      //Preparing
      testPlumbing.watch(blocks.testDrive)
      val testOutlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]]
      val otherOutlet = blocks.otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]]
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test wiring with incorrect inletId in response
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      val inletData = InletData(inlet.pump.drive, inlet.blockId, None, inlet.inletId, inlet.inletName)
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, otherOutlet, inletData)) //Incorrect inletId
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual blocks.testDrive}
  }
  "On user message" should{
    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Call pour(value) for other block
      blocks.otherBlock.otherHandler.sendValue(value1)
      val userData = blocks.otherDrive.getProcessedMessages.getOneWithType[M.UserData[Double]]
      println("[PumpAndDriveTest] userData: " + userData)
      userData.outletId shouldEqual otherOutlet.outletId
      userData.value    shouldEqual value1
      //Call pour(value) test block
      blocks.testBlock.testHandler.sendValue(value2)
      val userMessage = blocks.otherDrive.expectMsgType[M.UserMessage[Double]]
      println("[PumpAndDriveTest] userMessage: " + userMessage)
      userMessage.outletId shouldEqual testOutlet.outletId
      userMessage.inletId  shouldEqual otherInlet.inletId
      userMessage.value    shouldEqual value2}
    "process UserMessage in Connected | TurnedOn | Starting | Working | Stopping | Stopped" in new TestCase {
      //Preparing
      val outlet = blocks.otherBlock.outlet.asInstanceOf[OutPipe[Double]]
      val inlet = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]]
      blocks.testBlock.inlet.plug(blocks.otherBlock.outlet)
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val connectTo = blocks.otherDrive.expectMsgType[M.ConnectTo]
      connectTo.initiator       shouldEqual blocks.testDrive
      connectTo.outlet.outletId shouldEqual outlet.outletId
      connectTo.inlet.inletId   shouldEqual inlet.inletId
      val outletData = OutletData(outlet.pump.drive, outlet.blockId, None, outlet.outletId, outlet.outletName)
      blocks.otherDrive.send(
        connectTo.initiator,
        M.PipesConnected(connectTo.connectionId,connectTo.initiator, outletData, connectTo.inlet))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Helpers
      def testMsgProcessing(): Unit = {
        //Preparing
        val value = randomDouble()
        //Sending (with no load message returned)
        blocks.otherDrive.send(blocks.testDrive, M.UserMessage(outlet.outletId, inlet.inletId, value))
        blocks.otherDrive.expectNoMsg(2.seconds)
        blocks.testBlock.testHandler.getReceivedValues.contains(value) shouldEqual true}
      //Testing in Connected
      testMsgProcessing()
      //Testing in TurnedOn
      testPlumbing.send(blocks.testDrive, M.TurnOnDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveTurnedOn)
      testMsgProcessing()
      sleep(1.second) //Wait for processing
      //Testing in Starting
      testPlumbing.send(blocks.testDrive, M.StartDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
      //Testing in Working
      testMsgProcessing()
      //Testing in Stopping
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStopped)
      //Testing in Stopped
      testMsgProcessing()}
    "by UserMessage, processing of messages with no load response" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      blocks.testBlock.testHandler.setProcTimeout(1.second)
      //Send first messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      blocks.otherDrive.expectNoMsg(2.seconds)
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testHandler.getReceivedValues.head shouldEqual value1
      //Send second messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      blocks.otherDrive.expectNoMsg(2.seconds)
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testHandler.getReceivedValues(1) shouldEqual value2}
    "by UserMessage, processing of messages with load response" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      blocks.testBlock.testHandler.setProcTimeout(2.second)
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      blocks.otherDrive.expectNoMsg(1.seconds)
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      //Load messages
      val driveLoad1 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.subscriberId shouldEqual Tuple2(blocks.testDrive, testInlet.inletId)
      driveLoad1.outletId shouldEqual otherOutlet.outletId
      driveLoad1.inletQueueSize shouldEqual 1
      sleep(1.second) //Wait for end processing of first message
      val driveLoad2 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.subscriberId shouldEqual Tuple2(blocks.testDrive, testInlet.inletId)
      driveLoad2.outletId shouldEqual otherOutlet.outletId
      driveLoad2.inletQueueSize shouldEqual 0
      //Check of message processing
      sleep(3.seconds) //Wait for second messages will processed
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)}
    "by UserMessage, in case message processing time out send warning to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      blocks.testBlock.testHandler.setProcTimeout(5.second)
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      sleep(3.seconds) //Wait for messages long timeout
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      //Check of message processing
      sleep(1.second) //Wait for second messages will processed
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, in case message processing error send error to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Send and get error
      blocks.testBlock.testHandler.setProcError(Some(new Exception("Oops!!!")))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.seconds) //Wait for second messages will processed
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1)
      //Send and not get error
      blocks.testBlock.testHandler.setProcError(None)
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      sleep(1.seconds) //Wait for second messages will processed
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)}
    "by DriveLoad, evaluate message handling timeout" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val subId = (blocks.otherDrive.ref, otherInlet.inletId)
      val queueSize = randomInt(100, 1000)
      //Test for first message
      blocks.otherDrive.send(blocks.testDrive, M.DriveLoad(subId, testOutlet.outletId, queueSize))
      sleep(1.second) //Wait for processing
      blocks.testDrive.askForState[DriveState].outlets(testOutlet.outletId)._3(subId) shouldEqual queueSize
      val pushTimeout1 = blocks.testDrive.askForState[DriveState].outlets(testOutlet.outletId)._2
      pushTimeout1 shouldEqual  Some(queueSize * testDriveConfig.pushTimeoutCoefficient)
      //Test for second message
      blocks.otherDrive.send(blocks.testDrive, M.DriveLoad(subId, testOutlet.outletId, 0))
      sleep(1.second) //Wait for processing
      blocks.testDrive.askForState[DriveState].outlets(testOutlet.outletId)._3(subId) shouldEqual 0
      val pushTimeout2 = blocks.testDrive.askForState[DriveState].outlets(testOutlet.outletId)._2
      pushTimeout2 shouldEqual None}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      blocks.testBlock.testHandler.setProcTimeout(7.second)
      val value1 = randomDouble()
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      sleep(1.seconds) //Small timeout
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.second) //Wait for second messages will processed
      blocks.testBlock.testHandler.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, terminate in case incorrect inlet ID" in new TestCase {
      //Preparing
      val (_, testInlet, otherOutlet, _) = blocks.connectedBlocks
      testPlumbing.watch(blocks.testDrive)
      //Send user messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, 123456789, randomDouble())) //Incorrect inlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual blocks.testDrive}
    "by DriveLoad, terminate in case incorrect outlet ID" in new TestCase {
      //Preparing
      val (_, _, _, otherInlet) = blocks.connectedBlocks
      testPlumbing.watch(blocks.testDrive)
      val subId = (blocks.otherDrive.ref, otherInlet.inletId)
      val queueSize = randomInt(100, 1000)
      //Send DriveLoad
      blocks.otherDrive.send(blocks.testDrive, M.DriveLoad(subId, 123456789, queueSize)) //Incorrect outlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual blocks.testDrive}
  }
  "On stopping" should{
    "by StopDrive, run user stop function and reply with DriveStopped" in new TestCase {
      //Preparing
      blocks.connectedBlocks
      //Test
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      blocks.testBlock.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
      //Preparing
      blocks.connectedBlocks
      blocks.testBlock.setProcTimeout(5.second)
      //Test
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      blocks.testBlock.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStopped" in new TestCase {
      //Preparing
      blocks.connectedBlocks
      blocks.testBlock.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      blocks.testBlock.isOnStopCalled shouldEqual true}
    "by TurnOffDrive, stop receive new user msgs, wait for empty queues, and response DriveTurnedOff " in new TestCase {
      //Preparing
      val value1 = randomDouble()
      val value2 = randomDouble()
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      blocks.testBlock.isOnStopCalled shouldEqual true
      blocks.testBlock.testHandler.setProcTimeout(3.second)
      testPlumbing.send(blocks.testDrive, M.DriveStopped)
      //Send two slow messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value1))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, value2))
      val driveLoad1 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.inletQueueSize shouldEqual 1
      //Send TurnOffDrive
      testPlumbing.send(blocks.testDrive, M.TurnOffDrive)
      sleep(1.second) //Wait for will processed
      //Send two messages, which will not processed
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, -1))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.outletId, testInlet.inletId, -2))
      blocks.otherDrive.expectNoMsg(1.second)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      //The first slow message processed
      val driveLoad2 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.inletQueueSize shouldEqual 0
      testPlumbing.expectMsg(M.DriveTurnedOff)(6.seconds)
      //Test received
      blocks.testBlock.testHandler.getReceivedValues shouldEqual List(value1, value2)
      //Expect no more messages
      testUserLogging.expectNoMsg(3.second)
      blocks.otherDrive.expectNoMsg(3.second)
      testPlumbing.expectNoMsg(3.second)}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      blocks.connectedBlocks
      blocks.testBlock.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(blocks.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      blocks.testBlock.isOnStopCalled shouldEqual true}
  }
  "In work" should{
    "by SetVisualisationLaval, update visualisation laval" in new TestCase {
      //Preparing
      blocks.builtBlock
      val newVisualisationLaval = randomVisualisationLaval()
      //Send
      testPlumbing.send(blocks.testDrive, M.SetVisualisationLaval(newVisualisationLaval))
      //Check
      blocks.testDrive.askForState[DriveState].visualisationLaval shouldEqual newVisualisationLaval}
    "call uiInit() on buildung of drive" in new TestCase {
      //Preparing
      blocks.builtBlock
      //Check
      blocks.testBlock.isInitFrameCalled shouldEqual true
      blocks.testBlock.isCreateFrameCalled shouldEqual false
      blocks.testBlock.isShowFrameUICalled shouldEqual false
      blocks.testBlock.isHideFrameCalled shouldEqual false
      blocks.testBlock.isCloseFrameCalled shouldEqual false}
    "call uiCreate() on starting of drive" in new TestCase {
      //Preparing
      blocks.startedBlock
      //Check
      blocks.testBlock.isInitFrameCalled shouldEqual true
      blocks.testBlock.isCreateFrameCalled shouldEqual true
      blocks.testBlock.isShowFrameUICalled shouldEqual false
      blocks.testBlock.isHideFrameCalled shouldEqual false
      blocks.testBlock.isCloseFrameCalled shouldEqual false}
    "call uiLayout() on M.UpdateWindowPosition" in new TestCase {
      //Preparing
      blocks.builtBlock
      val upMsg = M.UpdateWindowPosition(1, randomDouble(), randomDouble())
      //Send
      testLayout.send(blocks.testDrive, upMsg)
      sleep(1.second) //Wait for receiving
      //Check
      blocks.testBlock.getLastUiLayout shouldEqual Some(Tuple3(upMsg.id, upMsg.x, upMsg.y))}
    "pass messages to LayoutActor on calling of pump methods" in new TestCase {
      //Preparing
      blocks.builtBlock
      val windowId = randomInt()
      val state = WindowState(
        isShown = randomBoolean(),
        x = randomDouble(),
        y = randomDouble(),
        h = randomDouble(),
        w = randomDouble())
      val prefs = WindowPreference(
        prefX = randomOpt(randomDouble()),
        prefY = randomOpt(randomDouble()))
      //Test for registerWindow
      blocks.testBlock.pump.registerWindow(windowId, state, prefs)
      val registerMsg = testLayout.expectMsgType[M.RegisterWindow]
      registerMsg.id shouldEqual windowId
      registerMsg.state shouldEqual state
      registerMsg.prefs shouldEqual prefs
      //Test for windowUpdated
      blocks.testBlock.pump.windowUpdated(windowId, state)
      val updatedMsg = testLayout.expectMsgType[M.WindowUpdated]
      updatedMsg.id shouldEqual windowId
      updatedMsg.state shouldEqual state
      //Test for layoutWindow
      blocks.testBlock.pump.layoutWindow(windowId)
      val layoutMsg = testLayout.expectMsgType[M.LayoutWindow]
      layoutMsg.id shouldEqual windowId}
    "call uiShow() and uiHide() by M.ShowBlockUi and M.HideBlockUi" in new TestCase {
      //Preparing
      blocks.startedBlock
      //Test for M.ShowBlockUi
      testPlumbing.send(blocks.testDrive, M.ShowBlockUi)
      sleep(1.second) //Wait for receiving
      blocks.testBlock.isShowFrameUICalled shouldEqual true
      //Test for M.HideBlockUi
      testPlumbing.send(blocks.testDrive, M.HideBlockUi)
      sleep(1.second) //Wait for receiving
      blocks.builtBlock.isHideFrameCalled shouldEqual true}
    "call testBlock() on drive stopping" in new TestCase {
      //Preparing
      blocks.startedBlock
      //Stop
      testPlumbing.send(blocks.testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      //Check
      blocks.testBlock.isCloseFrameCalled shouldEqual true}
    "pass UI event via impeller"  in new TestCase {
      //Preparing
      blocks.builtBlock
      val e1 = new UIEvent{}
      //Test
      blocks.testBlock.pump.sendUiEvent(e1)
      sleep(1.second) //Wait for receiving
      blocks.testBlock.getLastUiEvent shouldEqual Some(e1)}
    "if UI event handling failed, log error to user logger"  in new TestCase {
      //Preparing
      blocks.builtBlock
      blocks.testBlock.setProcError(Some(new Exception("Oops!!! UI event fail.")))
      val e1 = new UIEvent{}
      //Test
      blocks.testBlock.pump.sendUiEvent(e1)
      sleep(1.second) //Wait for receiving
      blocks.testBlock.getLastUiEvent shouldEqual Some(e1)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)}
  }
  "Service methods" should{
    "re send user logging to logger actor" in new TestCase {
      //Preparing
      blocks.builtBlock
      val infoMsg = M.UserLogInfo(message = randomString())
      val warnMsg = M.UserLogWarn(message = randomString())
      val errorMsg = M.UserLogError(error = Some(new Exception("Oops!!!")), message = randomString())
      //Test log info
      testActor.send(blocks.testDrive, infoMsg)
      val infoMsg1 = testUserLogging.expectMsgType[M.LogInfo]
      infoMsg1.blockId   shouldEqual Some(testBlockId)
      infoMsg1.blockName shouldEqual testBlockName
      infoMsg1.message   shouldEqual infoMsg.message
      //Test log warn
      testActor.send(blocks.testDrive, warnMsg)
      val warnMsg1 = testUserLogging.expectMsgType[M.LogWarning]
      warnMsg1.blockId   shouldEqual Some(testBlockId)
      warnMsg1.blockName shouldEqual testBlockName
      warnMsg1.message   shouldEqual warnMsg.message
      //Test log error
      testActor.send(blocks.testDrive, errorMsg)
      val errMsg1 = testUserLogging.expectMsgType[M.LogError]
      errMsg1.blockId     shouldEqual Some(testBlockId)
      errMsg1.blockName   shouldEqual testBlockName
      errMsg1.message     shouldEqual errorMsg.message
      errMsg1.message     shouldEqual errorMsg.message
      errMsg1.errors.head shouldEqual errorMsg.error.get}
    "create new user actor" in new TestCase {
      //Preparing
      blocks.builtBlock
      testActor
      //Try to create
      val ref = blocks.testBlock.pump.askForNewUserActor(Props( new TeatActor), None)
      testActor.send(ref, "Hi!")
      testActor.expectMsg("Hey!")}
    "if error on creating of user actor, throw exception in user code" in new TestCase {
      //Preparing
      blocks.builtBlock
      testActor
      val actorName = randomString()
      //Create
      blocks.testBlock.pump.askForNewUserActor(Props( new TeatActor), Some(actorName))
      //Fail
      val isError =
        try{
          blocks.testBlock.pump.askForNewUserActor(Props( new TeatActor), Some(actorName))
          false}
        catch{ case e: ExecutionException ⇒
          true}
      isError shouldEqual true}
  }
}
