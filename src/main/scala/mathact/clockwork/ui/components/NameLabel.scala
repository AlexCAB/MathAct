package mathact.clockwork.ui.components
import java.awt.Dimension
import mathact.clockwork.Clockwork
import mathact.clockwork.ui.Alignment
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Label with right text placing
 * Created by CAB on 10.03.2015.
 */

class NameLabel(clockwork:Clockwork, labText:String) extends Label with Alignment{
  //Construction
  val initWidth = clockwork.layout.calcStringWidth(labText, clockwork.skin.nameFont)
  val initHeight = clockwork.skin.nameHeight
  font = clockwork.skin.nameFont
  horizontalAlignment = Right
  text = labText
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
