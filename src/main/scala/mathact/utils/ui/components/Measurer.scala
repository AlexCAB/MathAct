package mathact.utils.ui.components
import swing.Color
import mathact.utils.ui.UIParams


/**
 * Value Measurer
 * Created by CAB on 16.03.2015.
 */

class Measurer(
 uiParams:UIParams.Measurer,
 varName:String,
 color:Color)
extends GridComponent {
  //Components
  val name = new NameLabel(uiParams, varName, color)
  val separator = new SeparatorLabel(uiParams, " = ", color)
  val value = new NumberLabel(uiParams, color)
  val gridRow = List(name, separator, value)
  //Methods
  def update(v:Double) = {
    value.setNumber(v)}}
