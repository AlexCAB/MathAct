package mathact.utils.ui
import java.awt.image.BufferedImage
import java.awt.{Color, Font}
import javax.swing.ImageIcon


/**
 * Set of UI components parameters interfaces
 * Created by CAB on 12.03.2015.
 */

object UIParams {
  trait BorderFrame{
    val backgroundColor:Color}
  trait TVFrame{
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
  trait DiscreteHorizontalSlider{
    val discreteSliderFont:Font
    val discreteSliderTextColor:Color
    val discreteSliderHeight:Int
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
  trait Switch extends NameLabel with DiscreteHorizontalSlider{
    val discreteSliderFont:Font
    val discreteSliderTextColor:Color
    val discreteSliderHeight:Int
    val backgroundColor:Color}
  trait Measurer extends NameLabel with SeparatorLabel with NumberLabel
  trait Executor{
    val startEnabledIcon:ImageIcon
    val startDisableIcon:ImageIcon
    val stopEnabledIcon:ImageIcon
    val stopDisableIcon:ImageIcon
    val stepEnabledIcon:ImageIcon
    val stepDisableIcon:ImageIcon
    val executorButtonsSize:Int}
  trait ResetButton{
    val resetButtonIcon:ImageIcon
    val resetButtonSize:Int}
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
    val backgroundColor:Color}
  trait SimpleGraph{
    val defaultNodeColor:Color
    val defaultNodeSize:Int
    val defaultEdgeColor:Color
    val defaultEdgeSize:Int
    val numberFormat:String
    val backgroundColor:Color}
  trait Pacman{
    val squareSize:Int
    val maze0:BufferedImage
    val maze1U:BufferedImage
    val maze1D:BufferedImage
    val maze1L:BufferedImage
    val maze1R:BufferedImage
    val maze2U:BufferedImage
    val maze2D:BufferedImage
    val maze2L:BufferedImage
    val maze2R:BufferedImage
    val maze3U:BufferedImage
    val maze3D:BufferedImage
    val maze3L:BufferedImage
    val maze3R:BufferedImage
    val maze4:BufferedImage
    val mazeV:BufferedImage
    val mazeH:BufferedImage
    val mazeE:BufferedImage
    val pacman:BufferedImage
    val pacmanSU:BufferedImage
    val pacmanSD:BufferedImage
    val pacmanSL:BufferedImage
    val pacmanSR:BufferedImage
    val pacmanBU:BufferedImage
    val pacmanBD:BufferedImage
    val pacmanBL:BufferedImage
    val pacmanBR:BufferedImage
    val ghostB:BufferedImage
    val ghostBU:BufferedImage
    val ghostBD:BufferedImage
    val ghostBL:BufferedImage
    val ghostBR:BufferedImage
    val ghostR:BufferedImage
    val ghostRU:BufferedImage
    val ghostRD:BufferedImage
    val ghostRL:BufferedImage
    val ghostRR:BufferedImage
    val ghostW:BufferedImage
    val ghostWU:BufferedImage
    val ghostWD:BufferedImage
    val ghostWL:BufferedImage
    val ghostWR:BufferedImage
    val pellet:BufferedImage
    val powerPellet:BufferedImage}}
