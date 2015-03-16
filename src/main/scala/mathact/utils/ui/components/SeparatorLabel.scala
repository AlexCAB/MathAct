package mathact.utils.ui.components
import java.awt.{Dimension, Color}
import mathact.utils.ui.UIParams
import scala.swing.Alignment.Center
import scala.swing.Label


/**
 * Center layout label
 * Created by CAB on 16.03.2015.
 */

class SeparatorLabel(uiParams:UIParams.SeparatorLabel, labText:String, textColor:Color = Color.black) extends Label with UIComponent{
  //Construction
  preferredSize = new Dimension(
    calcStringWidth(labText, uiParams.separatorFont),
    uiParams.separatorHeight)
  font = uiParams.separatorFont
  foreground = textColor
  horizontalAlignment = Center
  text = labText}
