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

import java.awt.geom.Rectangle2D
import javax.swing.SwingUtilities

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.analog.Sample
import mathact.parts.ui.Colors
import mathact.tools.Tool
import mathact.data.ui.C

import scalafx.embed.swing.SwingNode
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.Includes._
import org.jfree.chart.{ChartFactory, JFreeChart}
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import javafx.scene.canvas.Canvas

import org.jfree.fx.FXGraphics2D

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{BorderPane, StackPane}




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
  val defaultAutoRange: Boolean = true
  val defaultDrawPoints: Boolean = false
  val defaultPrefW: Double = 600
  val defaultPrefH: Double = 300
  val axisXName: String = "Time in seconds"
  val axisYName: String = "Input"
  val backgroundPaint: Color = Color.White
  val rangeGridLinePaint: Color = Color.Gray
  val domainGridLinePaint: Color = Color.Gray
  //Properties
  @volatile private var _minRange   = defaultMinRange
  @volatile private var _maxRange   = defaultMaxRange
  @volatile private var _traceTime  = defaultTraceTime
  @volatile private var _autoRange  = defaultAutoRange
  @volatile private var _drawPoints = defaultDrawPoints
  @volatile private var _prefW      = defaultPrefW
  @volatile private var _prefH      = defaultPrefH
  //Variables
  @volatile private var lines = List[Line]()
  @volatile private var currentColor = 0
  //Definitions

  private class Line(val i: Int, val name: String = "", val color: Color, ui: UI.type) extends Inflow[Sample] {
    //Construction
    val dataSeries = new XYSeries(name match{case "" ⇒ null case n ⇒ n})
    val renderer = new XYLineAndShapeRenderer()
    renderer.setSeriesPaint(0, color.toJColor)
    renderer.setBaseShapesVisible(_drawPoints)

    //
    dataSeries.add(1,1 + i)
    dataSeries.add(2,2 + i)
    dataSeries.add(3,3 + i)



    protected def drain(s: Sample): Unit = ??? // ui.sendCommand(???)

  }


  private class ScopeUI extends SfxFrame{
    //Params
    title = "Scope" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Bounds
    val minRange = _minRange
    val maxRange = if(_maxRange > minRange) _maxRange else minRange + 1
    //Definitions
    class ChartCanvas(chart: JFreeChart) extends Canvas {
      //Construction
      val g2 = new FXGraphics2D(getGraphicsContext2D)
      widthProperty.onChange{ draw() }
      heightProperty.onChange{ draw() }
      //Methods
      def draw(): Unit = {
        val width = getWidth
        val height = getHeight
        getGraphicsContext2D.clearRect(0, 0, width, height)
        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height))}
      override def isResizable: Boolean = true
      override def prefWidth(height: Double): Double = getWidth
      override def prefHeight(width: Double): Double =  getHeight}
    //Components
    val chart = ChartFactory.createXYLineChart(null, axisXName, axisYName, null)
    val plot = chart.getPlot.asInstanceOf[XYPlot]
    plot.setBackgroundPaint(backgroundPaint.toJColor)
    plot.setRangeGridlinePaint(rangeGridLinePaint.toJColor)
    plot.setDomainGridlinePaint(domainGridLinePaint.toJColor)
    plot.getRangeAxis.setRange(minRange, maxRange)
    plot.getRangeAxis.setAutoRange(_autoRange)
    lines.foreach{ line ⇒
      plot.setDataset(line.i, new XYSeriesCollection(line.dataSeries))
      plot.setRenderer(line.i, line.renderer)}
    val chartCanvas = new ChartCanvas(chart)
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        prefHeight = _prefH
        prefWidth = _prefW
        center = new StackPane{
          children = chartCanvas
          chartCanvas.widthProperty().bind( this.delegate.widthProperty)
          chartCanvas.heightProperty().bind( this.delegate.heightProperty)}}}
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
  def autoRange: Boolean = _autoRange
  def autoRange_=(v: Boolean){ _drawPoints = v }
  def drawPoints: Boolean = _autoRange
  def drawPoints_=(v: Boolean){ _drawPoints = v }
  def prefW: Double = _prefW
  def prefW_=(v: Double){ _prefW = v }
  def prefH: Double = _prefH
  def prefH_=(v: Double){ _prefH = v }
  //Inlets
  def in: Socket[Sample] = Inlet(buildLine(name = "Line",color = nextColor))
  def line(name: String = "Line", color: Color = nextColor): Socket[Sample] = Inlet(buildLine(name, color))}