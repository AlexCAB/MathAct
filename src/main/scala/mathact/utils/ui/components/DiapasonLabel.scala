package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.Environment
import mathact.utils.ui.Alignment
import scala.swing.Alignment._
import scala.swing.Label


/**
 * Diapason label like: [0,1]
 * Created by CAB on 10.03.2015.
 */

class DiapasonLabel (environment:Environment, min:Double, max:Double) extends Label with Alignment{
  //Construction
  val initWidth = List(min,max).map(x ⇒ environment.layout.calcDoubleWidth(x, environment.skin.nameFont)).sum + 20
  val initHeight = environment.skin.diapasonHeight
  font = environment.skin.diapasonFont
  horizontalAlignment = Center
  text = "[" + min + "," + max + "]"
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
