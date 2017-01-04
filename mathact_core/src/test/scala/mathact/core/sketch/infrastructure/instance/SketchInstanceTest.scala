/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package mathact.core.sketch.infrastructure.instance

import akka.actor.{Props, ActorRef}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.core.ActorTestSpec
import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.data.SketchData
import mathact.core.dummies.{TestSketchWithBigTimeout, TestSketchWithError, TestSketchWithSmallTimeout}
import mathact.core.model.config._
import mathact.core.model.holders.{LayoutRef, PlumbingRef, UserLoggingRef, SketchControllerRef}
import mathact.core.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of SketchInstanceActor
  * Created by CAB on 17.10.2016.
  */

class SketchInstanceTest extends ActorTestSpec {
  //Test model
  protected trait TestCase extends Suite{
    //Test config
    val testSketchInstanceConfig = new SketchInstanceConfigLike{
      val commonConfig = ConfigFactory.load()
      val sketchBuildingTimeout = 5.second
      val pumpConfig = new PumpConfigLike{
        val askTimeout = Timeout(1.second)}}
    //Test SketchData
    def newTestSketchData(clazz: Class[_] = classOf[TestSketchWithSmallTimeout]) = SketchData(
      clazz,
      className = clazz.getName,
      sketchName = Some("TestSketch1"),
      sketchDescription = Some("Testing sketch 1"),
      autorun = false,
      showUserLogUiAtStart = false,
      showVisualisationUiAtStart = false)
    //Helpers actors
    lazy val testActor = TestProbe("testActor_" + randomString())
    lazy val testSketchController = TestProbe("TestSketchController_" + randomString())
    lazy val testUserLogging = TestProbe("TestUserLogging_" + randomString())
    lazy val testPlumbing = TestProbe("TestPlumbing_" + randomString())
    lazy val testLayout = TestProbe("Layout_" + randomString())
    //Sketch instance actor
    def newSketchInstanceActor(sketchData: SketchData): ActorRef = {
      val controller = system.actorOf(Props(
        new SketchInstanceActor(
          testSketchInstanceConfig,
          sketchData,
          SketchControllerRef(testSketchController.ref),
          UserLoggingRef(testUserLogging.ref),
          PlumbingRef(testPlumbing.ref),
          LayoutRef(testLayout.ref))),
        "SketchInstanceActor_" + randomString())
      testSketchController.watch(controller)
      controller}}
  //Testing
  "SketchInstanceActor on start" should{
    "by CreateSketchInstance, create sketch instance and response with SketchInstanceReady" in new TestCase {
      //Preparing
      val controller = newSketchInstanceActor(newTestSketchData())
      //Create
      testSketchController.send(controller, M.CreateSketchInstance)
      sleep(1.second) //Some building timeout
      //Get context
      testActor.send(controller, M.BuildSketchContextFor(testActor.ref))
      val context = testActor.expectMsgType[Either[Exception, BlockContext]]
      println("[SketchInstanceTest] context: " + context)
      //Expect SketchInstanceReady
      val instance = testSketchController.expectMsgType[M.SketchInstanceReady]
      println("[SketchInstanceTest] instance: " + instance)
      //Expect LogInfo
      val logInfo = testUserLogging.expectMsgType[M.LogInfo]
      println("[SketchInstanceTest] logInfo: " + logInfo)
      //Expect no messages
      testActor.expectNoMsg(1.second)
      testSketchController.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by CreateSketchInstance, response with SketchInstanceFail context not created" in new TestCase {
      //Preparing
      val controller = newSketchInstanceActor(newTestSketchData(classOf[TestSketchWithError]))
      //Create
      testSketchController.send(controller, M.CreateSketchInstance)
      sleep(1.second) //Some building timeout
      //Expect SketchInstanceReady
      val fail = testSketchController.expectMsgType[M.SketchInstanceError]
      println("[SketchInstanceTest] fail: " + fail)
      //Expect LogInfo
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[SketchInstanceTest] logError: " + logError)
      //Expect no messages
      testActor.expectNoMsg(1.second)
      testSketchController.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by CreateSketchInstance, response with SketchInstanceFail if exception on creating" in new TestCase {
      //Preparing
      val controller = newSketchInstanceActor(newTestSketchData(classOf[TestSketchWithError]))
      //Create
      testSketchController.send(controller, M.CreateSketchInstance)
      sleep(1.second) //Some building timeout
      //Get context
      testActor.send(controller, M.BuildSketchContextFor(testActor.ref))
      val context = testActor.expectMsgType[Either[Exception, BlockContext]]
      println("[SketchInstanceTest] context: " + context)
      //Expect SketchInstanceReady
      val fail = testSketchController.expectMsgType[M.SketchInstanceError]
      println("[SketchInstanceTest] fail: " + fail)
      //Expect LogInfo
      val logError = testUserLogging.expectMsgType[M.LogError]
      println("[SketchInstanceTest] logError: " + logError)
      //Expect no messages
      testActor.expectNoMsg(1.second)
      testSketchController.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
    "by CreateSketchInstance, response with SketchInstanceFail if timeout of creating" in new TestCase {
      //Preparing
      val controller = newSketchInstanceActor(newTestSketchData(classOf[TestSketchWithBigTimeout]))
      //Create
      testSketchController.send(controller, M.CreateSketchInstance)
      sleep(1.second) //Some building timeout
      //Get context
      testActor.send(controller, M.BuildSketchContextFor(testActor.ref))
      val context = testActor.expectMsgType[Either[Exception, BlockContext]]
      println("[SketchInstanceTest] context: " + context)
      sleep(4.second) //Wait timeout
      //Expect SketchInstanceReady
      val fail = testSketchController.expectMsgType[M.SketchInstanceError]
      println("[SketchInstanceTest] fail: " + fail)
      //Expect LogInfo
      val logError1 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchInstanceTest] logError1: " + logError1)
      sleep(2.second) //Wait for end build
      //Expect LogInfo
      val logError2 = testUserLogging.expectMsgType[M.LogError]
      println("[SketchInstanceTest] logError2: " + logError2)
      //Expect no messages
      testActor.expectNoMsg(1.second)
      testSketchController.expectNoMsg(1.second)
      testUserLogging.expectNoMsg(1.second)
      testPlumbing.expectNoMsg(1.second)}
  }
}
