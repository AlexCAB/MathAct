package mathact.clockwork.ui.components

import java.awt.Dimension

import mathact.clockwork.Clockwork
import mathact.clockwork.ui.Alignment

import scala.swing.Alignment._
import scala.swing.Label


/**
 * Diapason label like: [0,1]
 * Created by CAB on 10.03.2015.
 */

class DiapasonLabel (clockwork:Clockwork, min:Double, max:Double) extends Label with Alignment{
  //Construction
  val initWidth = List(min,max).map(x ⇒ clockwork.layout.calcDoubleWidth(x, clockwork.skin.nameFont)).sum + 20
  val initHeight = clockwork.skin.diapasonHeight
  font = clockwork.skin.diapasonFont
  horizontalAlignment = Center
  text = "[" + min + "," + max + "]"
  preferredSize = new Dimension(initWidth, initHeight)
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
