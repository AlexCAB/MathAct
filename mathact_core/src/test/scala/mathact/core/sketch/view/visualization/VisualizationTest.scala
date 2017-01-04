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

package mathact.core.sketch.view.visualization

import akka.actor.{PoisonPill, Props}
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.VisualizationConfigLike
import mathact.core.model.data.visualisation._
import mathact.core.model.holders.SketchControllerRef
import mathact.core.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._


/** Visualization test
  * Created by CAB on 28.09.2016.
  */

class VisualizationTest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val testConfig = new VisualizationConfigLike{}
    //Helpers actors
    val sketchController = TestProbe("TestSketchController_" + randomString())
    //UI Actor
    val visualization = system.actorOf(
      Props(new VisualizationActor(testConfig, SketchControllerRef(sketchController.ref))))
    sketchController.watch(visualization)}
  //Testing
  "Visualization" should{
    "visualise blocks" in new TestCase {
      //Show UI
      sketchController.send(visualization, M.ShowVisualizationUI)
      sketchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Fill graph
      val blockAOutlet1 = OutletInfo(
        blockId = 1,
        blockName = Some("A"),
        outletId = 1,
        outletName = None)
      sketchController.send(visualization, M.BlockConstructedInfo(BlockInfo(
        blockId = 1,
        blockName = "A",
        blockImagePath = None,
        inlets = Seq(),
        outlets = Seq(blockAOutlet1))))
      sleep(1.second)
      val blockBInlet1 = InletInfo(
        blockId = 2,
        blockName = Some("B"),
        inletId = 1,
        inletName = Some("i_1"))
      val blockBInlet2 = InletInfo(
        blockId = 2,
        blockName = Some("B"),
        inletId = 2,
        inletName = Some("i_2"))
      val blockBOutlet1 = OutletInfo(
        blockId = 2,
        blockName = Some("B"),
        outletId = 3,
        outletName = Some("o_1"))
      val blockBOutlet2 = OutletInfo(
        blockId = 2,
        blockName = Some("B"),
        outletId = 4,
        outletName = Some("o_2"))
      sketchController.send(visualization, M.BlockConstructedInfo(BlockInfo(
        blockId = 2,
        blockName = "B",
        blockImagePath = None,
        inlets = Seq(blockBInlet1, blockBInlet2),
        outlets = Seq(blockBOutlet1, blockBOutlet2))))
      sleep(1.second)
      sketchController.send(visualization, M.BlocksConnectedInfo(blockAOutlet1, blockBInlet1))
      sleep(1.second)
      sketchController.send(visualization, M.BlocksConnectedInfo(blockBOutlet1, blockBInlet1))
      sleep(1.second)
      val blockCInlet1 = InletInfo(
        blockId = 3,
        blockName = Some("C"),
        inletId = 1,
        inletName = Some("i_1"))
      val blockCInlet2 = InletInfo(
        blockId = 3,
        blockName = Some("C"),
        inletId = 2,
        inletName = Some("i_2"))
      val blockCOutlet1 = OutletInfo(
        blockId = 3,
        blockName = Some("C"),
        outletId = 3,
        outletName = Some("o_1"))
      sketchController.send(visualization, M.BlockConstructedInfo(BlockInfo(
        blockId = 3,
        blockName = "C",
        blockImagePath = Some("mathact/userLog/info_img.png"),
        inlets = Seq(blockCInlet1, blockCInlet2),
        outlets = Seq(blockCOutlet1))))
      sleep(1.second)
      sketchController.send(visualization, M.BlocksConnectedInfo(blockAOutlet1, blockCInlet1))
      sleep(1.second)
      sketchController.send(visualization, M.BlocksConnectedInfo(blockCOutlet1, blockBInlet2))
      sleep(1.second)
      sketchController.send(visualization, M.AllBlockBuilt)
      //Test close button
      println("[VisualizationTest] CLICK CLOSE BUTTON (X).")
      sketchController.expectMsgType[M.VisualizationUIChanged](30.second).isShow shouldEqual false
      sketchController.send(visualization, M.ShowVisualizationUI)
      sketchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual true
      //Time for playing
      sleep(30.second)
      //Hide UI
      sketchController.send(visualization, M.HideVisualizationUI)
      sketchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Terminate UI
      visualization ! PoisonPill
      sketchController.expectTerminated(visualization)
    }
  }
}
