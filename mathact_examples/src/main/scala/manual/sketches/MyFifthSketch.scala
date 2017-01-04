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

package manual.sketches

import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.{BlockUI, FunUIWiring}
import mathact.core.bricks.ui.interaction.{UICommand, UIEvent}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox
import scalafxml.core.macros.sfxml


/** Using of block UI
  * Created by CAB on 03.01.2017.
  */

object MyFifthSketch{
  //Common definition
  case class UpdateVal(v: Int) extends UICommand
  case class SentMsg(m: String) extends UIEvent
  //FXML UI controller (require to be a static class)
  trait MyFxmlUILike{ val label: Label; val button: Button}
  @sfxml class MyFxmlUI(val label: Label, val button: Button) extends MyFxmlUILike
}


class MyFifthSketch extends SimpleWorkbench with HelperBlocks{ import MyFifthSketch._
  //OOP style
  object ObjIntPrinter extends EmptyBlock with ObjWiring with BlockUI with LinkThrough[Int, String]{
    //UI definition
    class MyUI extends SfxFrame{
      //Params
      title = "MyFifthSketch - ObjWiring"
      showOnStart = true
      //Components
      val label = new Label{
        prefWidth = 200}
      val button = new Button{
        text = "Send Hi!"
        onAction = handle{ sendEvent(SentMsg("Obj Hi!")) }}
      //Scene
      scene = new Scene{
        root = new HBox {
          prefWidth = 280
          children = Seq(label, button)}}
      //Commands reactions
      def onCommand = { case UpdateVal(v) ⇒ label.text = "Last value: " + v }
    }
    //UI registration
    UI(new MyUI)
    //Wiring
    private val handler = new Inflow[Int] with Outflow[String] {
      //Binding reaction on UI event
      UI.onEvent{ case SentMsg(m) ⇒ pour(m)}
      //Income message handler method
      protected def drain(v: Int): Unit = {
        //Update UI
        UI.sendCommand(UpdateVal(v))
        //Convert and send message to next block
        pour("Converted" + v.toString)
      }
    }
    //Connection points
    val in = Inlet(handler)
    val out = Outlet(handler)
  }
  //Functional style
  object FunIntPrinter extends EmptyBlock with FunWiring with BlockUI with FunUIWiring with LinkThrough[Int, String]{
    //Connection points
    val in = In[Int]
    val out = Out[String]
    //UI definition
    UI( new FxmlFrame[MyFxmlUILike]("manual/sketches/my_fxml_ui.fxml"){
      //Params
      title = "MyFifthSketch - FunWiring"
      showOnStart = true
      //Set actions
      controller.button.onAction = handle{ sendEvent(SentMsg("Fun Hi!")) }
      //Commands reactions
      def onCommand = { case UpdateVal(v) ⇒ controller.label.text = "Last value: " + v }
    })
    //Wiring
    in.map(v ⇒ UpdateVal(v)) >> UI
    in.map(v ⇒ "Converted" + v.toString) >> out
    UI.map{ case SentMsg(m) ⇒ m } >> out
  }
  //Connecting
  Generator ~> ObjIntPrinter ~> Logger
  Generator ~> FunIntPrinter ~> Logger
}