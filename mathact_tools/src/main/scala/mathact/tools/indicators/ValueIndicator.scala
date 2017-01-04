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

package mathact.tools.indicators

import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.util.Locale

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UICommand
import mathact.data.basic.SingleValue
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.paint.Color.White


/** Value indicator
  * Created by CAB on 28.12.2016.
  */

class ValueIndicator(implicit context: BlockContext)
extends Tool(context, "VI", "mathact/tools/indicators/value_indicator.png")
with ObjWiring with BlockUI with LinkIn[SingleValue]{
  //Helpers
  val decimalFormat = new DecimalFormat("0.0#######",  new DecimalFormatSymbols(Locale.US))
  //Definitions
  private case class Update(i: Int, value: String) extends UICommand
  private class Indicator(val i: Int, val name: String = "", ui: UI.type) extends Inflow[SingleValue] {
    protected def drain(v: SingleValue): Unit = ui.sendCommand(Update(i, decimalFormat.format(v.value)))}
  //UI
  private class MainUI(indicators: Seq[Indicator]) extends SfxFrame{
    //Params
    title = "Indicator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Components
    val lines = indicators.map( indicator ⇒ (
      indicator.i,
      indicator.name,
      new Label{
        prefWidth = 100
        prefHeight = 20
        alignment = Pos.CenterLeft
        text = "---"
        style = "-fx-font-weight: bold; -fx-font-size: 12pt;"}))
    val labels = lines.map(_._3).toVector
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        prefWidth = 300
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
    def onCommand = { case Update(i, value) ⇒ labels(i).text = value }}
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
  def in: Socket[SingleValue] = Inlet(buildIndicator(name = ""))
  def in(name: String = ""): Socket[SingleValue] = Inlet(buildIndicator(name))}