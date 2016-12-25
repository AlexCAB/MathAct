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

package mathact.tools.indicators

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UICommand
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.paint.Color.White


/** Boolean indicator
  * Created by CAB on 24.12.2016.
  */

class BoolIndicator(implicit context: BlockContext)
extends Tool(context, "BI", "mathact/tools/indicators/boolean_indicator.png")
with ObjWiring with BlockUI with LinkIn[Boolean]{
  //Definitions
  private case class Update(i: Int, value: Boolean) extends UICommand
  private class Indicator(val i: Int, val name: String = "", ui: UI.type) extends Inflow[Boolean] {
    protected def drain(v: Boolean): Unit = ui.sendCommand(Update(i, v))}
  //UI
  private class MainUI(indicators: Seq[Indicator]) extends SfxFrame{
    //Params
    title = "Bool indicator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Components
    val lines = indicators.map(indicator ⇒ (
      indicator.i,
      indicator.name,
      new Label{
        prefWidth = 60
        prefHeight = 20
        alignment = Pos.CenterLeft
        text = "---"
        style = "-fx-font-weight: bold; -fx-font-size: 12pt;"}))
    val labels = lines.map(_._3).toVector
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        prefWidth = 200
        center = new VBox(2){
          padding = Insets(1.0)
          alignment = Pos.CenterRight
          children = lines.map{ case (i, name, _) ⇒
            new Label{
              prefHeight = 20
              alignment = Pos.CenterRight
              text = (name match{ case "" ⇒ s"value $i"; case n ⇒ n}) + " = "
              style = "-fx-font-size: 12pt;"}}}
        right = new VBox(2){
          padding = Insets(1.0)
          children = lines.map{ case (_, _, label) ⇒ label }}}}
    //Commands reactions
    def onCommand = { case Update(i, value) ⇒ labels(i).text = if(value) "true" else "false" }}
  //Variables
  private var indicators = List[Indicator]()
  //UI registration
  UI(new MainUI(indicators))
  //Functions
  private def buildIndicator(name: String): Indicator = {
    val line = new Indicator(indicators.size, name, UI)
    indicators :+= line
    line}
  //Inlets
  def in: Socket[Boolean] = Inlet(buildIndicator(name = ""))
  def in(name: String = ""): Socket[Boolean] = Inlet(buildIndicator(name))}