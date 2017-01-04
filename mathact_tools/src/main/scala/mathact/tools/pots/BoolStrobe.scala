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

package mathact.tools.pots

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkOut
import mathact.core.bricks.plumbing.fitting.Plug
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UIEvent
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.Includes._
import scalafx.scene.image.{Image, ImageView}


/** Boolean strobe
  * Created by CAB on 26.12.2016.
  */

class BoolStrobe(implicit context: BlockContext)
extends Tool(context, "BS", "mathact/tools/pots/bool_strobe.png")
with ObjWiring with ObjOnStart with BlockUI with LinkOut[Boolean] {
  //Resources
  val upImg = new Image("mathact/tools/pots/up_strobe.png")
  val downImg = new Image("mathact/tools/pots/down_strobe.png")
  //Definitions
  private case object Strobe extends UIEvent
  //Variables
  private var _default = false
  //UI definition
  private class MainUI extends SfxFrame{
    //Params
    title = "Strobe" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Scene
    scene = new Scene{
      root = new HBox(4){
        val buttonStyle = "-fx-font-size: 14pt; -fx-font-weight: bold;"
        padding = Insets(4.0)
        alignment = Pos.Center
        children = new Button{
          prefWidth = 220
          graphic = new ImageView{ image = if(_default) downImg else upImg }
          style = buttonStyle
          onAction = handle(sendEvent(Strobe))}}}
    //Commands reactions
    def onCommand = { case _ ⇒ }}
  //Outflow
  private val outflow = new Outflow[Boolean] { def send(v: Boolean): Unit = pour(v) }
  //UI registration
  UI(new MainUI)
  UI.onEvent{ case Strobe ⇒
    outflow.send(! _default)
    outflow.send(_default) }
  //DSL
  def default: Boolean = _default
  def default_=(v: Boolean){ _default = v }
  //On start
  protected def onStart(): Unit = outflow.send(_default)
  //Output
  val out: Plug[Boolean] = Outlet(outflow)}