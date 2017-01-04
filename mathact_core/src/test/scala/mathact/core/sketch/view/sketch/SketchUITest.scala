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

package mathact.core.sketch.view.sketch

import akka.actor.{PoisonPill, Props}
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.holders.SketchControllerRef
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
    val sketchController = TestProbe("TestSketchController_" + randomString())
    //UI Actor
    val ui = system.actorOf(
      Props(new SketchUIActor(sketchUIConfig, SketchControllerRef(sketchController.ref))),
      "SketchUI_" + randomString())
    sketchController.watch(ui)}
  //Testing
  "SketchUI" should{
    "change UI view" in new TestCase {
      //Preparing
      import SketchUIElement._
      import SketchUiElemState._
      //Show UI
      sketchController.send(ui, M.ShowSketchUI)
      sketchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Update Title
      sketchController.send(ui, M.UpdateSketchUITitle("Testing..."))
      sleep(2.second)
      //Buttons test
      sketchController.send(ui, M.SetSketchUIStatusString("Do hit active button...", Color.Red))
      //Buttons test: LogBtn show
      sketchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemShow)))
      val logActS = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActS.element shouldEqual LogBtn
      logActS.action shouldEqual ElemShow
      //Buttons test: LogBtn hide
      sketchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemHide)))
      val logActH = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActH.element shouldEqual LogBtn
      logActH.action shouldEqual ElemHide
      //Buttons test: VisualisationBtn show
      sketchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemShow)))
      val visualisationBtnS = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnS.element shouldEqual VisualisationBtn
      visualisationBtnS.action shouldEqual ElemShow
      //Buttons test: VisualisationBtn hide
      sketchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemHide)))
      val visualisationBtnH = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnH.element shouldEqual VisualisationBtn
      visualisationBtnH.action shouldEqual ElemHide
      //Buttons test: RunBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(RunBtn → ElemEnabled)))
      val runBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      runBtn.element shouldEqual RunBtn
      runBtn.action shouldEqual ElemEnabled
      //Buttons test: LayoutFillBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(LayoutFillBtn → ElemEnabled)))
      val layoutFillBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      layoutFillBtn.element shouldEqual LayoutFillBtn
      layoutFillBtn.action shouldEqual ElemEnabled
      //Buttons test: LayoutStairsBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(LayoutStairsBtn → ElemEnabled)))
      val layoutStairsBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      layoutStairsBtn.element shouldEqual LayoutStairsBtn
      layoutStairsBtn.action shouldEqual ElemEnabled
      //Buttons test: ShowAllBlocksUiBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(ShowAllBlocksUiBtn → ElemEnabled)))
      val showAllBlocksUiBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      showAllBlocksUiBtn.element shouldEqual ShowAllBlocksUiBtn
      showAllBlocksUiBtn.action shouldEqual ElemEnabled
      //Buttons test: HideAllBlocksUiBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(HideAllBlocksUiBtn → ElemEnabled)))
      val hideAllBlocksUiBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      hideAllBlocksUiBtn.element shouldEqual HideAllBlocksUiBtn
      hideAllBlocksUiBtn.action shouldEqual ElemEnabled
      //Buttons test: SkipAllTimeoutTaskBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(SkipAllTimeoutTaskBtn → ElemEnabled)))
      val skipAllTimeoutTaskBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      skipAllTimeoutTaskBtn.element shouldEqual SkipAllTimeoutTaskBtn
      skipAllTimeoutTaskBtn.action shouldEqual ElemEnabled
      //Buttons test: StopSketchBtn
      sketchController.send(ui, M.UpdateSketchUIState(Map(StopSketchBtn → ElemEnabled)))
      val stopSketchBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      stopSketchBtn.element shouldEqual StopSketchBtn
      stopSketchBtn.action shouldEqual ElemEnabled
      //Close button
      sketchController.send(ui, M.SetSketchUIStatusString("Do hit close (X) button...", Color.Red))
      val closeBtn = sketchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      closeBtn.element shouldEqual CloseBtn
      closeBtn.action shouldEqual Unit
      //Buttons test done
      sketchController.send(ui, M.SetSketchUIStatusString("Buttons test done.", Color.Green))
      sleep(2.second)
      //Hide UI
      sketchController.send(ui, M.HideSketchUI)
      sketchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Terminate UI
      ui ! PoisonPill
      sketchController.expectTerminated(ui)
    }
  }
}
