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

import akka.actor.{ActorRef, Props, Terminated}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.core._
import mathact.core.bricks.blocks.{BlockLike, SketchContext}
import mathact.core.bricks.plumbing.{ObjFitting, OnStop, OnStart}
import mathact.core.dummies.TestActor
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike}
import mathact.core.model.enums.VisualisationLaval
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting._
import mathact.core.plumbing.Pump
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
    trait TestHandler{
      //Variables
      private var receivedValues = List[Double]()
      private var procTimeout: Option[Duration] = None
      private var procError: Option[Throwable] = None
      //Receive user message
      def testDrain(value: Double): Unit = synchronized{
        println(
          s"[TestIncut] do drain, value: $value, procTimeout: $procTimeout, " +
            s"procError: $procError, receivedValues: $receivedValues")
        receivedValues :+= value
        procTimeout.foreach(d ⇒ Thread.sleep(d.toMillis))
        procError.foreach(e ⇒ throw e)}
      //Send message
      def testPour(value: Double): Unit
      //Test methods
      def setProcTimeout(d: Duration): Unit = synchronized{ procTimeout = Some(d) }
      def setProcError(err: Option[Throwable]): Unit = synchronized{ procError = err }
      def getReceivedValues: List[Double] = synchronized{ receivedValues }
      def sendValue(value: Double): Unit = testPour(value)}
    //Helpers values
    val testBlockId = randomInt()
    val testBlockName = "TestBlockName" + randomString(10)
    val testDriveConfig = new DriveConfigLike{
      val pushTimeoutCoefficient = 10
      val startFunctionTimeout = 4.second
      val messageProcessingTimeout = 4.second
      val stopFunctionTimeout = 4.second
      val impellerMaxQueueSize = 3
      val uiOperationTimeout = 4.second}
    val testPumpConfig = new PumpConfigLike{ val askTimeout = Timeout(4.second) }
    //Helpers actors
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    lazy val testPlumbing = TestActor("TestPlumbing_" + randomString())((self, context) ⇒ {
      case M.NewDrive(blockPump) ⇒ Some{ Right{
        val drive = context.actorOf(Props(
          new DriveActor(testDriveConfig, testBlockId, blockPump, self, testUserLogging.ref, testVisualization.ref){
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
        println(s"[PumpAndDriveTest.testPlumbing.NewDrive] Created of drive for block: ${blockPump.blockName}, drive: $drive")
        drive}}})
    //Test workbench context
    lazy val testSketchContext = new SketchContext(
      system, testController.ref,
      testUserLogging.ref,
      testPlumbing.ref,
      testPumpConfig,
      ConfigFactory.load())
    {
      override val plumbing: ActorRef = testPlumbing.ref}
    //Test blocks
    object blocks{










      lazy val testBlock = new BlockLike with ObjFitting with OnStart with OnStop{ // with UIControl{
        //Variable
        @volatile private var onStartCalled = false
        @volatile private var onStopCalled = false
//        @volatile private var onShowUICalled = false
//        @volatile private var onHideUICalled = false
        @volatile private var procTimeout: Option[Duration] = None
        @volatile private var procError: Option[Throwable] = None
        //Pump
        val pump: Pump = new Pump(testSketchContext, this, testBlockName, None){}
        //Pipes
        val testPipe = new TestHandler with Outflow[Double] with Inflow[Double]{
          def testPour(value: Double): Unit = pour(value)
          protected def drain(value: Double): Unit = testDrain(value)}
        lazy val outlet = Outlet(testPipe, "testOutlet")
        lazy val inlet = Inlet(testPipe, "testInlet")
        //On start and stop
        protected def onStart() = {
          println("[PumpAndDriveTest.testBlock] onStart called.")
          onStartCalled = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        protected def onStop() = {
          println("[PumpAndDriveTest.testBlock] onStop called.")
          onStopCalled = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        //TODO Перерабтать при разработке UI тулкита (не забыть про проверку вызова cleanup() в случае фатальной ошибки)
//        //On show UI and hide UI
//        protected def onShowUI() = {
//          println("[PumpAndDriveTest.testBlock] onShowUI called.")
//          onShowUICalled = true
//          procTimeout.foreach(d ⇒ sleep(d))
//          procError.foreach(e ⇒ throw e)}
//        protected def onHideUI() = {
//          println("[PumpAndDriveTest.testBlock] onHideUI called.")
//          onHideUICalled = true
//          procTimeout.foreach(d ⇒ sleep(d))
//          procError.foreach(e ⇒ throw e)}
        //Helpers methods
        def setProcTimeout(d: Duration): Unit = { procTimeout = Some(d) }
        def setProcError(err: Option[Throwable]): Unit = { procError = err }
        def isOnStartCalled: Boolean = onStartCalled
        def isOnStopCalled: Boolean = onStopCalled
//        def isOnShowUICalled: Boolean = onShowUICalled
//        def isOnHideUICalled: Boolean = onHideUICalled
      }
      lazy val testDrive = testBlock.pump.drive
      lazy val otherDrive = TestActor("TestOtherDriver_" + randomString())((self, _) ⇒ {
        case M.AddOutlet(pipe, _) ⇒ Some(Right((0, 1)))  // (block ID, pipe ID)
        case M.AddInlet(pipe, _) ⇒  Some(Right((0, 2)))  // (block ID, pipe ID)
        case M.UserData(outletId, _) ⇒  Some(Right(None))})
      lazy val otherBlock = new BlockLike with ObjFitting{
        //Pump
        val pump: Pump = new Pump(testSketchContext, this, "OtherBlock", None){
          override val drive = otherDrive.ref}
        //Pipes
        val testIncut = new TestHandler with Outflow[Double] with Inflow[Double]{
          def testPour(value: Double): Unit = pour(value)
          protected def drain(value: Double): Unit = testDrain(value)}
        lazy val outlet = Outlet(testIncut, "otherOutlet")
        lazy val inlet = Inlet(testIncut, "otherInlet")}
      lazy val builtBlock = {
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
      lazy val connectedBlocks = {
        //Preparing
        val testOutlet = testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val testInlet = testBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
        val otherOutlet = otherBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val otherInlet = otherBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
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
        val conTo = conMsg.getOneWithType[M.ConnectTo]
        otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, addCon.outlet.pipeId, otherInlet))
        otherDrive.send(conTo.initiator, M.PipesConnected(conTo.connectionId, testOutlet, conTo.inlet))
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
      val outletId = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData.pipeId
      val inletId = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]].pipeData.pipeId
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
      val outlet = blocks.otherBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      //Connecting (test block have inlet)
      blocks.testBlock.inlet.plug(blocks.otherBlock.outlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test connecting
      val connectTo = blocks.otherDrive.expectMsgType[M.ConnectTo]
      println(s"[PumpAndDriveTest] connectTo: $connectTo")
      connectTo.initiator    shouldEqual blocks.testDrive
      connectTo.outletId     shouldEqual outlet.pipeId
      connectTo.inlet.pipeId shouldEqual inlet.pipeId
      //Send M.PipesConnected and expect M.DriveConnected
      blocks.otherDrive.send(connectTo.initiator, M.PipesConnected(connectTo.connectionId, outlet, inlet))
      val verData = testPlumbing.expectMsgType[M.DriveVerification].verificationData
      println(s"[PumpAndDriveTest] verData: $verData")
      verData.blockId    shouldEqual testBlockId
      verData.inlets    should have size 1
      verData.inlets.head.inletId    shouldEqual inlet.pipeId
      verData.inlets.head.publishers should have size 1
      verData.inlets.head.publishers.head.blockId   shouldEqual outlet.blockId
      verData.inlets.head.publishers.head.pipeId shouldEqual outlet.pipeId
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
      builtInfo.inlets.head.inletId    shouldEqual inlet.pipeId
      builtInfo.inlets.head.inletName  shouldEqual inlet.pipeName
      builtInfo.outlets shouldBe empty
      //Check pending list
      sleep(500.millis) //Wait for processing of PipesConnected by testBlock
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "by ConnectingDrive, create connections from pending list and reply with DriveConnected (for 'attach')" in new TestCase {
      //Preparing
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      //Connecting (test block have outlet)
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test connecting
      val addConnection = blocks.otherDrive.expectMsgType[M.AddConnection]
      addConnection.initiator     shouldEqual blocks.testDrive
      addConnection.inletId       shouldEqual inlet.pipeId
      addConnection.outlet.pipeId shouldEqual outlet.pipeId
      blocks.otherDrive.send(
        blocks.testDrive,
        M.ConnectTo(addConnection.connectionId, addConnection.initiator, outlet.pipeId, inlet))
      //Expect DriveVerification, DriveConnected
      val verData = testPlumbing.expectMsgType[M.DriveVerification].verificationData
      println(s"[PumpAndDriveTest] verData: $verData")
      verData.blockId     shouldEqual testBlockId
      verData.inlets      shouldBe empty
      verData.outlets     should have size 1
      verData.outlets.head.outletId    shouldEqual outlet.pipeId
      verData.outlets.head.subscribers should have size 1
      verData.outlets.head.subscribers.head.blockId shouldEqual inlet.blockId
      verData.outlets.head.subscribers.head.pipeId  shouldEqual inlet.pipeId
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
      builtInfo.outlets.head.outletId    shouldEqual outlet.pipeId
      builtInfo.outlets.head.outletName  shouldEqual outlet.pipeName
      //Check pendingConnections
      sleep(500.millis) //Wait for processing of PipesConnected by testBlock
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "put user messages to pending list and send it after turned on" in new TestCase {
      //Preparing
      val (v1,v2,v3,v4,v5) = (randomDouble(), randomDouble(), randomDouble(), randomDouble(), randomDouble())
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData  //Creating of test block with outlet
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send in Init
      blocks.testBlock.testPipe.sendValue(v1)
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send in Constructed
      blocks.testBlock.testPipe.sendValue(v2)
      //Connecting
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      //Send in Connecting
      blocks.testBlock.testPipe.sendValue(v3)
      //Connected
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet.pipeId, inlet))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send in Connected
      blocks.testBlock.testPipe.sendValue(v4)
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
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData  //Creating of test block with outlet
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      blocks.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet.pipeId, inlet))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Send to pending list
      blocks.testBlock.testPipe.sendValue(v1)
      blocks.testBlock.testPipe.sendValue(v2)
      blocks.testBlock.testPipe.sendValue(v3)
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
      val outlet = blocks.testBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = blocks.otherBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      blocks.testBlock.outlet.attach(blocks.otherBlock.inlet)
      //Construct
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      //Test connecting with incorrect outletId in response
      val addCon = blocks.otherDrive.expectMsgType[M.AddConnection]
      blocks.otherDrive.send(blocks.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, 123456789, inlet)) //Incorrect outletId
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
      blocks.otherBlock.testIncut.sendValue(value1)
      val userData = blocks.otherDrive.getProcessedMessages.getOneWithType[M.UserData[Double]]
      println("[PumpAndDriveTest] userData: " + userData)
      userData.outletId shouldEqual otherOutlet.pipeId
      userData.value    shouldEqual value1
      //Call pour(value) test block
      blocks.testBlock.testPipe.sendValue(value2)
      val userMessage = blocks.otherDrive.expectMsgType[M.UserMessage[Double]]
      println("[PumpAndDriveTest] userMessage: " + userData)
      userMessage.outletId shouldEqual testOutlet.pipeId
      userMessage.inletId  shouldEqual otherInlet.pipeId
      userMessage.value    shouldEqual value2}
    "process UserMessage in Connected | TurnedOn | Starting | Working | Stopping | Stopped" in new TestCase {
      //Preparing
      val outlet = blocks.otherBlock.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = blocks.testBlock.inlet.asInstanceOf[InPipe[Double]].pipeData
      blocks.testBlock.inlet.plug(blocks.otherBlock.outlet)
      testPlumbing.send(blocks.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      testPlumbing.send(blocks.testDrive, M.ConnectingDrive)
      val connectTo = blocks.otherDrive.expectMsgType[M.ConnectTo]
      connectTo.initiator    shouldEqual blocks.testDrive
      connectTo.outletId     shouldEqual outlet.pipeId
      connectTo.inlet.pipeId shouldEqual inlet.pipeId
      blocks.otherDrive.send(connectTo.initiator, M.PipesConnected(connectTo.connectionId, outlet, inlet))
      testPlumbing.expectMsgType[M.DriveVerification]
      testPlumbing.expectMsg(M.DriveConnected)
      //Helpers
      def testMsgProcessing(): Unit = {
        //Preparing
        val value = randomDouble()
        //Sending (with no load message returned)
        blocks.otherDrive.send(blocks.testDrive, M.UserMessage(outlet.pipeId, inlet.pipeId, value))
        blocks.otherDrive.expectNoMsg(2.seconds)
        blocks.testBlock.testPipe.getReceivedValues.contains(value) shouldEqual true}
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
      blocks.testBlock.testPipe.setProcTimeout(1.second)
      //Send first messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      blocks.otherDrive.expectNoMsg(2.seconds)
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testPipe.getReceivedValues.head shouldEqual value1
      //Send second messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      blocks.otherDrive.expectNoMsg(2.seconds)
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testPipe.getReceivedValues(1) shouldEqual value2}
    "by UserMessage, processing of messages with load response" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      blocks.testBlock.testPipe.setProcTimeout(2.second)
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      blocks.otherDrive.expectNoMsg(1.seconds)
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      //Load messages
      val driveLoad1 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.subscriberId shouldEqual Tuple2(blocks.testDrive, testInlet.pipeId)
      driveLoad1.outletId shouldEqual otherOutlet.pipeId
      driveLoad1.inletQueueSize shouldEqual 1
      sleep(1.second) //Wait for end processing of first message
      val driveLoad2 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.subscriberId shouldEqual Tuple2(blocks.testDrive, testInlet.pipeId)
      driveLoad2.outletId shouldEqual otherOutlet.pipeId
      driveLoad2.inletQueueSize shouldEqual 0
      //Check of message processing
      sleep(3.seconds) //Wait for second messages will processed
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1, value2)}
    "by UserMessage, in case message processing time out send warning to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      blocks.testBlock.testPipe.setProcTimeout(5.second)
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      sleep(3.seconds) //Wait for messages long timeout
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      //Check of message processing
      sleep(1.second) //Wait for second messages will processed
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, in case message processing error send error to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Send and get error
      blocks.testBlock.testPipe.setProcError(Some(new Exception("Oops!!!")))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.seconds) //Wait for second messages will processed
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1)
      //Send and not get error
      blocks.testBlock.testPipe.setProcError(None)
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      sleep(1.seconds) //Wait for second messages will processed
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 2
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1, value2)}
    "by DriveLoad, evaluate message handling timeout" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      val subId = (blocks.otherDrive.ref, otherInlet.pipeId)
      val queueSize = randomInt(100, 1000)
      //Test for first message
      blocks.otherDrive.send(blocks.testDrive, M.DriveLoad(subId, testOutlet.pipeId, queueSize))
      sleep(1.second) //Wait for processing
      blocks.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._3(subId) shouldEqual queueSize
      val pushTimeout1 = blocks.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2
      pushTimeout1 shouldEqual  Some(queueSize * testDriveConfig.pushTimeoutCoefficient)
      //Test for second message
      blocks.otherDrive.send(blocks.testDrive, M.DriveLoad(subId, testOutlet.pipeId, 0))
      sleep(1.second) //Wait for processing
      blocks.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._3(subId) shouldEqual 0
      val pushTimeout2 = blocks.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2
      pushTimeout2 shouldEqual None}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = blocks.connectedBlocks
      blocks.testBlock.testPipe.setProcTimeout(7.second)
      val value1 = randomDouble()
      //Send message
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
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
      blocks.testBlock.testPipe.getReceivedValues.size shouldEqual 1
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, terminate in case incorrect inlet ID" in new TestCase {
      //Preparing
      val (_, testInlet, otherOutlet, _) = blocks.connectedBlocks
      testPlumbing.watch(blocks.testDrive)
      //Send user messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, 123456789, randomDouble())) //Incorrect inlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual blocks.testDrive}
    "by DriveLoad, terminate in case incorrect outlet ID" in new TestCase {
      //Preparing
      val (_, _, _, otherInlet) = blocks.connectedBlocks
      testPlumbing.watch(blocks.testDrive)
      val subId = (blocks.otherDrive.ref, otherInlet.pipeId)
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
      blocks.testBlock.testPipe.setProcTimeout(3.second)
      testPlumbing.send(blocks.testDrive, M.DriveStopped)
      //Send two slow messages
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      val driveLoad1 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.inletQueueSize shouldEqual 1
      //Send TurnOffDrive
      testPlumbing.send(blocks.testDrive, M.TurnOffDrive)
      sleep(1.second) //Wait for will processed
      //Send two messages, which will not processed
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -1))
      blocks.otherDrive.send(blocks.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -2))
      blocks.otherDrive.expectNoMsg(1.second)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      //The first slow message processed
      val driveLoad2 = blocks.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.inletQueueSize shouldEqual 0
      //Test received
      blocks.testBlock.testPipe.getReceivedValues shouldEqual List(value1, value2)
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
      blocks.testDrive ! M.SetVisualisationLaval(newVisualisationLaval)
      //Check
      blocks.testDrive.askForState[DriveState].visualisationLaval shouldEqual newVisualisationLaval}
//TODO Переписать при разработке поддержки UI инструментов
//    "by ShowBlockUi, should call onShowUi()" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      //Test
//      testPlumbing.send(blocks.testDrive, M.ShowBlockUi)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnShowUICalled shouldEqual true}
//    "by ShowBlockUi if onShowUi() time out, should log warning in user logger" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      blocks.testBlock.setProcTimeout(5.second)
//      //Test
//      testPlumbing.send(blocks.testDrive, M.ShowBlockUi)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnShowUICalled shouldEqual true}
//    "by ShowBlockUi if onShowUi() fail, should log error in user logger" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      blocks.testBlock.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPlumbing.send(blocks.testDrive, M.ShowBlockUi)
//      val logError = testUserLogging.expectMsgType[M.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnShowUICalled shouldEqual true}
//    "by HideBlockUi, should call onHideUi()" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      //Test
//      testPlumbing.send(blocks.testDrive, M.HideBlockUi)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnHideUICalled shouldEqual true}
//    "by HideBlockUi if onHideUi() timeout, should log warning in user logger" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      blocks.testBlock.setProcTimeout(5.second)
//      //Test
//      testPlumbing.send(blocks.testDrive, M.HideBlockUi)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnHideUICalled shouldEqual true}
//    "by HideBlockUi if onHideUi() fail, should log error in user logger" in new TestCase {
//      //Preparing
//      blocks.builtBlock
//      blocks.testBlock.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPlumbing.send(blocks.testDrive, M.HideBlockUi)
//      val logError = testUserLogging.expectMsgType[M.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPlumbing.expectNoMsg(1.second)
//      blocks.testBlock.isOnHideUICalled shouldEqual true}
  }
}
