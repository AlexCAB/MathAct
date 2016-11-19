/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.tools.plots

import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale
import javax.swing.SwingUtilities

import info.monitorenter.gui.chart.Chart2D
import info.monitorenter.gui.chart.IAxis.AxisTitle
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport
import info.monitorenter.gui.chart.traces.Trace2DLtd
import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UICommand
import mathact.data.discrete.TimedValue
import mathact.data.ui.C
import mathact.tools.Tool

import scalafx.embed.swing.SwingNode
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{FlowPane, BorderPane, HBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import java.awt.{Color ⇒ JColor}
import info.monitorenter.util.Range


/** Simple Chart recorder
  * Created by CAB on 13.11.2016.
  */

class ChartRecorder(implicit context: BlockContext)
extends Tool(context, "CR", "mathact/tools/plots/chart_recorder.png")
with ObjWiring with ObjOnStart with BlockUI with LinkIn[TimedValue]{
  //Parameters
  val defaultMinRange: Double = -1
  val defaultMaxRange: Double = 1
  val defaultAxisXName: String = "time in seconds"
  val defaultAxisYName: String = "value"
  val defaultMaxTraceSize: Int = 100
  val defaultPrefW: Double = 600
  val defaultPrefH: Double = 300
  val lineColors = List(Black, Gold, Gray, Green, Bisque, Blue, Honeydew)
  val gridColor = JColor.GRAY
  val chartBackgroundColor = JColor.WHITE
  //Variables
  @volatile private var _minRange     = defaultMinRange
  @volatile private var _maxRange     = defaultMaxRange
  @volatile private var _axisXName    = defaultAxisXName
  @volatile private var _axisYName    = defaultAxisYName
  @volatile private var _maxTraceSize = defaultMaxTraceSize
  @volatile private var _prefW        = defaultPrefW
  @volatile private var _prefH        = defaultPrefH
  @volatile private var _startTime    = -1L
  @volatile private var lines = List[Line]()
  @volatile private var currentColor = 0
  //Definitions
  private case class ChartUpdate(i: Int, value: Double, time: Long) extends UICommand
  private class Line(val i: Int, val name: String = "", val color: Color, ui: UI.type) extends Inflow[TimedValue] {
    protected def drain(v: TimedValue): Unit = ui.sendCommand(ChartUpdate(i, v.value, v.time))}
  private class ValueBox(name: String, color: Color) extends HBox(2){
    //Parameters
    alignment = Pos.Center
    //Helpers
    val decimalFormat = new DecimalFormat("0.0###",  new DecimalFormatSymbols(Locale.US))
    //UI
    val label =  new Label{
      text = "---; "
      textFill = color
      style = "-fx-font-size: 11pt;"}
    children = Seq(
      new Label{
        text = name + " = "
        textFill = color
        style = "-fx-font-weight: bold; -fx-font-size: 11pt;"},
      label)
    //Methods
    def update(value: Double): Unit = { label.text = decimalFormat.format(value) + "; " }}
  private class ChartUI extends SfxFrame{
    //Params
    title = "Timed vals pot" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Bounds
    val minRange = _minRange
    val maxRange = if(_maxRange > minRange) _maxRange else minRange + 1
    var startTime = _startTime
    //Components
    val (labels, traces) = lines
      .map{ line ⇒
        val label = new ValueBox(line.name, line.color)
        val trace = new Trace2DLtd(_maxTraceSize)
        trace.setName(null)
        trace.setColor(new JColor(
          (line.color.red * 255).toInt,
          (line.color.green * 255).toInt,
          (line.color.blue * 255).toInt))
        (label, trace)}
      .toVector
      .unzip
    val chartNode = new SwingNode()
    SwingUtilities.invokeLater(new Runnable{
      override def run(): Unit = {
        val chart = new Chart2D
        chart.getAxisX.setAxisTitle(new AxisTitle(_axisXName))
        chart.getAxisY.setAxisTitle(new AxisTitle(_axisYName))
        chart.getAxisX.setPaintGrid(true)
        chart.getAxisY.setPaintGrid(true)
        chart.getAxisY.setRangePolicy(new RangePolicyMinimumViewport(new Range(minRange, maxRange)))
        chart.setGridColor(gridColor)
        chart.setBackground(chartBackgroundColor)
        traces.foreach(t ⇒ chart.addTrace(t))
        chartNode.content = chart}})
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        prefWidth = _prefW
        prefHeight = _prefH
        center = chartNode
        bottom = new FlowPane{
          padding = Insets(1.0)
          children = labels}}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        startTime = if (startTime < 0) System.currentTimeMillis() else 0L
      case ChartUpdate(i, value, time) ⇒
        labels(i).update(value)
        traces(i).addPoint((time - startTime) / 1000.0, value)}}
  //UI registration and events handling
  UI(new ChartUI)
  //Functions
  private def nextColor: Color = {
    val c = lineColors(currentColor)
    currentColor = if(currentColor >= lineColors.size) 0 else currentColor + 1
    c}
  private def buildLine(name: String, color: Color): Line = {
    val line = new Line(lines.size, name, color, UI)
    lines +:= line
    line}
  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  //DSL
  def minRange: Double = _minRange
  def minRange_=(v: Double){ _minRange = v }
  def maxRange: Double = _maxRange
  def maxRange_=(v: Double){ _maxRange = v }
  def axisXName: String = _axisXName
  def axisXName_=(v: String){ _axisXName = v }
  def axisYName: String = _axisYName
  def axisYName_=(v: String){ _axisYName = v }
  def maxTraceSize: Int = _maxTraceSize
  def maxTraceSize_=(v: Int){ _maxTraceSize = v }
  def prefW: Double = _prefW
  def prefW_=(v: Double){ _prefW = v }
  def prefH: Double = _prefH
  def prefH_=(v: Double){ _prefH = v }
  def startTime: Long = _startTime
  def startTime_=(v: Long){ _startTime = v }
  //Inlets
  def in: Socket[TimedValue] = Inlet(buildLine(name = "Line",color = nextColor))
  def line(name: String = "Line", color: Color = nextColor): Socket[TimedValue] = Inlet(buildLine(name, color))}
