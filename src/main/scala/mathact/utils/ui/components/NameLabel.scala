package mathact.utils.ui.components
import java.awt.{Dimension,Color}
import mathact.utils.ui.UIParams
import scala.swing.Alignment._
import scala.swing.{Alignment, Label}


/**
 * Label with right text placing
 * Created by CAB on 10.03.2015.
 */

class NameLabel(
  uiParams:UIParams.NameLabel,
  labText:String,
  textColor:Color = Color.black,
  alignment:Alignment.Value = Right)
extends Label with UIComponent{
  //Construction
  preferredSize = new Dimension(
    calcStringWidth(labText, uiParams.nameFont),
    uiParams.nameHeight)
  font = uiParams.nameFont
  foreground = textColor
  horizontalAlignment = alignment
  text = labText}
