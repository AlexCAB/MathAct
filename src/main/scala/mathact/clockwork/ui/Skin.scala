package mathact.clockwork.ui
import java.awt.Font

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
  def titleFor(component:Any, default:String):String = {
    component.getClass.getCanonicalName match{
      case null ⇒ "PotBoard"
      case n ⇒ n.split("[.]").last.replace("$","")}}
  //Sliders
  val horizontalSliderHeight = 16
  val potSliderWidth = 150}
