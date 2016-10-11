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

package mathact.core.control.view.main

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.MainUIConfigLike
import mathact.core.model.data.sketch.SketchInfo
import mathact.core.model.enums.SketchStatus
import mathact.core.model.messages.M
import org.scalatest.Suite
import scala.concurrent.duration._


/** Testing of main UI
  * Created by CAB on 11.10.2016.
  */

class MainUITest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val mainUIConfig = new MainUIConfigLike{}
    //Helpers actors
    val testMainController = TestProbe("TestMainController_" + randomString())
    //UI Actor
    val ui = system.actorOf(Props(
      new MainUIActor(mainUIConfig, testMainController.ref)),
      "MainUIActor_" + randomString())
    testMainController.watch(ui)}
  //Testing
  "MainUI" should{
    "show 'no sketches' list" in new TestCase {
      //Set empty list of sketch
      testMainController.send(ui, M.SetSketchList(List()))
      println("[MainUITest] PLEASE HIT CLOSE (X) BUTTON")
      testMainController.expectMsg(30.seconds, M.MainCloseBtnHit)
      //Terminate UI
      testMainController.send(ui, M.TerminateMainUI)
      testMainController.expectMsg(M.MainUITerminated)
      testMainController.expectTerminated(ui)}
    "show sketches list and run one of" in new TestCase {
      //Set empty list of sketch
      testMainController.send(ui, M.SetSketchList(List(
        SketchInfo(
          className = "sketch_class_1",
          sketchName = Some("Sketch A"),
          sketchDescription = Some("My first sketch"),
          lastRunStatus = SketchStatus.Ready),
        SketchInfo(
          className = "sketch_class_2",
          sketchName = None,
          sketchDescription = Some("My first sketch"),
          lastRunStatus = SketchStatus.Autorun),
        SketchInfo(
          className = "sketch_class_3",
          sketchName = Some("Sketch C"),
          sketchDescription = None,
          lastRunStatus = SketchStatus.Failed))))
      println("[MainUITest] PLEASE HIT FIRST RUN BUTTON")
      //Wait for hit of first sketch
      val ranSketch = testMainController.expectMsgType[M.RunSketch](30.seconds).sketch
      ranSketch.className shouldEqual "sketch_class_1"
      //Terminate UI
      testMainController.send(ui, M.TerminateMainUI)
      testMainController.expectMsg(M.MainUITerminated)
      testMainController.expectTerminated(ui)}
  }
}
