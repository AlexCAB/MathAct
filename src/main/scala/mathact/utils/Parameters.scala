package mathact.utils
import java.awt.{Color, Font}
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import mathact.utils.ui.UIParams._


/**
 * Parameters
 * Created by CAB on 10.03.2015.
 */

class Parameters {
  //PotBoard
  object PotBoard extends GridFrame with Potentiometer{
    val sliderHeight = 18
    val sliderWidth = 150
    val nameFont = new Font(Font.SERIF, Font.BOLD, 16)
    val nameHeight = 18
    val valueFont = new Font(Font.SERIF, 0, 14)
    val valueHeight = 18
    val backgroundColor:Color = Color.WHITE
    val sliderScale = 1000}
  //Doer
  object Doer extends FlowFrame with Executor with HorizontalSlider{
    val startEnabledIcon = new ImageIcon(getClass.getResource("/start_e.png"))
    val startDisableIcon = new ImageIcon(getClass.getResource("/start_d.png"))
    val stopEnabledIcon = new ImageIcon(getClass.getResource("/stop_e.png"))
    val stopDisableIcon = new ImageIcon(getClass.getResource("/stop_d.png"))
    val stepEnabledIcon = new ImageIcon(getClass.getResource("/step_e.png"))
    val stepDisableIcon = new ImageIcon(getClass.getResource("/step_d.png"))
    val executorButtonsSize = 33
    val sliderHeight = 16
    val sliderWidth = 180
    val sliderScale = 1000
    val backgroundColor = Color.WHITE}
  //XTracer
  object XTracer extends BorderFrame with MinMaxAvgPane with XYsPlot {
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
    val borderColor = Color.LIGHT_GRAY
    val borderSize = 1
    val textColor = Color.BLACK
    val backgroundColor = Color.WHITE}
  //Logger
  object Logger extends BorderFrame with TextLinePane{
    val textBackgroundColor = Color.BLACK
    val defaultColor = Color.GREEN
    val textFont:Font = new Font(Font.MONOSPACED, 0, 12)
    val backgroundColor = Color.WHITE}
  //XYPlot
  object XYPlot extends BorderFrame with MinMaxAvgPane with XYsPlot {
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
    val borderColor = Color.LIGHT_GRAY
    val borderSize = 1
    val textColor = Color.BLACK
    val backgroundColor = Color.WHITE}
  //YHistogram
  object YHistogram extends BorderFrame with MinMaxAvgPane with YHistPlot {
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
    val intervalMarkerColor = new Color(222, 222, 255, 128)
    val barsMargin:Double = .6
    val borderColor = Color.LIGHT_GRAY
    val borderSize = 1
    val textColor = Color.BLACK
    val backgroundColor = Color.WHITE}
  //XYHistogram
  object XYHistogram extends BorderFrame with MinMaxAvgPane with YHistPlot {
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
    val intervalMarkerColor = new Color(222, 222, 255, 128)
    val barsMargin = .6
    val borderColor = Color.LIGHT_GRAY
    val borderSize = 1
    val textColor = Color.BLACK
    val backgroundColor = Color.WHITE}
  //YChartRecorder
  object YChartRecorder extends BorderFrame with VariablesBar with Chart {
    val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight = 16
    val numberFormat = "#0.000000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, Font.BOLD, 14)
    val separatorHeight = 16
    val gridColor = new Color(200,200,200)
    val backgroundColor = Color.WHITE
    val borderColor = Color.LIGHT_GRAY
    val borderSize = 1
    val chartBackgroundColor = Color.WHITE}
  //CalcWithManual
  object AuthManPot extends BorderFrame with HorizontalSlider with NumberSpinner with AutoManButtons{
    val authEnabledIcon = new ImageIcon(getClass.getResource("/auth_e.png"))
    val authDisableIcon = new ImageIcon(getClass.getResource("/auth_d.png"))
    val manEnabledIcon = new ImageIcon(getClass.getResource("/man_e.png"))
    val manDisableIcon = new ImageIcon(getClass.getResource("/man_d.png"))
    val authManButtonsSize = 25
    val sliderHeight = 16
    val sliderWidth = 180
    val sliderScale = 1000
    val valueFont = new Font(Font.SERIF, Font.BOLD, 14)
    val valueHeight = 16
    val backgroundColor = Color.WHITE}
  //ValuesBoard
  object ValuesBoard extends GridFrame with Measurer{
    val nameFont = new Font(Font.SERIF, Font.BOLD, 16)
    val nameHeight = 16
    val numberFormat = "#0.000000000000000000"
    val numberFont = new Font(Font.SERIF, 0, 14)
    val numberHeight = 16
    val separatorFont = new Font(Font.SERIF, 0, 14)
    val separatorHeight = 16
    val backgroundColor = Color.WHITE}
  //Stepper
  object Stepper extends FlowFrame with Executor with HorizontalSlider with SelectionBar{
    val startEnabledIcon = new ImageIcon(getClass.getResource("/start_e.png"))
    val startDisableIcon = new ImageIcon(getClass.getResource("/start_d.png"))
    val stopEnabledIcon = new ImageIcon(getClass.getResource("/stop_e.png"))
    val stopDisableIcon = new ImageIcon(getClass.getResource("/stop_d.png"))
    val stepEnabledIcon = new ImageIcon(getClass.getResource("/step_e.png"))
    val stepDisableIcon = new ImageIcon(getClass.getResource("/step_d.png"))
    val executorButtonsSize = 28
    val sliderHeight = 16
    val sliderWidth = 180
    val sliderScale = 1000
    val nameFont = new Font(Font.SERIF, Font.BOLD, 16)
    val nameHeight = 16
    val listFont = new Font(Font.SERIF, 0, 18)
    val listHeight = 28
    val listInitWidth = 140
    val textColor:Color = Color.BLACK
    val backgroundColor = Color.WHITE}
  //SimpleStaticGraph
  object SimpleStaticGraph extends BorderFrame with SimpleGraph{
    val defaultNodeColor = Color.GRAY
    val defaultNodeSize = 10
    val defaultEdgeColor = Color.BLACK
    val defaultEdgeSize = 1
    val numberFormat = "#0.00000"
    val backgroundColor = Color.WHITE}
  //SimpleClassicPacman
  object SimpleClassicPacman extends Pacman
  with TVFrame with HorizontalSlider with Executor with ResetButton with NameLabel with NumberLabel{
    val backgroundColor = new Color(199,217,235)
    val squareSize:Int = 25
    val maze0       = ImageIO.read(getClass.getResource("/maze_0.png"))
    val maze1U      = ImageIO.read(getClass.getResource("/maze_1_u.png"))
    val maze1D      = ImageIO.read(getClass.getResource("/maze_1_d.png"))
    val maze1L      = ImageIO.read(getClass.getResource("/maze_1_l.png"))
    val maze1R      = ImageIO.read(getClass.getResource("/maze_1_r.png"))
    val maze2U      = ImageIO.read(getClass.getResource("/maze_2_u.png"))
    val maze2D      = ImageIO.read(getClass.getResource("/maze_2_d.png"))
    val maze2L      = ImageIO.read(getClass.getResource("/maze_2_l.png"))
    val maze2R      = ImageIO.read(getClass.getResource("/maze_2_r.png"))
    val maze3U      = ImageIO.read(getClass.getResource("/maze_3_u.png"))
    val maze3D      = ImageIO.read(getClass.getResource("/maze_3_d.png"))
    val maze3L      = ImageIO.read(getClass.getResource("/maze_3_l.png"))
    val maze3R      = ImageIO.read(getClass.getResource("/maze_3_r.png"))
    val maze4       = ImageIO.read(getClass.getResource("/maze_4.png"))
    val mazeV       = ImageIO.read(getClass.getResource("/maze_v.png"))
    val mazeH       = ImageIO.read(getClass.getResource("/maze_h.png"))
    val mazeE       = ImageIO.read(getClass.getResource("/maze_e.png"))
    val pacman      = ImageIO.read(getClass.getResource("/pacman.png"))
    val pacmanSU    = ImageIO.read(getClass.getResource("/pacman_s_u.png"))
    val pacmanSD    = ImageIO.read(getClass.getResource("/pacman_s_d.png"))
    val pacmanSL    = ImageIO.read(getClass.getResource("/pacman_s_l.png"))
    val pacmanSR    = ImageIO.read(getClass.getResource("/pacman_s_r.png"))
    val pacmanBU    = ImageIO.read(getClass.getResource("/pacman_b_u.png"))
    val pacmanBD    = ImageIO.read(getClass.getResource("/pacman_b_d.png"))
    val pacmanBL    = ImageIO.read(getClass.getResource("/pacman_b_l.png"))
    val pacmanBR    = ImageIO.read(getClass.getResource("/pacman_b_r.png"))
    val ghostB      = ImageIO.read(getClass.getResource("/ghost_b.png"))
    val ghostBU     = ImageIO.read(getClass.getResource("/ghost_b_u.png"))
    val ghostBD     = ImageIO.read(getClass.getResource("/ghost_b_d.png"))
    val ghostBL     = ImageIO.read(getClass.getResource("/ghost_b_l.png"))
    val ghostBR     = ImageIO.read(getClass.getResource("/ghost_b_r.png"))
    val ghostR      = ImageIO.read(getClass.getResource("/ghost_r.png"))
    val ghostRU     = ImageIO.read(getClass.getResource("/ghost_r_u.png"))
    val ghostRD     = ImageIO.read(getClass.getResource("/ghost_r_d.png"))
    val ghostRL     = ImageIO.read(getClass.getResource("/ghost_r_l.png"))
    val ghostRR     = ImageIO.read(getClass.getResource("/ghost_r_r.png"))
    val ghostW      = ImageIO.read(getClass.getResource("/ghost_w.png"))
    val ghostWU     = ImageIO.read(getClass.getResource("/ghost_w_u.png"))
    val ghostWD     = ImageIO.read(getClass.getResource("/ghost_w_d.png"))
    val ghostWL     = ImageIO.read(getClass.getResource("/ghost_w_l.png"))
    val ghostWR     = ImageIO.read(getClass.getResource("/ghost_w_r.png"))
    val pellet      = ImageIO.read(getClass.getResource("/pellet.png"))
    val powerPellet = ImageIO.read(getClass.getResource("/power_pellet.png"))
    val sliderHeight = 16
    val sliderWidth = 165
    val sliderScale = 1000
    val startEnabledIcon = new ImageIcon(getClass.getResource("/start_e.png"))
    val startDisableIcon = new ImageIcon(getClass.getResource("/start_d.png"))
    val stopEnabledIcon = new ImageIcon(getClass.getResource("/stop_e.png"))
    val stopDisableIcon = new ImageIcon(getClass.getResource("/stop_d.png"))
    val stepEnabledIcon = new ImageIcon(getClass.getResource("/step_e.png"))
    val stepDisableIcon = new ImageIcon(getClass.getResource("/step_d.png"))
    val executorButtonsSize = 28
    val resetButtonIcon:ImageIcon = new ImageIcon(getClass.getResource("/reset.png"))
    val resetButtonSize:Int = 28
    val textColor:Color = Color.BLUE
    val nameFont:Font = new Font(Font.SERIF, Font.BOLD, 14)
    val nameHeight:Int = 16
    val numberFormat = "#0000000"
    val numberFont = new Font(Font.SERIF, Font.BOLD, 14)
    val numberHeight = 16}
  object PacmanView extends Pacman with BorderFrame {
    val backgroundColor = Color.BLACK
    val squareSize:Int = 25
    val maze0       = ImageIO.read(getClass.getResource("/maze_0.png"))
    val maze1U      = ImageIO.read(getClass.getResource("/maze_1_u.png"))
    val maze1D      = ImageIO.read(getClass.getResource("/maze_1_d.png"))
    val maze1L      = ImageIO.read(getClass.getResource("/maze_1_l.png"))
    val maze1R      = ImageIO.read(getClass.getResource("/maze_1_r.png"))
    val maze2U      = ImageIO.read(getClass.getResource("/maze_2_u.png"))
    val maze2D      = ImageIO.read(getClass.getResource("/maze_2_d.png"))
    val maze2L      = ImageIO.read(getClass.getResource("/maze_2_l.png"))
    val maze2R      = ImageIO.read(getClass.getResource("/maze_2_r.png"))
    val maze3U      = ImageIO.read(getClass.getResource("/maze_3_u.png"))
    val maze3D      = ImageIO.read(getClass.getResource("/maze_3_d.png"))
    val maze3L      = ImageIO.read(getClass.getResource("/maze_3_l.png"))
    val maze3R      = ImageIO.read(getClass.getResource("/maze_3_r.png"))
    val maze4       = ImageIO.read(getClass.getResource("/maze_4.png"))
    val mazeV       = ImageIO.read(getClass.getResource("/maze_v.png"))
    val mazeH       = ImageIO.read(getClass.getResource("/maze_h.png"))
    val mazeE       = ImageIO.read(getClass.getResource("/maze_e.png"))
    val pacman      = ImageIO.read(getClass.getResource("/pacman.png"))
    val pacmanSU    = ImageIO.read(getClass.getResource("/pacman_s_u.png"))
    val pacmanSD    = ImageIO.read(getClass.getResource("/pacman_s_d.png"))
    val pacmanSL    = ImageIO.read(getClass.getResource("/pacman_s_l.png"))
    val pacmanSR    = ImageIO.read(getClass.getResource("/pacman_s_r.png"))
    val pacmanBU    = ImageIO.read(getClass.getResource("/pacman_b_u.png"))
    val pacmanBD    = ImageIO.read(getClass.getResource("/pacman_b_d.png"))
    val pacmanBL    = ImageIO.read(getClass.getResource("/pacman_b_l.png"))
    val pacmanBR    = ImageIO.read(getClass.getResource("/pacman_b_r.png"))
    val ghostB      = ImageIO.read(getClass.getResource("/ghost_b.png"))
    val ghostBU     = ImageIO.read(getClass.getResource("/ghost_b_u.png"))
    val ghostBD     = ImageIO.read(getClass.getResource("/ghost_b_d.png"))
    val ghostBL     = ImageIO.read(getClass.getResource("/ghost_b_l.png"))
    val ghostBR     = ImageIO.read(getClass.getResource("/ghost_b_r.png"))
    val ghostR      = ImageIO.read(getClass.getResource("/ghost_r.png"))
    val ghostRU     = ImageIO.read(getClass.getResource("/ghost_r_u.png"))
    val ghostRD     = ImageIO.read(getClass.getResource("/ghost_r_d.png"))
    val ghostRL     = ImageIO.read(getClass.getResource("/ghost_r_l.png"))
    val ghostRR     = ImageIO.read(getClass.getResource("/ghost_r_r.png"))
    val ghostW      = ImageIO.read(getClass.getResource("/ghost_w.png"))
    val ghostWU     = ImageIO.read(getClass.getResource("/ghost_w_u.png"))
    val ghostWD     = ImageIO.read(getClass.getResource("/ghost_w_d.png"))
    val ghostWL     = ImageIO.read(getClass.getResource("/ghost_w_l.png"))
    val ghostWR     = ImageIO.read(getClass.getResource("/ghost_w_r.png"))
    val pellet      = ImageIO.read(getClass.getResource("/pellet.png"))
    val powerPellet = ImageIO.read(getClass.getResource("/power_pellet.png"))}}
