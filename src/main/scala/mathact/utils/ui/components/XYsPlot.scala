package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.{Component, Color, BorderPanel}
import org.jfree.chart.{JFreeChart, ChartFactory, ChartPanel}
import org.jfree.chart.plot.XYPlot
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer


/**
 * Plot with one X and several Y lines.
 * Created by CAB on 13.03.2015.
 */

class XYsPlot(
  uiParams:UIParams.XYsPlot,
  width:Int,
  height:Int,
  drawPoints:Boolean = true)
extends BorderPanel with UIComponent{
  //Variables
  private var chart:Option[(JFreeChart,List[XYSeries])] = None
  //Functions
  private def createPlot(lines:List[(String,Color)], lower:Double, upper:Double, autoRange:Boolean)
  :(JFreeChart,List[XYSeries]) = {
    //Create plot
    val dss = lines.map{case(n,_) ⇒ new XYSeries(n)}
    val chart = ChartFactory.createXYLineChart(null, null, null, null)
    val plot = chart.getPlot.asInstanceOf[XYPlot]
    plot.setBackgroundPaint(uiParams.backgroundPaint)
    plot.setRangeGridlinePaint(uiParams.rangeGridlinePaint)
    plot.setDomainGridlinePaint(uiParams.domainGridlinePaint)
    plot.getRangeAxis.setRange(lower, upper)
    plot.getRangeAxis.setAutoRange(autoRange)
    //Renderer
    val rs = lines.map{case(_,c) ⇒ {
      val r = new XYLineAndShapeRenderer()
      r.setSeriesPaint(0, c)
      r.setBaseShapesVisible(drawPoints)
      r}}
    //Data
    dss.zip(rs).zipWithIndex.map{case ((d,r),i) => {
      plot.setDataset(i, new XYSeriesCollection(d))
      plot.setRenderer(i, r)}}
    (chart, dss)}
  //Construction
  preferredSize = new Dimension(width, height)
  //Methods
  def setLines(lines:List[(String,Color)], lower:Double, upper:Double, autoRange:Boolean):Unit = {
    chart = Some(createPlot(lines, lower, upper, autoRange))
    layout(Component.wrap(new ChartPanel(chart.get._1))) = BorderPanel.Position.Center}
  def update(xs:List[Double], yss:List[List[Double]]):Unit = chart.map{case (_,dss) ⇒ {
    dss.zip(yss).foreach{case(ds,ys) ⇒ {
      ds.clear()
      xs.zip(ys).foreach{case (x,y) => ds.add(x,y)}}}}}
  def update(xys:List[(List[Double],List[Double])]):Unit = chart.map{case (_,dss) ⇒ {
    dss.zip(xys).foreach{case(ds,(xs,ys)) ⇒ {
      ds.clear()
      xs.zip(ys).foreach{case (x,y) => ds.add(x,y)}}}}}}
