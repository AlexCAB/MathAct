package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Label with right text placing
 * Created by CAB on 10.03.2015.
 */

class NameLabel(uiParams:UIParams.NameLabel, labText:String) extends Label with ToyComponent{
  //Construction
  val initWidth = calcStringWidth(labText, uiParams.nameFont)
  val initHeight = uiParams.nameHeight
  font = uiParams.nameFont
  horizontalAlignment = Right
  text = labText
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
