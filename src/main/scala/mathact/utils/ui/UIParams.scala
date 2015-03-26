package mathact.utils.ui
import java.awt.{Color, Font}
import javax.swing.ImageIcon


/**
 * Set of UI components parameters interfaces
 * Created by CAB on 12.03.2015.
 */

object UIParams {
  trait BorderFrame{
    val backgroundColor:Color}
  trait FlowFrame{
    val backgroundColor:Color}
  trait GridFrame{
    val backgroundColor:Color}
  trait HorizontalSlider{
    val sliderHeight:Int
    val sliderWidth:Int
    val sliderScale:Int
    val backgroundColor:Color}
  trait DiapasonLabel{
    val valueFont:Font
    val valueHeight:Int}
  trait NameLabel{
    val nameFont:Font
    val nameHeight:Int}
  trait SeparatorLabel{
    val separatorFont:Font
    val separatorHeight:Int}
  trait NumberLabel{
    val numberFormat:String
    val numberFont:Font
    val numberHeight:Int}
  trait NumberSpinner{
    val valueFont:Font
    val valueHeight:Int
    val backgroundColor:Color}
  trait Potentiometer extends NameLabel with DiapasonLabel with NumberSpinner with HorizontalSlider{
    val sliderHeight:Int
    val sliderWidth:Int
    val valueFont:Font
    val valueHeight:Int}
  trait Measurer extends NameLabel with SeparatorLabel with NumberLabel
  trait Executor{
    val startEnabledIcon:ImageIcon
    val startDisableIcon:ImageIcon
    val stopEnabledIcon:ImageIcon
    val stopDisableIcon:ImageIcon
    val stepEnabledIcon:ImageIcon
    val stepDisableIcon:ImageIcon
    val executorButtonsSize:Int}
  trait MinMaxAvgPane extends NameLabel with NumberLabel with SeparatorLabel{
    val textColor:Color
    val borderColor:Color
    val borderSize:Int}
  trait XYsPlot{
    val backgroundPaint:Color
    val rangeGridlinePaint:Color
    val domainGridlinePaint:Color}
  trait TextLinePane{
    val textBackgroundColor:Color
    val textFont:Font}
  trait YHistPlot{
    val backgroundPaint:Color
    val rangeGridlinePaint:Color
    val domainGridlinePaint:Color
    val intervalMarkerColor:Color
    val barsMargin:Double}
  trait Chart{
    val gridColor:Color
    val chartBackgroundColor:Color}
  trait VariablesBar extends NameLabel with NumberLabel with SeparatorLabel{
    val borderColor:Color
    val borderSize:Int
    val backgroundColor:Color}
  trait AutoManButtons{
    val authEnabledIcon:ImageIcon
    val manEnabledIcon:ImageIcon
    val authDisableIcon:ImageIcon
    val manDisableIcon:ImageIcon
    val authManButtonsSize:Int}
  trait DropDownList{
    val listFont:Font
    val listHeight:Int}
  trait SelectionBar extends NameLabel with DropDownList{
    val textColor:Color
    val backgroundColor:Color}}
