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

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.analog.Sample
import mathact.parts.ui.Colors
import mathact.tools.Tool
import mathact.data.ui.C

import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._



/** Simple oscilloscope tool
  * Created by CAB on 24.11.2016.
  */

class SimpleScope(implicit context: BlockContext)
extends Tool(context, "SS", "mathact/tools/plots/simple_scope.png")
with ObjWiring with ObjOnStart with BlockUI with LinkIn[Sample] with Colors{
  //Parameters
  val defaultMinRange: Double = -1
  val defaultMaxRange: Double = 1
  val defaultTraceTime: Int = 1000  //In milli seconds
  val defaultPrefW: Double = 600
  val defaultPrefH: Double = 300
  val axisXName: String = "Time in seconds"
  val axisYName: String = "Input"
  //Properties
  @volatile private var _minRange     = defaultMinRange
  @volatile private var _maxRange     = defaultMaxRange
  @volatile private var _traceTime    = defaultTraceTime
  @volatile private var _prefW        = defaultPrefW
  @volatile private var _prefH        = defaultPrefH
  //Variables
  @volatile private var lines = List[Line]()
  @volatile private var currentColor = 0
  //Definitions

  private class Line(val i: Int, val name: String = "", val color: Color, ui: UI.type) extends Inflow[Sample] {

    protected def drain(s: Sample): Unit = ??? // ui.sendCommand(???)

  }


  private class ScopeUI extends SfxFrame{
    //Params
    title = "Scope" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
//    //Bounds
//    val minRange = _minRange
//    val maxRange = if(_maxRange > minRange) _maxRange else minRange + 1
//    var startTime = _startTime
//    //Components
//    val (labels, traces) = lines
//      .map{ line ⇒
//        val label = new ValueBox(line.name, line.color)
//        val trace = new Trace2DLtd(_maxTraceSize)
//        trace.setName(null)
//        trace.setColor(new JColor(
//          (line.color.red * 255).toInt,
//          (line.color.green * 255).toInt,
//          (line.color.blue * 255).toInt))
//        (label, trace)}
//      .toVector
//      .unzip
//    val chartNode = new SwingNode()
//    SwingUtilities.invokeLater(new Runnable{
//      override def run(): Unit = {
//        val chart = new Chart2D
//        chart.getAxisX.setAxisTitle(new AxisTitle(_axisXName))
//        chart.getAxisY.setAxisTitle(new AxisTitle(_axisYName))
//        chart.getAxisX.setPaintGrid(true)
//        chart.getAxisY.setPaintGrid(true)
//        chart.getAxisY.setRangePolicy(new RangePolicyMinimumViewport(new Range(minRange, maxRange)))
//        chart.setGridColor(gridColor)
//        chart.setBackground(chartBackgroundColor)
//        traces.foreach(t ⇒ chart.addTrace(t))
//        chartNode.content = chart}})
//    //Scene
//    scene = new Scene{
//      fill = White
//      root = new BorderPane{
//        prefWidth = _prefW
//        prefHeight = _prefH
//        center = chartNode
//        bottom = new FlowPane{
//          padding = Insets(1.0)
//          children = labels}}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
//        startTime = if (startTime < 0) System.currentTimeMillis() else 0L
//      case ChartUpdate(i, value, time) ⇒
//        labels(i).update(value)
//        traces(i).addPoint((time - startTime) / 1000.0, value)
    }
  }
  //UI registration and events handling
  UI(new ScopeUI)
  //Functions
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
  def traceTime: Int = _traceTime
  def traceTime_=(v: Int){ _traceTime = v }
  def prefW: Double = _prefW
  def prefW_=(v: Double){ _prefW = v }
  def prefH: Double = _prefH
  def prefH_=(v: Double){ _prefH = v }
  //Inlets
  def in: Socket[Sample] = Inlet(buildLine(name = "Line",color = nextColor))
  def line(name: String = "Line", color: Color = nextColor): Socket[Sample] = Inlet(buildLine(name, color))



}