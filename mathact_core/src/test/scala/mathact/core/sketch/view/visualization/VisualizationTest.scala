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

package mathact.core.sketch.view.visualization

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.VisualizationConfigLike
import mathact.core.model.data.visualisation._
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
    val visualization = system.actorOf(Props(new VisualizationActor(testConfig, sketchController.ref)))
    sketchController.watch(visualization)}
  //Testing
  "Visualization" should{
    "visualise blocks" in new TestCase {
      //Show UI
      sketchController.send(visualization, M.ShowVisualizationUI)
      sketchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Fill graph
      sketchController.send(visualization, M.BlockBuilt(BlockBuiltInfo(
        blockId = 1,
        blockName = "A",
        blockImagePath = None,
        inlets = Map(),
        outlets = Map(
          1 → OutletConnectionsInfo(
            blockId = 1,
            outletId = 1,
            outletName = None,
            subscribers = List(
              SubscriberInfo(blockId = 2, inletId = 1)))))))
      sleep(1.second)
      sketchController.send(visualization, M.BlockBuilt(BlockBuiltInfo(
        blockId = 2,
        blockName = "B",
        blockImagePath = None,
        inlets = Map(
          1 → InletConnectionsInfo(
            blockId = 2,
            inletId = 1,
            inletName = Some("i_1"),
            publishers = List(
              PublisherInfo(blockId = 1, outletId = 1),
              PublisherInfo(blockId = 2, outletId = 3))),
          2 → InletConnectionsInfo(
            blockId = 2,
            inletId = 2,
            inletName = Some("i_2"),
            publishers = List())),
        outlets = Map(
          3 → OutletConnectionsInfo(
            blockId = 2,
            outletId = 3,
            outletName = Some("o_1"),
            subscribers = List(
              SubscriberInfo(blockId = 2, inletId = 1),
              SubscriberInfo(blockId = 3, inletId = 1))),
          4 → OutletConnectionsInfo(
            blockId = 2,
            outletId = 4,
            outletName = Some("o_2"),
            subscribers = List(
              SubscriberInfo(blockId = 3, inletId = 2)))))))
      sleep(1.second)
      sketchController.send(visualization, M.BlockBuilt(BlockBuiltInfo(
        blockId = 3,
        blockName = "C",
        blockImagePath = Some("mathact/userLog/info_img.png"),
        inlets = Map(
          1 → InletConnectionsInfo(
            blockId = 3,
            inletId = 1,
            inletName = Some("i_1"),
            publishers = List(
              PublisherInfo(blockId = 2, outletId = 3))),
          2 → InletConnectionsInfo(
            blockId = 3,
            inletId = 2,
            inletName = Some("i_2"),
            publishers = List(
              PublisherInfo(blockId = 2, outletId = 4)))),
        outlets = Map(
          3 → OutletConnectionsInfo(
            blockId = 3,
            outletId = 3,
            outletName = Some("o_1"),
            subscribers = List())))))
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
//      //Terminate UI
//      sketchController.send(visualization, M.TerminateVisualization)
//      ??? //sketchController.expectMsg(M.VisualizationTerminated)
//      sketchController.expectTerminated(visualization)
    }
  }
}
