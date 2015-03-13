package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Diapason label like: [0,1]
 * Created by CAB on 10.03.2015.
 */

class DiapasonLabel (uiParams:UIParams.DiapasonLabel, min:Double, max:Double) extends Label with ToyComponent{
  //Construction
//  val initWidth = List(min,max).map(x ⇒ calcDoubleWidth(x, uiParams.valueFont)).sum + 20
//  val initHeight = uiParams.valueHeight
  preferredSize = new Dimension(
    List(min,max).map(x ⇒ calcDoubleWidth(x, uiParams.valueFont)).sum + 20,
    uiParams.valueHeight)
  font = uiParams.valueFont
  horizontalAlignment = Center
  text = "[" + min + "," + max + "]"
//  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
//  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}
}
