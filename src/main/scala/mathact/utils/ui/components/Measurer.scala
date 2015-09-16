package mathact.utils.ui.components
import swing.Color
import mathact.utils.ui.UIParams
import scala.swing.Alignment.Left


/**
 * Value Measurer
 * Created by CAB on 16.03.2015.
 */

class Measurer(
 uiParams:UIParams.Measurer,
 varName:String,
 color:Color,
 initValue:String)
extends GridComponent {
  //Components
  val name = new NameLabel(uiParams, varName, color)
  val separator = new SeparatorLabel(uiParams, " = ", color)
  val width = name.calcStringWidth(initValue, uiParams.nameFont) + 8
  val value = new NameLabel(uiParams, initValue, color, Left, Some(width))
  val gridRow = List(name, separator, value)
  //Methods
  def update(v:String) = {
    value.setName(v)}}
