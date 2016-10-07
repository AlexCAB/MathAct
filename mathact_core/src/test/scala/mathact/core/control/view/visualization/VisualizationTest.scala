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

package mathact.core.control.view.visualization

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.VisualizationConfigLike
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
    val workbenchController = TestProbe("TestSketchController_" + randomString())
    //UI Actor
    val visualization = system.actorOf(Props(new VisualizationActor(testConfig, workbenchController.ref)))
    workbenchController.watch(visualization)}
  //Testing
  "Visualization" should{
    "visualise tools" in new TestCase {
      //Show UI
      workbenchController.send(visualization, M.ShowVisualizationUI)
      workbenchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual true
      sleep(2.second)
      //






      //Test close button
      println("[VisualizationTest] Click close button (X).")
      sleep(2.second)
      workbenchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual false
      workbenchController.send(visualization, M.ShowVisualizationUI)
      workbenchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Hide UI
      workbenchController.send(visualization, M.HideVisualizationUI)
      workbenchController.expectMsgType[M.VisualizationUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Terminate UI
      workbenchController.send(visualization, M.TerminateVisualization)
      workbenchController.expectMsg(M.VisualizationTerminated)
      workbenchController.expectTerminated(visualization)}
  }
}
