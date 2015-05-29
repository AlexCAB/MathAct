package mathact.utils.ui.components
import java.awt.{Paint,Color}
import mathact.utils.ui.UIParams
import org.jfree.chart.plot.{IntervalMarker, PlotOrientation, XYPlot}
import org.jfree.chart.renderer.xy.{StandardXYBarPainter, ClusteredXYBarRenderer}
import org.jfree.chart.{ChartPanel, ChartFactory}
import org.jfree.data.xy.{XYSeriesCollection,XYSeries}
import org.jfree.ui.Layer
import scala.swing.{BorderPanel,Color,Dimension,Component}


/**
 * Y data chart
 * Created by CAB on 16.03.2015.
 */

class HistPlot(
  uiParams:UIParams.YHistPlot,
  width:Int,
  height:Int,
  lower:Double,
  upper:Double,
  autoRange:Boolean,
  targetLower:Double,
  targetUpper:Double)
extends BorderPanel with UIComponent{
  //Variables
  private var colors = List[Color]()
  //Construction
  preferredSize = new Dimension(width, height)
  //Series
  private val xySeries = new XYSeries("")
  private val dataset = new XYSeriesCollection(xySeries)
  //Chart
  private val chart = ChartFactory.createXYBarChart(
    null,
    null,
    false,
    null,
    dataset,
    PlotOrientation.VERTICAL,
    false, //names
    true,
    false)
  private val plot = chart.getPlot.asInstanceOf[XYPlot]
  //Renderer
  private val renderer = new ClusteredXYBarRenderer{
    override def getItemPaint(row:Int, column:Int):Paint = {
      colors.size match {
        case s if s > column ⇒ colors(column)
        case _ ⇒ Color.GRAY}}}
  renderer.setMargin(uiParams.barsMargin)
  renderer.setShadowVisible(false)
  private val painter = new StandardXYBarPainter
  renderer.setBarPainter(painter)
  plot.setRenderer(renderer)
  //Params
  plot.setNoDataMessage("NO DATA!")
  plot.setBackgroundPaint(uiParams.backgroundPaint)
  plot.setRangeGridlinePaint(uiParams.rangeGridlinePaint)
  plot.setDomainGridlinePaint(uiParams.domainGridlinePaint)
  plot.getRangeAxis.setRange(lower, upper)
  plot.getRangeAxis.setAutoRange(autoRange)
  //Target
  if(targetLower != targetUpper) {
    val target = new IntervalMarker(targetLower, targetUpper)
    target.setPaint(uiParams.intervalMarkerColor)
    plot.addRangeMarker(target, Layer.BACKGROUND)}
  //Add
  layout(Component.wrap(new ChartPanel(chart))) = BorderPanel.Position.Center
  //Methods
  def updateY(bars:List[(Color,Double)]):Unit = {
    val (colors, values) = bars.unzip
    this.colors = colors
    xySeries.clear()
    (1 to bars.size).zip(values).foreach{case (x,y) ⇒ xySeries.add(x,y)}}
  def updateXY(bars:List[(Color,(Double,Double))]):Unit = {
    val (colors, values) = bars.unzip
    this.colors = colors
    xySeries.clear()
    values.foreach{case (x,y) ⇒ xySeries.add(x,y)}}}
