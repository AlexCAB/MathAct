package mathact.utils.ui
import java.awt.{Font, Canvas}


/**
 * Trait provide alignment properties fro UI gridRow
 * Created by CAB on 10.03.2015.
 */

trait ToyComponent {
  //Abstract
  val initWidth:Int
  val initHeight:Int
  def setNewSize(w:Int,h:Int):Unit
  //Helpers Methods
  private lazy val canvas = new Canvas()
  def calcStringWidth(string:String, font:Font):Int = canvas.getFontMetrics(font).stringWidth(string)
  def calcDoubleWidth(value:Double, font:Font):Int = calcStringWidth(value.toString,font)}
