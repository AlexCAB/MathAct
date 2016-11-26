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

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.analog.Sample
import mathact.parts.ui.Colors
import mathact.tools.Tool
import mathact.data.ui.{C, E}

import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.Includes._
import org.jfree.chart.{ChartFactory, JFreeChart}
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import org.jfree.fx.FXGraphics2D
import javafx.scene.canvas.Canvas

import mathact.core.bricks.ui.interaction.UICommand

import scala.concurrent.Future
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Spinner, SpinnerValueFactory}
import scalafx.scene.layout.{BorderPane, HBox, StackPane}


/** Simple oscilloscope tool
  * Created by CAB on 24.11.2016.
  */

class SimpleScope(implicit context: BlockContext)
extends Tool(context, "SS", "mathact/tools/plots/simple_scope.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkIn[Sample] with Colors{
  //Parameters
  val defaultMinRange: Double = -1
  val defaultMaxRange: Double = 1
  val defaultTraceTime: Int = 1000  //In milli seconds
  val minTraceTime: Int = 10        //In milli seconds
  val maxTraceTime: Int = 1000000   //In milli seconds
  val traceTimeStep: Int = 1000     //In milli seconds
  val defaultAutoRange: Boolean = false
  val defaultDrawPoints: Boolean = false
  val defaultPrefW: Double = 600
  val defaultPrefH: Double = 300
  val uiAxisXName: String = "Time in milli seconds"
  val uiAxisYName: String = "Input"
  val uiTraceTimeName: String = "Trace time:"
  val uiElemsHeight: Int = 25
  val uiBtnSize: Int = 25
  val uiSpinnerWidth: Int = 100
  val uiLabelStyle: String =  "-fx-font-weight: bold; -fx-font-size: 11pt;"
  val uiBackgroundPaint: Color = Color.White
  val uiRangeGridLinePaint: Color = Color.Gray
  val uiDomainGridLinePaint: Color = Color.Gray
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
  @volatile private var cleanWorker: Option[CleanWorker] = None
  @volatile private var currentTraceTime = defaultTraceTime
  @volatile private var cleanTime = 0L
  //Definitions
  private case class UpdateTrace(i: Int, s: Sample) extends UICommand
  private class Line(val i: Int, val name: String = "", val color: Color, ui: UI.type) extends Inflow[Sample] {
    protected def drain(s: Sample): Unit =  ui.sendCommand(UpdateTrace(i, s))}
  private class CleanWorker(val period: Int, ui: UI.type){
    //Values
    val startTime = System.currentTimeMillis()
    //Variables
    @volatile private var work = true
    @volatile private var count = 0L
    @volatile private object Mutex
    //Methods
    def doStop(): Unit = {
      work = false
      Mutex.synchronized(Mutex.notifyAll())}
    //Worker
    Future{ while (work) {
      //Set prev time
      val current = System.currentTimeMillis()
      val error = (current - startTime) % period
      val duration = period - error
      count += 1
      //Sleep
      Mutex.synchronized(Mutex.wait(if(duration < 1) 1 else duration))
      //Cleaning lines
      ui.sendCommand(C.Clean)}}}
  private class ScopeUI extends SfxFrame{
    //Params
    title = "Scope" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Bounds
    val minRange = _minRange
    val maxRange =
      if(_maxRange > minRange) _maxRange else minRange + 1
    val traceTime =
      if(_traceTime < minTraceTime) minTraceTime else if(_traceTime > maxTraceTime) maxTraceTime else _traceTime
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
    val traces = lines
      .map{ line ⇒
        val dataSeries = new XYSeries(line.name match{case "" ⇒ null case n ⇒ n})
        val renderer = new XYLineAndShapeRenderer()
        renderer.setSeriesPaint(0, line.color.toJColor)
        renderer.setBaseShapesVisible(_drawPoints)
        (line.i, (dataSeries, renderer))}
      .toMap
    val chart = ChartFactory.createXYLineChart(null, uiAxisXName, uiAxisYName, null)
    val plot = chart.getPlot.asInstanceOf[XYPlot]
    plot.setBackgroundPaint(uiBackgroundPaint.toJColor)
    plot.setRangeGridlinePaint(uiRangeGridLinePaint.toJColor)
    plot.setDomainGridlinePaint(uiDomainGridLinePaint.toJColor)
    plot.getRangeAxis.setRange(minRange, maxRange)
    plot.getDomainAxis.setRange(0, traceTime)
    plot.getRangeAxis.setAutoRange(_autoRange)
    traces.foreach{ case (i, (series, renderer)) ⇒
      plot.setDataset(i, new XYSeriesCollection(series))
      plot.setRenderer(i, renderer)}
    val chartCanvas = new ChartCanvas(chart)
    val spinnerTraceTime = new Spinner[Int]{
      prefHeight = uiElemsHeight
      prefWidth = uiSpinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{
        plot.getDomainAxis.setRange(0, value.value)
        sendEvent(E.IntValueChanged(value.value))}
      valueFactory = new SpinnerValueFactory
        .IntegerSpinnerValueFactory(minTraceTime, maxTraceTime, traceTime, traceTimeStep)
        .asInstanceOf[SpinnerValueFactory[Int]]}
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        prefHeight = _prefH
        prefWidth = _prefW
        center = new StackPane{
          children = chartCanvas
          chartCanvas.widthProperty().bind( this.delegate.widthProperty)
          chartCanvas.heightProperty().bind( this.delegate.heightProperty)}
        bottom = new HBox(2){
          padding = Insets(4.0)
          alignment = Pos.CenterLeft
          children = Seq(
            new Label {
              prefHeight = uiElemsHeight
              text = uiTraceTimeName
              style = uiLabelStyle},
            spinnerTraceTime)}}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        chartCanvas.disable = false
        spinnerTraceTime.disable  = false
        sendEvent(E.IntValueChanged(traceTime))
      case C.Stop ⇒
        chartCanvas.disable = true
        spinnerTraceTime.disable  = true
      case UpdateTrace(i, s) ⇒ if(s.time >= cleanTime ) {
        traces(i)._1.add(s.time - cleanTime, s.value)
        chartCanvas.draw()}
      case C.Clean ⇒
        traces.foreach{ case (_, (series, _)) ⇒ series.clear() }
        cleanTime = System.currentTimeMillis()}}
  //UI registration and events handling
  UI(new ScopeUI)
  //Functions
  private def buildLine(name: String, color: Color): Line = {
    val line = new Line(lines.size, name, color, UI)
    lines +:= line
    line}
  //On start and on stop
  protected def onStart(): Unit = {
    UI.sendCommand(C.Start) }
  protected def onStop(): Unit = {
    UI.sendCommand(C.Stop)
    cleanWorker.foreach(_.doStop())}
  //UI handling
  UI.onEvent{ case E.IntValueChanged(newTraceTime) ⇒
    currentTraceTime = newTraceTime
    cleanWorker.foreach(_.doStop())
    cleanWorker = Some(new CleanWorker(newTraceTime, UI))}
  //DSL
  def minRange: Double = _minRange
  def minRange_=(v: Double){ _minRange = v }
  def maxRange: Double = _maxRange
  def maxRange_=(v: Double){ _maxRange = v }
  def traceTime: Int = _traceTime
  def traceTime_=(v: Int){ _traceTime = v }
  def autoRange: Boolean = _autoRange
  def autoRange_=(v: Boolean){ _autoRange = v }
  def drawPoints: Boolean = _drawPoints
  def drawPoints_=(v: Boolean){ _drawPoints = v }
  def prefW: Double = _prefW
  def prefW_=(v: Double){ _prefW = v }
  def prefH: Double = _prefH
  def prefH_=(v: Double){ _prefH = v }
  //Inlets
  def in: Socket[Sample] = Inlet(buildLine(name = "Line",color = nextColor))
  def line(name: String = "Line", color: Color = nextColor): Socket[Sample] = Inlet(buildLine(name, color))}