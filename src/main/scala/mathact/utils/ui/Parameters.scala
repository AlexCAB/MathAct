package mathact.utils.ui
import java.awt.{Color, Font}
import javax.swing.ImageIcon
import UIParams._


/**
 * Parameters
 * Created by CAB on 10.03.2015.
 */

class Parameters {
  //Names
  def titleFor(name:String, component:Any, default:String):String = {
    name match{
      case n if n != "" ⇒ n
      case _ ⇒ component.getClass.getCanonicalName match{
        case null ⇒ default
        case n ⇒ n.split("[.]").last.replace("$","")}}}
  //PotBoard
  object PotBoard extends Potentiometer{
    val updatePriority = 1
    val sliderHeight = 16
    val sliderWidth = 150
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val diapasonFont = new Font(Font.SERIF, Font.BOLD, 14)
    val diapasonHeight = 16
    val valueFont = new Font(Font.SERIF, Font.BOLD, 14)
    val valueHeight = 16}
  //Doer
  object Doer extends Executor with Slider{
    val updatePriority = 0
    val startEnabledIcon = new ImageIcon(getClass.getResource("/start_e.png"))
    val startDisableIcon = new ImageIcon(getClass.getResource("/start_d.png"))
    val stopEnabledIcon = new ImageIcon(getClass.getResource("/stop_e.png"))
    val stopDisableIcon = new ImageIcon(getClass.getResource("/stop_d.png"))
    val stepEnabledIcon = new ImageIcon(getClass.getResource("/step_e.png"))
    val stepDisableIcon = new ImageIcon(getClass.getResource("/step_d.png"))
    val executorButtonsSize = 33
    val sliderHeight = 16
    val sliderWidth = 180}
  //XTracer
  object XTracer extends MinMaxAvgPane with XYsPlot {
    val updatePriority = 2
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val numberFormat = "#0.000000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, Font.BOLD, 14)
    val separatorHeight = 16
    val backgroundPaint = Color.WHITE
    val rangeGridlinePaint = Color.BLACK
    val domainGridlinePaint = Color.BLACK


  }
}
