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

package mathact.core.sketch.view.sketch

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._
import scalafx.scene.paint.Color


/** Testing of SketchUI actor
  * Created by CAB on 21.09.2016.
  */

class SketchUITest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val sketchUIConfig = new SketchUIConfigLike{}
    //Helpers actors
    val workbenchController = TestProbe("TestSketchController_" + randomString())
    //UI Actor
    val ui = system.actorOf(Props(new SketchUIActor(sketchUIConfig, workbenchController.ref)), "SketchUI_" + randomString())
    workbenchController.watch(ui)}
  //Testing
  "SketchUI" should{
    "change UI view" in new TestCase {
      //Preparing
      import SketchUIElement._
      import SketchUiElemState._
      //Show UI
      workbenchController.send(ui, M.ShowSketchUI)
      workbenchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Buttons test
      workbenchController.send(ui, M.SetSketchUIStatusString("Do hit active button...", Color.Red))
      //Buttons test: LogBtn show
      workbenchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemShow)))
      val logActS = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActS.element shouldEqual LogBtn
      logActS.action shouldEqual ElemShow
      //Buttons test: LogBtn hide
      workbenchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemHide)))
      val logActH = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActH.element shouldEqual LogBtn
      logActH.action shouldEqual ElemHide
      //Buttons test: VisualisationBtn show
      workbenchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemShow)))
      val visualisationBtnS = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnS.element shouldEqual VisualisationBtn
      visualisationBtnS.action shouldEqual ElemShow
      //Buttons test: VisualisationBtn hide
      workbenchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemHide)))
      val visualisationBtnH = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnH.element shouldEqual VisualisationBtn
      visualisationBtnH.action shouldEqual ElemHide
      //Buttons test: RunBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(RunBtn → ElemEnabled)))
      val runBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      runBtn.element shouldEqual RunBtn
      runBtn.action shouldEqual ElemEnabled
      //Buttons test: ShowAllToolsUiBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(ShowAllToolsUiBtn → ElemEnabled)))
      val showAllToolsUiBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      showAllToolsUiBtn.element shouldEqual ShowAllToolsUiBtn
      showAllToolsUiBtn.action shouldEqual ElemEnabled
      //Buttons test: HideAllToolsUiBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(HideAllToolsUiBtn → ElemEnabled)))
      val hideAllToolsUiBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      hideAllToolsUiBtn.element shouldEqual HideAllToolsUiBtn
      hideAllToolsUiBtn.action shouldEqual ElemEnabled
      //Buttons test: SkipAllTimeoutTaskBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(SkipAllTimeoutTaskBtn → ElemEnabled)))
      val skipAllTimeoutTaskBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      skipAllTimeoutTaskBtn.element shouldEqual SkipAllTimeoutTaskBtn
      skipAllTimeoutTaskBtn.action shouldEqual ElemEnabled
      //Buttons test: StopSketchBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(StopSketchBtn → ElemEnabled)))
      val stopSketchBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      stopSketchBtn.element shouldEqual StopSketchBtn
      stopSketchBtn.action shouldEqual ElemEnabled
      //Close button
      workbenchController.send(ui, M.SetSketchUIStatusString("Do hit close (X) button...", Color.Red))
      val closeBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      closeBtn.element shouldEqual CloseBtn
      closeBtn.action shouldEqual Unit
      //Buttons test done
      workbenchController.send(ui, M.SetSketchUIStatusString("Buttons test done.", Color.Green))
      sleep(2.second)
      //Hide UI
      workbenchController.send(ui, M.HideSketchUI)
      workbenchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual false
      sleep(2.second)
//      //Terminate UI
//      workbenchController.send(ui, M.TerminateSketchUI)
//      ??? //workbenchController.expectMsg(M.SketchUITerminated)
//      workbenchController.expectTerminated(ui)
    }


  }
}
