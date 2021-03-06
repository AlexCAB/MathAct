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

package mathact.core.sketch.view.logging

import akka.actor.{PoisonPill, Props}
import akka.testkit.TestProbe
import mathact.core.UIActorTestSpec
import mathact.core.model.config.UserLoggingConfigLike
import mathact.core.model.holders.SketchControllerRef
import mathact.core.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of UserLogging actor
  * Created by CAB on 23.09.2016.
  */

class UserLoggingTest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    protected def newConfig(showOnErr: Boolean) = new UserLoggingConfigLike{
      val uiFxmlPath = "mathact/userLog/ui.fxml"
      val showUIOnError = showOnErr}
    //Helpers actors
    val sketchController = TestProbe("TestSketchController_" + randomString())
    //UI Actor
    protected def newUserLog(config: UserLoggingConfigLike) = system.actorOf(
      Props(new UserLoggingActor(config, SketchControllerRef(sketchController.ref))),
      "UserLogging_" + randomString())}
  //Testing
  "UserLogging" should{
    "log events" in new TestCase {
      //Preparing
      val userLog = newUserLog(newConfig(showOnErr = false))
      sketchController.watch(userLog)
      //Show UI
      sketchController.send(userLog, M.ShowUserLoggingUI)
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Log events
      sketchController.send(userLog, M.LogInfo(
        blockId = Some(1001),
        blockName = "Block 1",
        message = "Info message."))
      sleep(2.second)
      sketchController.send(userLog, M.LogInfo(
        blockId = Some(1001),
        blockName = "Block 1",
        message = "Long info message: \n" +
          "A dream written down with a date becomes a Goal. \n" +
          "A goal broken down into steps becomes a plan. \n" +
          "A plan backed by action makes your dreams come true. \n"))
      sleep(2.second)
      sketchController.send(userLog, M.LogWarning(
        blockId = Some(1002),
        blockName = "Block 2",
        message = "Warning message."))
      sleep(2.second)
      sketchController.send(userLog, M.LogError(
        blockId = Some(1003),
        blockName = "Block 3",
        errors = Seq(new Exception("Oops!!! But not worries, this is just a test :)")),
        message = "!!!Error message.!!!"))
      sleep(2.second)
      //Test close button
      sketchController.send(userLog, M.LogInfo(None, "TESTING", "Click close button (X)."))
      sketchController.expectMsgType[M.UserLoggingUIChanged](30.second).isShow shouldEqual false
      sketchController.send(userLog, M.ShowUserLoggingUI)
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      //Test done
      sketchController.send(userLog, M.LogInfo(None, "TESTING", "Test done, you can play with UI next 30 second"))
      sleep(30.second)  //Time for playing with UI
      //Terminate UI
      userLog ! PoisonPill
      sketchController.expectTerminated(userLog)}
    "show logger on error" in new TestCase {
      //Preparing
      val userLog = newUserLog(newConfig(showOnErr = true))
      sketchController.watch(userLog)
      //Log error 1
      sketchController.send(userLog, M.LogError(
        blockId = Some(1001),
        blockName = "Block 1",
        errors = Seq(new Exception("Oops!!!")),
        message = "!!!ERROR!!!"))
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Hide UI
      sketchController.send(userLog, M.HideUserLoggingUI)
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Log error 2
      sketchController.send(userLog, M.LogError(
        blockId = Some(1002),
        blockName = "Block 2",
        errors = Seq(new Exception("Oops!!!")),
        message = "!!!ERROR!!!"))
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Hide UI
      sketchController.send(userLog, M.HideUserLoggingUI)
      sketchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Terminate UI
      userLog ! PoisonPill
      sketchController.expectTerminated(userLog)
    }
  }
}
