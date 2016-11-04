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
      val initialLayoutType = WindowsLayoutKind.FillScreen}
    //Helpers actors
    lazy val testSketchController = TestProbe("TestSketchController_" + randomString())
    lazy val testPlumbing = TestProbe("TestPlumbing_" + randomString())
    lazy val testDrive1 = TestProbe("TestDrive1_" + randomString())
    lazy val testDrive2 = TestProbe("TestDrive2_" + randomString())
    //Data generation
    def newWindowState = WindowState(
      isShown = randomBoolean(),
      x = randomDouble(),
      y = randomDouble(),
      h = randomDouble(),
      w = randomDouble(),
      title = randomString())
     def newWindowPreference = WindowPreference(
       prefX = randomOpt(randomDouble()),
       prefY = randomOpt(randomDouble()))
    //Layout
    lazy val layout = system.actorOf(
      Props(new LayoutActor(testConfig, SketchControllerRef(testSketchController.ref)){
        override  def receive: PartialFunction[Any, Unit] = {
          case GetDriveState ⇒ sender ! ActorState(windows.toMap.map{ case (k,d) ⇒ (k, (d.state, d.preference))})
          case m ⇒ super.receive.apply(m)}}),
      "TestLayout_" + randomString())
    lazy val window1 =  M.RegisterWindow(DriveRef(testDrive1.ref), 1, newWindowState, newWindowPreference)
    lazy val window2 =  M.RegisterWindow(DriveRef(testDrive2.ref), 1, newWindowState, newWindowPreference)
    lazy val layoutWithWindows = {
      layout
      testDrive1.send(layout, window1)
      testDrive2.send(layout, window2)
      testPlumbing.send(layout,  M.AllDrivesConstruct)
      testDrive1.expectMsgType[M.SetWindowPosition]
      testDrive2.expectMsgType[M.SetWindowPosition]
      layout}}
  //Testing
  "Layout" should{
    "by M.RegisterWindow add window to list and by M.AllDrivesConstruct do layout of each window" in new TestCase {
      //Preparing
      layout
      //Register window 1
      testDrive1.send(layout, M.RegisterWindow(DriveRef(testDrive1.ref), 1, newWindowState, newWindowPreference))
      //Register window 2
      testDrive2.send(layout, M.RegisterWindow(DriveRef(testDrive2.ref), 1, newWindowState, newWindowPreference))
      //Send  M.AllDrivesConstruct
      testDrive1.expectNoMsg()
      testDrive2.expectNoMsg()
      testPlumbing.send(layout,  M.AllDrivesConstruct)
      //Expect SetWindowPosition
      testDrive1.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      testDrive2.expectMsgType[M.SetWindowPosition].id shouldEqual 1}
    "by M.WindowUpdated update stored data" in new TestCase {
      //Preparing
      layoutWithWindows
      val newState = newWindowState
      //Update
      testDrive1.send(layout, M.WindowUpdated(DriveRef(testDrive1.ref), 1, newWindowState))
      //Test
      val ((idDrive, idWin), (winStat, winPref)) = layout.askForState[ActorState].windows.head
      idDrive.ref shouldEqual testDrive1.ref
      idWin       shouldEqual 1
      winStat     shouldEqual newState
      winPref     shouldEqual window1.prefs}
    "by M.LayoutWindow do layout for required window " in new TestCase {
      //Preparing
      layoutWithWindows
      //Test
      testDrive1.send(layout, M.LayoutWindow(DriveRef(testDrive1.ref), 1))
      testDrive1.expectMsgType[M.SetWindowPosition].id shouldEqual 1}
    "by M.DoLayout do layout of each window and not do layout of hidden windows" in new TestCase {
      //Preparing
      layoutWithWindows
      //Test for FillScreen
      testSketchController.send(layout, M.DoLayout(WindowsLayoutKind.FillScreen))
      testDrive1.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      testDrive2.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      //Test for WindowsStairs
      testSketchController.send(layout, M.DoLayout(WindowsLayoutKind.WindowsStairs))
      testDrive1.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      testDrive2.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      //Hide window 1
      testDrive1.send(layout, M.WindowUpdated(DriveRef(testDrive1.ref), 1, newWindowState.copy(isShown = false)))
      sleep(1.second) //Wait for message will processed
      //Test for after
      testSketchController.send(layout, M.DoLayout(WindowsLayoutKind.FillScreen))
      testDrive2.expectMsgType[M.SetWindowPosition].id shouldEqual 1
      testDrive1.expectNoMsg()}
  }
}
