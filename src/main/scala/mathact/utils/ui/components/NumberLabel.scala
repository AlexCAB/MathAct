package mathact.utils.ui.components
import java.awt.Dimension
import java.text.{NumberFormat, DecimalFormat}
import java.util.Locale
import mathact.utils.ui.UIParams
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Label for numbers
 * Created by CAB on 13.03.2015.
 */

class NumberLabel (uiParams:UIParams.NumberLabel) extends Label with ToyComponent{
  //Helpers
  private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
  decimal.applyPattern(uiParams.numberFormat)
  //Construction
  val initWidth = calcStringWidth(uiParams.numberFormat, uiParams.numberFont)
  val initHeight = uiParams.numberHeight
  font = uiParams.numberFont
  horizontalAlignment = Left
  text = "---"
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNumber(v:Double):Unit = {
    text = if(v.isNaN){"---"}else{decimal.format(v)}}
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}