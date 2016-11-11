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
import mathact.core.bricks.ui.interaction.{UIEvent, UICommand}
import mathact.core.bricks.ui.{FunUIWiring, BlockUI}
import mathact.tools.EmptyBlock
import mathact.tools.workbenches.SimpleWorkbench

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.{TextArea, Button}
import scalafx.scene.layout.HBox

import scalafxml.core.macros.sfxml


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
  case class DoerSate(isShown: Boolean) extends UIEvent
  //FXML UI controller
  trait LoggerUILike{
    val textArea: TextArea
    val showDoer: Button
    val hideDoer: Button}
  @sfxml class LoggerUI(val textArea: TextArea, val showDoer: Button, val hideDoer: Button) extends LoggerUILike}


class BlocksWithUiExample extends SimpleWorkbench { import BlocksWithUiExample._
  //Blocks
  val doer = new EmptyBlock with BlockUI with ObjWiring{ name = "Doer"
    //UI definition
    class DoerUI extends SfxFrame{
      //Params
      title = "BlocksWithUiExample - Doer"
      showOnStart = true
      //Scene
      scene = new Scene{
        root = new HBox {
          prefWidth = 280
          children = new Button{
            text = "Say Hi!"
            onAction = handle{ sendEvent(LogLine("Hi!"))}}}}
      //Commands reactions
      def onCommand = {
        case ShowDoer ⇒ show()
        case HideDoer ⇒ hide()}}
    //UI registration
    UI(new DoerUI)
    //Handlers
    private val handler = new Outflow[UICommand] with Inflow[UIEvent]{
      //Reactions on UI event
      UI.onEvent{
        case  LogLine(text) ⇒ pour(AddLine(text))}
      //Managing of UI by send commands
      def drain(v: UIEvent): Unit = v match{
        case DoerSate(isShown) ⇒ UI.sendCommand(if(isShown) ShowDoer else HideDoer)
        case _ ⇒}}
    //Pipes
    val in = Inlet(handler)
    val out = Outlet(handler)}
  val logger = new EmptyBlock with BlockUI with FunWiring with FunUIWiring { name = "Logger"
    //Pipes
    val in = In[UICommand]
    val out = Out[UIEvent]
    //UI definition
    UI( new FxmlFrame[LoggerUILike]("examples/ui/logger_ui.fxml"){
      //Params
      title = "BlocksWithUiExample - Logger"
      showOnStart = true
      //Set actions
      controller.showDoer.onAction = handle{ sendEvent(DoerSate(isShown = true))}
      controller.hideDoer.onAction = handle{ sendEvent(DoerSate(isShown = false))}
      //Commands reactions
      def onCommand = {
        case AddLine(text) ⇒ controller.textArea.appendText(text + "\n")
        case _ ⇒}})
    //Wiring
    in >> UI >> out}
  //Connecting
  doer.out ~> logger.in
  doer.in  <~ logger.out}
