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
import mathact.core.bricks.{OnStart, OnStop, SketchContext}
import mathact.core.dummies.TestActor
import mathact.core.model.config.{DriveConfigLike, PumpConfigLike}
import mathact.core.model.enums.VisualisationLaval
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting._
import mathact.core.plumbing.{Fitting, Pump}
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
    //Helpers values
    val testToolId = randomInt()
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
      case M.NewDrive(toolPump) ⇒ Some{ Right{
        val drive = context.actorOf(Props(
          new DriveActor(testDriveConfig, testToolId, toolPump, self, testUserLogging.ref, testVisualization.ref){
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
        println(s"[PumpAndDriveTest.testPlumbing.NewDrive] Created of drive for tool: ${toolPump.toolName}, drive: $drive")
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
    //Test tools
    object tools{
      lazy val testTool = new Fitting with OnStart with OnStop{ // with UIControl{
        //Variable
        @volatile private var onStartCalled = false
        @volatile private var onStopCalled = false
//        @volatile private var onShowUICalled = false
//        @volatile private var onHideUICalled = false
        @volatile private var procTimeout: Option[Duration] = None
        @volatile private var procError: Option[Throwable] = None
        //Pump
        val pump: Pump = new Pump(testSketchContext, this, "TestTool", None){}
        //Pipes
        val testPipe = new TestIncut[Double]
        lazy val outlet = Outlet(testPipe, "testOutlet")
        lazy val inlet = Inlet(testPipe, "testInlet")
        //On start and stop
        protected def onStart() = {
          println("[PumpAndDriveTest.testTool] onStart called.")
          onStartCalled = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        protected def onStop() = {
          println("[PumpAndDriveTest.testTool] onStop called.")
          onStopCalled = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        //TODO Перерабтать при разработке UI тулкита (не забыть про проверку вызова cleanup() в случае фатальной ошибки)
//        //On show UI and hide UI
//        protected def onShowUI() = {
//          println("[PumpAndDriveTest.testTool] onShowUI called.")
//          onShowUICalled = true
//          procTimeout.foreach(d ⇒ sleep(d))
//          procError.foreach(e ⇒ throw e)}
//        protected def onHideUI() = {
//          println("[PumpAndDriveTest.testTool] onHideUI called.")
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
      lazy val testDrive = testTool.pump.drive
      lazy val otherDrive = TestActor("TestOtherDriver_" + randomString())((self, _) ⇒ {
        case M.AddOutlet(pipe, _) ⇒ Some(Right((0, 1)))  // (tool ID, pipe ID)
        case M.AddInlet(pipe, _) ⇒  Some(Right((0, 2)))  // (tool ID, pipe ID)
        case M.UserData(outletId, _) ⇒  Some(Right(None))})
      lazy val otherTool = new Fitting{
        //Pump
        val pump: Pump = new Pump(testSketchContext, this, "OtherTool", None){
          override val drive = otherDrive.ref}
        //Pipes
        val testIncut = new TestIncut[Double]
        lazy val outlet = Outlet(testIncut, "otherOutlet")
        lazy val inlet = Inlet(testIncut, "otherInlet")}
      lazy val builtTool = {
        testPlumbing.send(tools.testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        testPlumbing.send(testTool.pump.drive, M.ConnectingDrive)
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.ToolBuilt]
        testPlumbing.send(testTool.pump.drive, M.TurnOnDrive)
        testPlumbing.expectMsg(M.DriveTurnedOn)
        testTool.isOnStartCalled shouldEqual false
        testTool}
      lazy val connectedTools = {
        //Preparing
        val testOutlet = testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val testInlet = testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
        val otherOutlet = otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val otherInlet = otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
        //Connecting
        testTool.inlet.plug(otherTool.outlet)
        testTool.outlet.attach(otherTool.inlet)
        //Send ConstructDrive
        testPlumbing.send(tools.testDrive, M.ConstructDrive)
        testPlumbing.expectMsg(M.DriveConstructed)
        //Process for other tool
        testPlumbing.send(testDrive, M.ConnectingDrive)
        val conMsg =  otherDrive.expectNMsg(2)
        val addCon = conMsg.getOneWithType[M.AddConnection]
        val conTo = conMsg.getOneWithType[M.ConnectTo]
        otherDrive.send(testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, addCon.outlet.pipeId, otherInlet))
        otherDrive.send(conTo.initiator, M.PipesConnected(conTo.connectionId, conTo.inlet.pipeId, conTo.outletId))
        testPlumbing.expectMsg(M.DriveConnected)
        testUserLogging.expectMsgType[M.LogInfo]
        testVisualization.expectMsgType[M.ToolBuilt]
        //Turning on
        testPlumbing.send(tools.testDrive, M.TurnOnDrive)
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
      val outletId = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData.pipeId
      val inletId = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData.pipeId
      tools.otherTool.outlet
      tools.otherTool.inlet
       //Testing
      val driveState = tools.testDrive.askForState[DriveState]
      driveState.outlets should have size 1
      driveState.inlets should have size 1
      driveState.outlets.keys should contain (outletId)
      driveState.inlets.keys should contain (inletId)}
    "before ConnectingDrive, add new connections to pending list" in new TestCase {
      //Preparing
      val testOutlet1 = tools.testTool.outlet
      val testInlet1 = tools.testTool.inlet
      val otherOutlet1 = tools.otherTool.outlet
      val otherInlet1 =tools.otherTool.inlet
      //Connecting and disconnecting
      testOutlet1.attach(otherInlet1)
      testInlet1.plug(otherOutlet1)
      //Testing
      val pendingCon = tools.testDrive.askForState[DriveState].pendingConnections
      pendingCon should have size 2}
    "Not accept new inlets/outlets after ConstructDrive message" in new TestCase {
      //Preparing
      tools.testTool
      //Send ConstructDrive
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Try to create inlet
      Try{tools.testTool.inlet}.toOption shouldBe empty
      val logWarning1 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning1: " + logWarning1)
      //Try to create outlet
      Try{tools.testTool.outlet}.toOption shouldBe empty
      val logWarning2 = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning2: " + logWarning2)}
    "Not accept new connection after ConstructDrive message" in new TestCase {
      //Preparing
      val testOutlet1 = tools.testTool.outlet
      val testInlet1 = tools.testTool.inlet
      val otherOutlet1 = tools.otherTool.outlet
      val otherInlet1 = tools.otherTool.inlet
      //Send ConstructDrive
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
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
      val outlet = tools.otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      //Connecting (test tool have inlet)
      tools.testTool.inlet.plug(tools.otherTool.outlet)
      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      //Test connecting
      val connectTo = tools.otherDrive.expectMsgType[M.ConnectTo]
      println(s"[PumpAndDriveTest] connectTo: $connectTo")
      connectTo.initiator    shouldEqual tools.testDrive
      connectTo.outletId     shouldEqual outlet.pipeId
      connectTo.inlet.pipeId shouldEqual inlet.pipeId
      //Send M.PipesConnected and expect M.DriveConnected
      tools.otherDrive.send(connectTo.initiator, M.PipesConnected(connectTo.connectionId, inlet.pipeId, outlet.pipeId))
      testPlumbing.expectMsg(M.DriveConnected)
      //Check ToolBuiltInfo
      val builtInfo = testVisualization.expectMsgType[M.ToolBuilt].builtInfo
      println(s"[PumpAndDriveTest] builtInfo: $builtInfo")
      builtInfo.toolId    shouldEqual testToolId
      builtInfo.toolName  shouldEqual "TestTool"
      builtInfo.toolImagePath shouldEqual None
      builtInfo.inlets    should have size 1
      builtInfo.inlets.values.head.toolId     shouldEqual testToolId
      builtInfo.inlets.values.head.inletId    shouldEqual inlet.pipeId
      builtInfo.inlets.values.head.inletName  shouldEqual inlet.pipeName
      builtInfo.inlets.values.head.publishers should have size 1
      builtInfo.inlets.values.head.publishers.head.toolId   shouldEqual outlet.toolId
      builtInfo.inlets.values.head.publishers.head.outletId shouldEqual outlet.pipeId
      builtInfo.outlets shouldBe empty
      //Check pending list
      sleep(500.millis) //Wait for processing of PipesConnected by testTool
      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "by ConnectingDrive, create connections from pending list and reply with DriveConnected (for 'attach')" in new TestCase {
      //Preparing
      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      //Connecting (test tool have outlet)
      tools.testTool.outlet.attach(tools.otherTool.inlet)
      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send ConstructDrive
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      //Test connecting
      val addConnection = tools.otherDrive.expectMsgType[M.AddConnection]
      addConnection.initiator     shouldEqual tools.testDrive
      addConnection.inletId       shouldEqual inlet.pipeId
      addConnection.outlet.pipeId shouldEqual outlet.pipeId
      tools.otherDrive.send(
        tools.testDrive,
        M.ConnectTo(addConnection.connectionId, addConnection.initiator, outlet.pipeId, inlet))
      //Expect DriveConnected
      testPlumbing.expectMsg(M.DriveConnected)
      //Check ToolBuiltInfo
      val builtInfo = testVisualization.expectMsgType[M.ToolBuilt].builtInfo
      println(s"[PumpAndDriveTest] builtInfo: $builtInfo")
      builtInfo.toolId    shouldEqual testToolId
      builtInfo.toolName  shouldEqual "TestTool"
      builtInfo.toolImagePath shouldEqual None
      builtInfo.inlets    shouldBe empty
      builtInfo.outlets should have size 1
      builtInfo.outlets.values.head.toolId      shouldEqual testToolId
      builtInfo.outlets.values.head.outletId    shouldEqual outlet.pipeId
      builtInfo.outlets.values.head.outletName  shouldEqual outlet.pipeName
      builtInfo.outlets.values.head.subscribers should have size 1
      builtInfo.outlets.values.head.subscribers.head.toolId   shouldEqual inlet.toolId
      builtInfo.outlets.values.head.subscribers.head.inletId shouldEqual inlet.pipeId
      //Check pendingConnections
      sleep(500.millis) //Wait for processing of PipesConnected by testTool
      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
    "put user messages to pending list and send it after turned on" in new TestCase {
      //Preparing
      val (v1,v2,v3,v4,v5) = (randomDouble(), randomDouble(), randomDouble(), randomDouble(), randomDouble())
      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData  //Creating of test tool with outlet
      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      tools.testTool.outlet.attach(tools.otherTool.inlet)
      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Send in Init
      tools.testTool.testPipe.sendValue(v1)
      //Construct
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send in Constructed
      tools.testTool.testPipe.sendValue(v2)
      //Connecting
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      val addCon = tools.otherDrive.expectMsgType[M.AddConnection]
      //Send in Connecting
      tools.testTool.testPipe.sendValue(v3)
      //Connected
      tools.otherDrive.send(tools.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet.pipeId, inlet))
      testPlumbing.expectMsg(M.DriveConnected)
      //Send in Connected
      tools.testTool.testPipe.sendValue(v4)
      //Check pending list
      val driveState = tools.testDrive.askForState[DriveState]
      driveState.pendingMessages should have size 4
      driveState.pendingMessages
        .map(_._2.asInstanceOf[Double])
        .zip(List(v1,v2,v3,v4,v5))
        .foreach{ case (av, bv) ⇒ av shouldEqual bv }}
    "by TurnOnDrive, send all messages from pending list" in new TestCase {
      //Preparing
      val (v1,v2,v3) = (randomDouble(), randomDouble(), randomDouble())
      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData  //Creating of test tool with outlet
      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      tools.testTool.outlet.attach(tools.otherTool.inlet)
      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
      //Construct
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Connecting
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      val addCon = tools.otherDrive.expectMsgType[M.AddConnection]
      tools.otherDrive.send(tools.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, outlet.pipeId, inlet))
      testPlumbing.expectMsg(M.DriveConnected)
      //Send to pending list
      tools.testTool.testPipe.sendValue(v1)
      tools.testTool.testPipe.sendValue(v2)
      tools.testTool.testPipe.sendValue(v3)
      //Turning on
      testPlumbing.send(tools.testDrive, M.TurnOnDrive)
      //Expect user messages to be sent
      tools.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v1
      tools.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v2
      tools.otherDrive.expectMsgType[M.UserMessage[Double]].value shouldEqual v3
      //Expect
      testPlumbing.expectMsg(M.DriveTurnedOn)}
    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
      //Preparing
      tools.builtTool
      //Test
      testPlumbing.send(tools.testDrive, M.StartDrive)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      tools.testTool.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
      //Preparing
      tools.builtTool
      tools.testTool.setProcTimeout(5.second)
      //Test
      testPlumbing.send(tools.testDrive, M.StartDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStarted)
      val logInfo1 = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] logInfo1: " + logInfo1)
      tools.testTool.isOnStartCalled shouldEqual true}
    "by StartDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStarted" in new TestCase {
      //Preparing
      tools.builtTool
      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(tools.testDrive, M.StartDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      tools.testTool.isOnStartCalled shouldEqual true}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      tools.builtTool
      tools.testTool.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(tools.testDrive, M.StartDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStarted)
      tools.testTool.isOnStartCalled shouldEqual true}
    "on error of building do terminate" in new TestCase {
      //Preparing
      testPlumbing.watch(tools.testDrive)
      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      tools.testTool.outlet.attach(tools.otherTool.inlet)
      //Construct
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      //Send ConnectingDrive
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      //Test connecting with incorrect outletId in response
      val addCon = tools.otherDrive.expectMsgType[M.AddConnection]
      tools.otherDrive.send(tools.testDrive, M.ConnectTo(addCon.connectionId, addCon.initiator, 123456789, inlet)) //Incorrect outletId
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual tools.testDrive}
  }
  "On user message" should{
    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Call pour(value) for other tool
      tools.otherTool.testIncut.sendValue(value1)
      val userData = tools.otherDrive.getProcessedMessages.getOneWithType[M.UserData[Double]]
      println("[PumpAndDriveTest] userData: " + userData)
      userData.outletId shouldEqual otherOutlet.pipeId
      userData.value    shouldEqual value1
      //Call pour(value) test tool
      tools.testTool.testPipe.sendValue(value2)
      val userMessage = tools.otherDrive.expectMsgType[M.UserMessage[Double]]
      println("[PumpAndDriveTest] userMessage: " + userData)
      userMessage.outletId shouldEqual testOutlet.pipeId
      userMessage.inletId  shouldEqual otherInlet.pipeId
      userMessage.value    shouldEqual value2}
    "process UserMessage in Connected | TurnedOn | Starting | Working | Stopping | Stopped" in new TestCase {
      //Preparing
      val outlet = tools.otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
      val inlet = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
      tools.testTool.inlet.plug(tools.otherTool.outlet)
      testPlumbing.send(tools.testDrive, M.ConstructDrive)
      testPlumbing.expectMsg(M.DriveConstructed)
      testPlumbing.send(tools.testDrive, M.ConnectingDrive)
      val connectTo = tools.otherDrive.expectMsgType[M.ConnectTo]
      connectTo.initiator    shouldEqual tools.testDrive
      connectTo.outletId     shouldEqual outlet.pipeId
      connectTo.inlet.pipeId shouldEqual inlet.pipeId
      tools.otherDrive.send(connectTo.initiator, M.PipesConnected(connectTo.connectionId, inlet.pipeId, outlet.pipeId))
      testPlumbing.expectMsg(M.DriveConnected)
      //Helpers
      def testMsgProcessing(): Unit = {
        //Preparing
        val value = randomDouble()
        //Sending (with no load message returned)
        tools.otherDrive.send(tools.testDrive, M.UserMessage(outlet.pipeId, inlet.pipeId, value))
        tools.otherDrive.expectNoMsg(2.seconds)
        tools.testTool.testPipe.getReceivedValues.contains(value) shouldEqual true}
      //Testing in Connected
      testMsgProcessing()
      //Testing in TurnedOn
      testPlumbing.send(tools.testDrive, M.TurnOnDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveTurnedOn)
      testMsgProcessing()
      sleep(1.second) //Wait for processing
      //Testing in Starting
      testPlumbing.send(tools.testDrive, M.StartDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStarted)    //Test drive in Working state
      //Testing in Working
      testMsgProcessing()
      //Testing in Stopping
      testPlumbing.send(tools.testDrive, M.StopDrive)
      testMsgProcessing()
      testPlumbing.expectMsg(M.DriveStopped)
      //Testing in Stopped
      testMsgProcessing()}
    "by UserMessage, processing of messages with no load response" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val value1 = randomDouble()
      val value2 = randomDouble()
      tools.testTool.testPipe.setProcTimeout(1.second)
      //Send first messages
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      tools.otherDrive.expectNoMsg(2.seconds)
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
      tools.testTool.testPipe.getReceivedValues.head shouldEqual value1
      //Send second messages
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      tools.otherDrive.expectNoMsg(2.seconds)
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
      tools.testTool.testPipe.getReceivedValues(1) shouldEqual value2}
    "by UserMessage, processing of messages with load response" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val value1 = randomDouble()
      val value2 = randomDouble()
      tools.testTool.testPipe.setProcTimeout(2.second)
      //Send message
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      tools.otherDrive.expectNoMsg(1.seconds)
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      //Load messages
      val driveLoad1 = tools.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.subscriberId shouldEqual Tuple2(tools.testDrive, testInlet.pipeId)
      driveLoad1.outletId shouldEqual otherOutlet.pipeId
      driveLoad1.inletQueueSize shouldEqual 1
      sleep(1.second) //Wait for end processing of first message
      val driveLoad2 = tools.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.subscriberId shouldEqual Tuple2(tools.testDrive, testInlet.pipeId)
      driveLoad2.outletId shouldEqual otherOutlet.pipeId
      driveLoad2.inletQueueSize shouldEqual 0
      //Check of message processing
      sleep(3.seconds) //Wait for second messages will processed
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)}
    "by UserMessage, in case message processing time out send warning to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val value1 = randomDouble()
      tools.testTool.testPipe.setProcTimeout(5.second)
      //Send message
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      sleep(3.seconds) //Wait for messages long timeout
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      //Check of message processing
      sleep(1.second) //Wait for second messages will processed
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, in case message processing error send error to user logger" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Send and get error
      tools.testTool.testPipe.setProcError(Some(new Exception("Oops!!!")))
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.seconds) //Wait for second messages will processed
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1)
      //Send and not get error
      tools.testTool.testPipe.setProcError(None)
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      sleep(1.seconds) //Wait for second messages will processed
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)}
    "by DriveLoad, evaluate message handling timeout" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val subId = (tools.otherDrive.ref, otherInlet.pipeId)
      val queueSize = randomInt(100, 1000)
      //Test for first message
      tools.otherDrive.send(tools.testDrive, M.DriveLoad(subId, testOutlet.pipeId, queueSize))
      sleep(1.second) //Wait for processing
      tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._3(subId) shouldEqual queueSize
      val pushTimeout1 = tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2
      pushTimeout1 shouldEqual  Some(queueSize * testDriveConfig.pushTimeoutCoefficient)
      //Test for second message
      tools.otherDrive.send(tools.testDrive, M.DriveLoad(subId, testOutlet.pipeId, 0))
      sleep(1.second) //Wait for processing
      tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._3(subId) shouldEqual 0
      val pushTimeout2 = tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2
      pushTimeout2 shouldEqual None}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      tools.testTool.testPipe.setProcTimeout(7.second)
      val value1 = randomDouble()
      //Send message
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      sleep(1.seconds) //Small timeout
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarn = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarn: " + logWarn)
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      sleep(1.second) //Wait for second messages will processed
      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1)}
    "by UserMessage, terminate in case incorrect inlet ID" in new TestCase {
      //Preparing
      val (_, testInlet, otherOutlet, _) = tools.connectedTools
      testPlumbing.watch(tools.testDrive)
      //Send user messages
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, 123456789, randomDouble())) //Incorrect inlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual tools.testDrive}
    "by DriveLoad, terminate in case incorrect outlet ID" in new TestCase {
      //Preparing
      val (_, _, _, otherInlet) = tools.connectedTools
      testPlumbing.watch(tools.testDrive)
      val subId = (tools.otherDrive.ref, otherInlet.pipeId)
      val queueSize = randomInt(100, 1000)
      //Send DriveLoad
      tools.otherDrive.send(tools.testDrive, M.DriveLoad(subId, 123456789, queueSize)) //Incorrect outlet ID
      //Expect drive termination
      testPlumbing.expectMsgType[Terminated].actor shouldEqual tools.testDrive}
  }
  "On stopping" should{
    "by StopDrive, run user stop function and reply with DriveStopped" in new TestCase {
      //Preparing
      tools.connectedTools
      //Test
      testPlumbing.send(tools.testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      tools.testTool.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
      //Preparing
      tools.connectedTools
      tools.testTool.setProcTimeout(5.second)
      //Test
      testPlumbing.send(tools.testDrive, M.StopDrive)
      sleep(3.second) //Wait for LogWarning will send
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      tools.testTool.isOnStopCalled shouldEqual true}
    "by StopDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStopped" in new TestCase {
      //Preparing
      tools.connectedTools
      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
      //Test
      testPlumbing.send(tools.testDrive, M.StopDrive)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      tools.testTool.isOnStopCalled shouldEqual true}
    "by TurnOffDrive, stop receive new user msgs, wait for empty queues, and response DriveTurnedOff " in new TestCase {
      //Preparing
      val value1 = randomDouble()
      val value2 = randomDouble()
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      testPlumbing.send(tools.testDrive, M.StopDrive)
      testPlumbing.expectMsg(M.DriveStopped)
      val infoMsg  = testUserLogging.expectMsgType[M.LogInfo]
      println("[PumpAndDriveTest] infoMsg: " + infoMsg)
      tools.testTool.isOnStopCalled shouldEqual true
      tools.testTool.testPipe.setProcTimeout(3.second)
      testPlumbing.send(tools.testDrive, M.DriveStopped)
      //Send two slow messages
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
      val driveLoad1 = tools.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
      driveLoad1.inletQueueSize shouldEqual 1
      //Send TurnOffDrive
      testPlumbing.send(tools.testDrive, M.TurnOffDrive)
      sleep(1.second) //Wait for will processed
      //Send two messages, which will not processed
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -1))
      tools.otherDrive.send(tools.testDrive, M.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -2))
      tools.otherDrive.expectNoMsg(1.second)
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError1: " + logError1)
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError2: " + logError2)
      //The first slow message processed
      val driveLoad2 = tools.otherDrive.expectMsgType[M.DriveLoad]
      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
      driveLoad2.inletQueueSize shouldEqual 0
      //Test received
      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)
      //Expect no more messages
      testUserLogging.expectNoMsg(3.second)
      tools.otherDrive.expectNoMsg(3.second)
      testPlumbing.expectNoMsg(3.second)}
    "by SkipTimeoutTask, not skip task if no timeout, and skip if it is" in new TestCase {
      //Preparing
      tools.connectedTools
      tools.testTool.setProcTimeout(7.second)
      //Test not skip
      testPlumbing.send(tools.testDrive, M.StopDrive)
      sleep(1.second) //Small timeout
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      testUserLogging.expectNoMsg(2.second) //SkipTimeoutTask should not have effect till LogWarning
      //Test skip
      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
      println("[PumpAndDriveTest] logWarning: " + logWarning)
      testPlumbing.send(tools.testDrive, M.SkipTimeoutTask)
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[PumpAndDriveTest] logError: " + logError)
      testPlumbing.expectMsg(M.DriveStopped)
      tools.testTool.isOnStopCalled shouldEqual true}
  }
  "In work" should{
    "by SetVisualisationLaval, update visualisation laval" in new TestCase {
      //Preparing
      tools.builtTool
      val newVisualisationLaval = randomVisualisationLaval()
      //Send
      tools.testDrive ! M.SetVisualisationLaval(newVisualisationLaval)
      //Check
      tools.testDrive.askForState[DriveState].visualisationLaval shouldEqual newVisualisationLaval}
//TODO Переписать при разработке поддержки UI инструментов
//    "by ShowToolUi, should call onShowUi()" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPlumbing.send(tools.testDrive, M.ShowToolUi)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnShowUICalled shouldEqual true}
//    "by ShowToolUi if onShowUi() time out, should log warning in user logger" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcTimeout(5.second)
//      //Test
//      testPlumbing.send(tools.testDrive, M.ShowToolUi)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnShowUICalled shouldEqual true}
//    "by ShowToolUi if onShowUi() fail, should log error in user logger" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPlumbing.send(tools.testDrive, M.ShowToolUi)
//      val logError = testUserLogging.expectMsgType[M.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnShowUICalled shouldEqual true}
//    "by HideToolUi, should call onHideUi()" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPlumbing.send(tools.testDrive, M.HideToolUi)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnHideUICalled shouldEqual true}
//    "by HideToolUi if onHideUi() timeout, should log warning in user logger" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcTimeout(5.second)
//      //Test
//      testPlumbing.send(tools.testDrive, M.HideToolUi)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[M.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnHideUICalled shouldEqual true}
//    "by HideToolUi if onHideUi() fail, should log error in user logger" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPlumbing.send(tools.testDrive, M.HideToolUi)
//      val logError = testUserLogging.expectMsgType[M.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPlumbing.expectNoMsg(1.second)
//      tools.testTool.isOnHideUICalled shouldEqual true}
  }
}
