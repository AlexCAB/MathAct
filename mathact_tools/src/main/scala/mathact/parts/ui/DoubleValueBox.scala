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

package mathact.parts.ui

import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.util.Locale

import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color


/** Displaying double value with name
  * Created by CAB on 24.11.2016.
  */

class DoubleValueBox(name: String, color: Color) extends HBox(2){
  //Parameters
  alignment = Pos.Center
  //Helpers
  val decimalFormat = new DecimalFormat("0.0###",  new DecimalFormatSymbols(Locale.US))
  //UI
  val label =  new Label{
    text = "---; "
    textFill = color
    style = "-fx-font-size: 11pt;"}
  children = Seq(
    new Label{
      text = name + " = "
      textFill = color
      style = "-fx-font-weight: bold; -fx-font-size: 11pt;"},
    label)
  //Methods
  def update(value: Double): Unit = { label.text = decimalFormat.format(value) + "; " }}