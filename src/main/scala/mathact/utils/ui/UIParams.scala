package mathact.utils.ui
import java.awt.Font
import javax.swing.ImageIcon

/**
 * Set of UI components parameters interfaces
 * Created by CAB on 12.03.2015.
 */

object UIParams {
  trait Slider{
    val sliderHeight:Int
    val sliderWidth:Int}
  trait DiapasonLabel{
    val valueFont:Font
    val valueHeight:Int}
  trait NameLabel{
    val nameFont:Font
    val nameHeight:Int}
  trait NumberSpinner{
    val valueFont:Font
    val valueHeight:Int}
  trait Potentiometer extends NameLabel with DiapasonLabel with NumberSpinner with Slider{
    val sliderHeight:Int
    val sliderWidth:Int
    val valueFont:Font
    val valueHeight:Int}
  trait Executor{
    val startEnabledIcon:ImageIcon
    val startDisableIcon:ImageIcon
    val stopEnabledIcon:ImageIcon
    val stopDisableIcon:ImageIcon
    val stepEnabledIcon:ImageIcon
    val stepDisableIcon:ImageIcon
    val executorButtonsSize:Int}}
