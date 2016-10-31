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

package examples.ui

import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.{UICommand, BlockUI, UIEvent}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.{TextField, Button}
import scalafx.scene.layout.HBox

/** Example of using of BlockUI trait
  * Created by CAB on 31.10.2016.
  */

object BlocksWithUiExample{
  //Commands
  case object ShowDoer extends UICommand
  case object HideDoer extends UICommand
  case class AddLine(text: String) extends UICommand
  //Events
  case class LogLine(text: String) extends UIEvent
  case class DoerSate(isSown: Boolean) extends UIEvent}


class BlocksWithUiExample extends SimpleWorkbench { import BlocksWithUiExample._
  //Blocks
  val doer = new EmptyBlock with BlockUI with ObjWiring{ name = "Doer"
    //UI definition
    class DoerUI extends Frame{
      //Scene
      title = "BlocksWithUiExample - Doer"
      showOnStart = true
      scene = new Scene{
        root = new HBox {
          children = new Button{
            text = "Say Hi!"
            onAction = handle{ sendEvent(LogLine("Hi!"))}}}}
      //Commands reactions
      def onCommand = {
        case ShowDoer ⇒ show()
        case HideDoer ⇒ hide()}}
    //UI registration
    UI[DoerUI]
    //Handlers
    private val handler = new Outflow[String] with Inflow[Boolean]{
      //Reactions on UI event
      UI.onEvent{
        case  LogLine(text) ⇒ pour(text)}
      //Managing of UI by send commands
      def drain(v: Boolean): Unit =
        UI.sendCommand(if(v) ShowDoer else HideDoer)}
    //Pipes
    val in = Inlet(handler)
    val out = Outlet(handler)}
  val logger = new EmptyBlock with BlockUI with FunWiring { name = "Logger"
    //Pipes
    val out = Out[Boolean]
    val in = In[String]
    //UI definition
    UI( new Frame{
      //Scene
      title = "BlocksWithUiExample - Logger"
      showOnStart = true
      val textField = new TextField
      scene = new Scene{
        root = new HBox {
          children = Seq(
            new Button{
              text = "Show doer"
              onAction = handle{ sendEvent(DoerSate(isSown = true))}},
            new Button{
              text = "Hide doer"
              onAction = handle{ sendEvent(DoerSate(isSown = false))}},
            textField)}}
      //Commands reactions
      def onCommand = {
        case LogLine(text) ⇒  textField.appendText(" " + text)}})
    //Wiring
    in.map(t ⇒ LogLine(t)) >> UI.map{ case DoerSate(isSown) ⇒ isSown } >> in}
  //Connecting
  doer.out ~> logger.in
  logger.out ~> doer.in}
