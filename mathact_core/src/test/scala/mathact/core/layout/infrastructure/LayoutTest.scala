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

package mathact.core.layout.infrastructure

import javafx.geometry.Rectangle2D

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.core.ActorTestSpec
import mathact.core.model.config.LayoutConfigLike
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.enums._
import mathact.core.model.holders.{DriveRef, SketchControllerRef}
import mathact.core.model.messages.M
import org.scalatest.Suite
import concurrent.duration._


/** Testing of layout
  * Created by CAB on 04.11.2016.
  */

class LayoutTest extends ActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Definitions
    case class ActorState(windows: Map[(DriveRef, Int), (WindowState, WindowPreference)])
    //Test config
    val testConfig = new LayoutConfigLike{
      val initialLayoutKind = WindowsLayoutKind.WindowsStairs
      val screenIndent = 10
      val stairsStep = 30}
    //Helpers actors
    lazy val testSketchController = TestProbe("TestSketchController_" + randomString())
    lazy val testPlumbing = TestProbe("TestPlumbing_" + randomString())
    lazy val testDrive1 = TestProbe("TestDrive1_" + randomString())
    lazy val testDrive2 = TestProbe("TestDrive2_" + randomString())
    //Data generation
    def newWindowState(title: String) = WindowState(
      isShown = true,
      x = randomDouble(),
      y = randomDouble(),
      h = randomDouble(),
      w = randomDouble(),
      title)
     def newWindowPreference = WindowPreference(
       prefX = randomOpt(randomDouble()),
       prefY = randomOpt(randomDouble()))
    //Test windows
    lazy val window1 =  M.RegisterWindow(DriveRef(testDrive1.ref), 1, newWindowState("t1"), newWindowPreference)
    lazy val window2 =  M.RegisterWindow(DriveRef(testDrive2.ref), 1, newWindowState("t2"), newWindowPreference)
    //Layout
    object actors{
      lazy val layout = system.actorOf(
        Props(new LayoutActor(testConfig, SketchControllerRef(testSketchController.ref), new Rectangle2D(0,0,800,600)){
          override  def receive: PartialFunction[Any, Unit] = {
            case GetDriveState ⇒ sender ! ActorState(windows
              .map{ case d ⇒
                val id = (d.drive,d.windowId)
                (id, (states(id), d.preference))}
              .toMap)
            case m ⇒ super.receive.apply(m)}}),
        "TestLayout_" + randomString())
      lazy val layoutWithWindows = {
        layout
        testDrive1.send(layout, window1)
        testDrive2.send(layout, window2)
        testPlumbing.send(layout, M.AllUiInitialized)
        testPlumbing.send(actors.layout, M.AllUiCreated)
        testDrive1.expectMsgType[M.SetWindowPosition]
        testDrive1.send(layout, M.WindowPositionUpdated(windowId = 1))
        testDrive2.expectMsgType[M.SetWindowPosition]
        testDrive2.send(layout, M.WindowPositionUpdated(windowId = 1))
        layout}}
  }
  //Testing
  "Layout" should{
    "by M.RegisterWindow add window to list and by M.AllUiInitialized do layout of each window" in new TestCase {
      //Preparing
      actors.layout
      //Register window 1
      testDrive1.send(actors.layout, window1)
      //Register window 2
      testDrive2.send(actors.layout, window2)
      //Send  M.AllUiInitialized
      testDrive1.expectNoMsg(1.second)
      testDrive2.expectNoMsg(1.second)
      testPlumbing.send(actors.layout, M.AllUiInitialized)
      testPlumbing.send(actors.layout, M.AllUiCreated)
      //Expect SetWindowPosition
      testDrive1.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive1.expectNoMsg(1.second)
      testDrive2.expectNoMsg(1.second)
      testDrive1.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      testDrive2.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive2.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      testDrive1.expectNoMsg(1.second)
      testDrive2.expectNoMsg(1.second)}
    "by M.WindowUpdated update stored data" in new TestCase {
      //Preparing
      actors.layoutWithWindows
      val newState = newWindowState("new_t1")
      //Update
      testDrive1.send(actors.layout, M.WindowUpdated(DriveRef(testDrive1.ref), 1, newState))
      //Test
      val windows = actors.layout.askForState[ActorState].windows
      println("[LayoutTest] windows: ")
      windows.foreach(w ⇒ println("    " + w))
      val ((idDrive, idWin), (winStat, winPref)) = actors.layout.askForState[ActorState].windows.head
      idDrive.ref shouldEqual testDrive1.ref
      idWin       shouldEqual 1
      winStat     shouldEqual newState
      winPref     shouldEqual window1.prefs}
    "by M.LayoutWindow do layout for required window " in new TestCase {
      //Preparing
      actors.layoutWithWindows
      //Test
      testDrive1.send(actors.layout, M.LayoutWindow(DriveRef(testDrive1.ref), 1))
      testDrive1.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive1.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      testDrive1.expectNoMsg(1.second)
      testDrive2.expectNoMsg(1.second)}
    "by M.DoLayout do layout of each window and not do layout of hidden windows" in new TestCase {
      //Preparing
      actors.layoutWithWindows
      //Test for WindowsStairs
      testSketchController.send(actors.layout, M.DoLayout(WindowsLayoutKind.WindowsStairs))
      testDrive1.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive1.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      testDrive2.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive2.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      //Hide window 1
      testDrive1.send(actors.layout, M.WindowUpdated(DriveRef(testDrive1.ref), 1, newWindowState("t1").copy(isShown = false)))
      sleep(1.second) //Wait for message will processed
      //Test for after
      testSketchController.send(actors.layout, M.DoLayout(WindowsLayoutKind.WindowsStairs))
      testDrive2.expectMsgType[M.SetWindowPosition].windowId shouldEqual 1
      testDrive2.send(actors.layout, M.WindowPositionUpdated(windowId = 1))
      testDrive1.expectNoMsg()}
  }
}
