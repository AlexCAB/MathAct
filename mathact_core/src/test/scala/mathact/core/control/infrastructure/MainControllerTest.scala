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

package mathact.core.control.infrastructure

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums.SketchStatus
import mathact.core.ActorTestSpec
import mathact.core.model.config.MainConfigLike
import mathact.core.model.messages.M
import org.scalatest.Suite
import scala.concurrent.duration._


/** Testing of main controller
  * Created by CAB on 12.10.2016.
  */

class MainControllerTest extends ActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val testMainConfig = new MainConfigLike{
      val config = ConfigFactory.load()
      val sketchBuildingTimeout = 5.second
      val pumping = null
      val mainUI = null
      val sketchUI = null
      val userLogging = null
      val visualization = null}
    //Sketch data
    val sketchData = SketchData(
      clazz = this.getClass,
      className = "test.sketch.Class",
      sketchName = Some("My sketch"),
      sketchDescription = Some("My first sketch"),
      autorun = false,
      showUserLogUiAtStart = false,
      showVisualisationUiAtStart = false)
    //Helpers actors
    val testApplication = TestProbe("TestApplication_" + randomString())
    val testMainUi = TestProbe("TestMainUi_" + randomString())
    val testSketchController = TestProbe("TestSketchController_" + randomString())
    //Variables
    @volatile private var exitCode: Option[Int] = None
    //MainController
    val mainController = system.actorOf(Props(
      new MainController(testMainConfig, i ⇒ {exitCode = Some(i)}){
        val mainUi = testMainUi.ref
        def createSketchController(c: MainConfigLike, d: SketchData, mc: ActorRef): ActorRef = testSketchController.ref}),
      "TestAskMainController_" + randomString())
    testApplication.watch(mainController)
    //Methods
    def getExitCode: Option[Int] = exitCode}
  //Testing
  "MainController" should{
    "by MainControllerStart with no auto-run, set sketch list to UI and wait for user action" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData)))
      testMainUi.expectMsgType[M.SetSketchList].sketches.head.className shouldEqual sketchData.className
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testMainUi.expectMsg(M.TerminateMainUI)
      testMainUi.send(mainController, M.MainUITerminated)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "by MainControllerStart with auto-run, run sketch with autorun = true" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData.copy(autorun = true))))
      testSketchController.expectMsg(M.StartSketchController)
      testApplication.send(mainController, M.NewSketchContext(null, sketchData.className))
      testSketchController.expectMsgType[M.GetSketchContext].sender shouldEqual testApplication.ref
      testSketchController.send(mainController, M.SketchBuilt(sketchData.className, null))
      //Send SketchDone
      testSketchController.send(mainController, M.SketchDone(sketchData.className))
      val sketches1 = testMainUi.expectMsgType[M.SetSketchList].sketches
      sketches1.head.className shouldEqual sketchData.className
      sketches1.head.lastRunStatus shouldEqual SketchStatus.Ended
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testMainUi.expectMsg(M.TerminateMainUI)
      testMainUi.send(mainController, M.MainUITerminated)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "by RunSketch, run selected sketch" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData)))
      testMainUi.expectMsgType[M.SetSketchList].sketches.head.className shouldEqual sketchData.className
      //Run sketch
      testMainUi.send(mainController, M.RunSketch(sketchData.toSketchInfo(SketchStatus.Ready)))
      testSketchController.expectMsg(M.StartSketchController)
      testApplication.send(mainController, M.NewSketchContext(null, sketchData.className))
      testSketchController.expectMsgType[M.GetSketchContext].sender shouldEqual testApplication.ref
      testSketchController.send(mainController, M.SketchBuilt(sketchData.className, null))
      testMainUi.expectMsg(M.HideMainUI)
      //Send SketchError
      testSketchController.send(mainController, M.SketchError(sketchData.className, new Exception("Oops!!!")))
      val sketches1 = testMainUi.expectMsgType[M.SetSketchList].sketches
      sketches1.head.className shouldEqual sketchData.className
      sketches1.head.lastRunStatus shouldEqual SketchStatus.Failed
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testMainUi.expectMsg(M.TerminateMainUI)
      testMainUi.send(mainController, M.MainUITerminated)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
  }
}
