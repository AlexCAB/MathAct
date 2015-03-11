package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.Environment
import mathact.utils.ui.Alignment
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Label with right text placing
 * Created by CAB on 10.03.2015.
 */

class NameLabel(environment:Environment, labText:String) extends Label with Alignment{
  //Construction
  val initWidth = environment.layout.calcStringWidth(labText, environment.skin.nameFont)
  val initHeight = environment.skin.nameHeight
  font = environment.skin.nameFont
  horizontalAlignment = Right
  text = labText
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
