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

package mathact.tools.pots

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkOut
import mathact.core.bricks.plumbing.fitting.Plug
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UIEvent
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.Includes._


/** Boolean switch
  * Created by CAB on 24.12.2016.
  */

class BoolSwitch(implicit context: BlockContext)
extends Tool(context, "BS", "mathact/tools/pots/bool_switch.png")
with ObjWiring with BlockUI with LinkOut[Boolean] {
  //Definitions
  private case class Value(v: Boolean) extends UIEvent
  //UI definition
  private class MainUI extends SfxFrame{
    //Params
    title = "Bool switch" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Scene
    scene = new Scene{
      root = new HBox(4){
        val buttonStyle = "-fx-font-size: 14pt; -fx-font-weight: bold;"
        padding = Insets(4.0)
        alignment = Pos.Center
        children = Seq(
          new Button("T"){
            prefWidth = 120
            style = buttonStyle
            onAction = handle(sendEvent(Value(true)))},
          new Button("F"){
            prefWidth = 120
            style = buttonStyle
            onAction = handle(sendEvent(Value(false)))})}}
    //Commands reactions
    def onCommand = { case _ ⇒ }}
  //UI registration
  UI(new MainUI)
  //Output
  val out: Plug[Boolean] = Outlet(new Outflow[Boolean]{ UI.onEvent{ case v: Value ⇒ pour(v.v) }})}
