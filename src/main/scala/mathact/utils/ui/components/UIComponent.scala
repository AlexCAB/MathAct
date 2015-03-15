package mathact.utils.ui.components

import java.awt.{Canvas, Font}


/**
 * Trait provide alignment properties fro UI gridRow
 * Created by CAB on 10.03.2015.
 */

trait UIComponent {
  //Helpers
  private lazy val canvas = new Canvas()
  //Methods
  def calcStringWidth(string:String, font:Font):Int = canvas.getFontMetrics(font).stringWidth(string)
  def calcDoubleWidth(value:Double, font:Font):Int = calcStringWidth(value.toString,font)}
