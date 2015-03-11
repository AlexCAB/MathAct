package mathact.utils.ui
import java.awt.Font
import javax.swing.ImageIcon


/**
 * UI Parameters
 * Created by CAB on 10.03.2015.
 */

class Skin {
  //Fonts
  val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
  val nameHeight = 16
  val diapasonFont = new Font(Font.SERIF, Font.BOLD, 14)
  val diapasonHeight = 16
  val valueFont = new Font(Font.SERIF, Font.BOLD, 14)
  val valueHeight = 16
  //Names
  def titleFor(name:String, component:Any, default:String):String = {
    name match{
      case n if n != "" ⇒ n
      case _ ⇒ component.getClass.getCanonicalName match{
        case null ⇒ "PotBoard"
        case n ⇒ n.split("[.]").last.replace("$","")}}}
  //Sliders
  val horizontalSliderHeight = 16
  val potSliderWidth = 150
  val doerSliderWidth = 180
  //Executor
  val startEnabledIcon = new ImageIcon(getClass.getResource("/start_e.png"))
  val startDisableIcon = new ImageIcon(getClass.getResource("/start_d.png"))
  val stopEnabledIcon = new ImageIcon(getClass.getResource("/stop_e.png"))
  val stopDisableIcon = new ImageIcon(getClass.getResource("/stop_d.png"))
  val stepEnabledIcon = new ImageIcon(getClass.getResource("/step_e.png"))
  val stepDisableIcon = new ImageIcon(getClass.getResource("/step_d.png"))
  val executorButtonsSize = 33

}
