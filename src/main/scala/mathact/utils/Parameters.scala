package mathact.utils
import java.awt.{Color, Font}
import javax.swing.ImageIcon
import mathact.utils.ui.UIParams._


/**
 * Parameters
 * Created by CAB on 10.03.2015.
 */

class Parameters {
  //PotBoard
  object PotBoard extends Potentiometer{
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
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val numberFormat = "#0.000000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, Font.BOLD, 14)
    val separatorHeight = 16
    val backgroundPaint = Color.WHITE
    val rangeGridlinePaint = Color.BLACK
    val domainGridlinePaint = Color.BLACK}
  //Logger
  object Logger extends TextLinePane{
    val textBackgroundColor = Color.BLACK
    val defaultColor = Color.GREEN
    val textFont:Font = new Font(Font.SERIF, Font.BOLD, 14)}
  //XYPlot
  object XYPlot extends MinMaxAvgPane with XYsPlot {
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val numberFormat = "#0.000000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, Font.BOLD, 14)
    val separatorHeight = 16
    val backgroundPaint = Color.WHITE
    val rangeGridlinePaint = Color.BLACK
    val domainGridlinePaint = Color.BLACK}
  //Histogram
  object Histogram extends MinMaxAvgPane with YHistogram {
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val numberFormat = "#0.000000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, Font.BOLD, 14)
    val separatorHeight = 16
    val backgroundPaint = Color.WHITE
    val rangeGridlinePaint = Color.BLACK
    val domainGridlinePaint = Color.BLACK}

}
