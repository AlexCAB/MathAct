package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.{Component, Color, BorderPanel}
import org.jfree.chart.ChartFactory
import org.jfree.chart.plot.XYPlot
import org.jfree.data.xy.{XYSeries,XYSeriesCollection}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ChartPanel

/**
 * Plot with one X and several Y lines.
 * Created by CAB on 13.03.2015.
 */

class XYsPlot(
  uiParams:UIParams.XYsPlot,
  lines:List[(String,Color)], //(Name, Color)
  minRange:Double,
  maxRange:Double,
  width:Int,
  height:Int,
  drawPoints:Boolean = true)
extends BorderPanel with ToyComponent{
  //Parameters
  val initWidth = width
  val initHeight = height
  //Create plot
  private val dss = lines.map{case(n,_) ⇒ new XYSeries(n)}
  private val rs = lines.map{case(_,c) ⇒ {
    val r = new XYLineAndShapeRenderer()
    r.setSeriesPaint(0, c)
    r.setBaseShapesVisible(drawPoints)
    r}}
  private val chart = ChartFactory.createXYLineChart(null, null, null,  null)
  private val plot = chart.getPlot.asInstanceOf[XYPlot]
  plot.setBackgroundPaint(uiParams.backgroundPaint)
  plot.setRangeGridlinePaint(uiParams.rangeGridlinePaint)
  plot.setDomainGridlinePaint(uiParams.domainGridlinePaint)
  private val a = plot.getRangeAxis
  a.setRange(minRange, maxRange)
  dss.zip(rs).zipWithIndex.map{case ((d,r),i) => {
    plot.setDataset(i, new XYSeriesCollection(d))
    plot.setRenderer(i, r)}}
  //Add
  layout(Component.wrap(new ChartPanel(chart))) = BorderPanel.Position.Center
  //Methods
  def


  var xs:List[Double] = _
  var yss:List[List[Double]] = _
  def run() = {
    dss.zip(yss).foreach{case(ds,ys) ⇒ {
      ds.clear()
      xs.zip(ys).foreach{case (x,y) => ds.add(x,y)}}}}


  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}













































