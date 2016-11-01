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

package mathact.core.app.infrastructure

import akka.actor.{PoisonPill, ActorRef, Props}
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import mathact.core.bricks.data.SketchData
import mathact.core.model.enums.SketchStatus
import mathact.core.ActorTestSpec
import mathact.core.model.config.MainConfigLike
import mathact.core.model.messages.M
import org.scalatest.Suite


/** Testing of main controller
  * Created by CAB on 12.10.2016.
  */

class MainControllerTest extends ActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    lazy val testMainConfig = new MainConfigLike{
      val config = ConfigFactory.load()
      val sketchInstance = null
      val plumbing = null
      val mainUI = null
      val sketchUI = null
      val userLogging = null
      val visualization = null}
    //Sketch data
    lazy val sketchData = SketchData(
      clazz = this.getClass,
      className = "test.sketch.Class",
      sketchName = Some("My sketch"),
      sketchDescription = Some("My first sketch"),
      autorun = false,
      showUserLogUiAtStart = false,
      showVisualisationUiAtStart = false)
    //Helpers actors
    lazy val testApplication = TestProbe("TestApplication_" + randomString())
    lazy val testMainUi = TestProbe("TestMainUi_" + randomString())
    lazy val testSketchController = TestProbe("TestSketchController_" + randomString())
    //Variables
    @volatile private var exitCode: Option[Int] = None
    //MainController
    lazy val mainController = system.actorOf(Props(
      new MainController(testMainConfig, i ⇒ {exitCode = Some(i)}){
        val mainUi = testMainUi.ref
        context.watch(testMainUi.ref)
        def createSketchController(c: MainConfigLike, d: SketchData): ActorRef = testSketchController.ref}),
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
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "by MainControllerStart with auto-run, run sketch with autorun = true" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData.copy(autorun = true))))
      testSketchController.expectMsg(M.LaunchSketch)
      testApplication.send(mainController, M.NewSketchContext(null, sketchData.className))
      testSketchController.expectMsgType[M.GetSketchContext].sender shouldEqual testApplication.ref
      testSketchController.send(mainController, M.SketchBuilt(sketchData.className))
      testMainUi.expectMsg(M.HideMainUI)
      //Send SketchDone
      testSketchController.send(mainController, M.SketchDone(sketchData.className))
      testSketchController.testActor ! PoisonPill
      val sketches1 = testMainUi.expectMsgType[M.SetSketchList].sketches
      sketches1.head.className shouldEqual sketchData.className
      sketches1.head.lastRunStatus shouldEqual SketchStatus.Ended
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "by RunSketch, run selected sketch" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData)))
      testMainUi.expectMsgType[M.SetSketchList].sketches.head.className shouldEqual sketchData.className
      //Run sketch
      testMainUi.send(mainController, M.RunSketch(sketchData.toSketchInfo(SketchStatus.Ready)))
      testSketchController.expectMsg(M.LaunchSketch)
      testApplication.send(mainController, M.NewSketchContext(null, sketchData.className))
      testSketchController.expectMsgType[M.GetSketchContext].sender shouldEqual testApplication.ref
      testSketchController.send(mainController, M.SketchFail(sketchData.className))
      testMainUi.expectMsg(M.HideMainUI)
      //Send SketchError
      testSketchController.send(mainController, M.SketchError(sketchData.className, Seq(new Exception("Oops!!!"))))
      testSketchController.testActor ! PoisonPill
      val sketches1 = testMainUi.expectMsgType[M.SetSketchList].sketches
      sketches1.head.className shouldEqual sketchData.className
      sketches1.head.lastRunStatus shouldEqual SketchStatus.Failed
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "if sketch controller terminated, mark it Failed and show main UI" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData)))
      testMainUi.expectMsgType[M.SetSketchList].sketches.head.className shouldEqual sketchData.className
      //Run sketch
      testMainUi.send(mainController, M.RunSketch(sketchData.toSketchInfo(SketchStatus.Ready)))
      testSketchController.expectMsg(M.LaunchSketch)
      testApplication.send(mainController, M.NewSketchContext(null, sketchData.className))
      testSketchController.expectMsgType[M.GetSketchContext].sender shouldEqual testApplication.ref
      testSketchController.send(mainController, M.SketchBuilt(sketchData.className))
      testMainUi.expectMsg(M.HideMainUI)
      //Send Terminated(SketchControllerActor)
      testSketchController.testActor ! PoisonPill
      val sketches1 = testMainUi.expectMsgType[M.SetSketchList].sketches
      sketches1.head.className shouldEqual sketchData.className
      sketches1.head.lastRunStatus shouldEqual SketchStatus.Failed
      //Stop app
      testMainUi.send(mainController, M.MainCloseBtnHit)
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(0)}
    "if main UI terminated, stop application" in new TestCase {
      //Send MainControllerStart
      testApplication.send(mainController, M.MainControllerStart(List(sketchData)))
      testMainUi.expectMsgType[M.SetSketchList].sketches.head.className shouldEqual sketchData.className
      //Send Terminated(MainUI)
      testMainUi.testActor ! PoisonPill
      testApplication.expectTerminated(mainController)
      getExitCode shouldEqual Some(-1)}
  }
}
