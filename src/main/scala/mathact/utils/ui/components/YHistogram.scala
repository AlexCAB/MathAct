package mathact.utils.ui.components

import java.awt.Paint

import mathact.utils.ui.UIParams
import org.jfree.chart.plot.{PlotOrientation, XYPlot}
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.xy.{XYItemRenderer, XYLineAndShapeRenderer}
import org.jfree.chart.{ChartPanel, ChartFactory, JFreeChart}
import org.jfree.data.general.DatasetUtilities
import org.jfree.data.xy.{XYSeriesCollection,XYSeries}
import scala.swing.{BorderPanel,Color,Dimension,Component}


/**
 * Histogram panel
 * Created by CAB on 15.03.2015.
 */

class YHistogram (
  uiParams:UIParams.YHistogram,
  width:Int,
  height:Int)
extends BorderPanel with UIComponent{



  //Variables
  private var chart:Option[(JFreeChart,XYSeries)] = None
  //Functions
  private def createHistogram(bars:List[Color], lower:Double, upper:Double, autoRange:Boolean)
  :(JFreeChart,XYSeries) = {
    //Renderer
//    object Renderer extends XYItemRenderer {
//      override def getItemPaint(row:Int, column:Int):Paint = bars(column)}
    //Create plot
    val histData = (0 until bars.size).map(i ⇒ {i → .2})
    val xySeries = new XYSeries("")
    println(bars.size)
    histData.foreach{case (v,n) ⇒ xySeries.add(v,n)}
    val dataset = new XYSeriesCollection(xySeries)
    val chart = ChartFactory.createXYBarChart(
      null,
      null,
      false,
      null,
      dataset,
      PlotOrientation.VERTICAL,
      false,
      true,
      false)
    val plot = chart.getPlot.asInstanceOf[XYPlot]
    plot.setNoDataMessage("NO DATA!")
    plot.setBackgroundPaint(uiParams.backgroundPaint)
    plot.setRangeGridlinePaint(uiParams.rangeGridlinePaint)
    plot.setDomainGridlinePaint(uiParams.domainGridlinePaint)
    plot.getRangeAxis.setRange(lower, upper)
    plot.getRangeAxis.setAutoRange(autoRange)
//    plot.setRenderer(Renderer)



//
//    val dss = bars.map{case color ⇒ new XYSeries("")}
//
//
//    val chart = ChartFactory.createXYLineChart(null, null, null, null)
//
//
//


//    //Renderer
//    val rs = lines.map{case(_,c) ⇒ {
//      val r = new XYLineAndShapeRenderer()
//      r.setSeriesPaint(0, c)
//      r.setBaseShapesVisible(drawPoints)
//      r}}
//    //Data
//    dss.zip(rs).zipWithIndex.map{case ((d,r),i) => {
//      plot.setDataset(i, new XYSeriesCollection(d))
//      plot.setRenderer(i, r)}}
    (chart, xySeries)



  }
  //Construction
  preferredSize = new Dimension(width, height)
  //Methods
  def setBars(bars:List[Color], lower:Double, upper:Double, autoRange:Boolean):Unit = {
    println("TTTTTTTTTT")
    chart = Some(createHistogram(bars, lower, upper, autoRange))
    layout(Component.wrap(new ChartPanel(chart.get._1))) = BorderPanel.Position.Center}
//
//
//  def update(xs:List[Double], yss:List[List[Double]]):Unit = chart.map{case (_,dss) ⇒ {
//    dss.zip(yss).foreach{case(ds,ys) ⇒ {
//      ds.clear()
//      xs.zip(ys).foreach{case (x,y) => ds.add(x,y)}}}}}
//  def update(xys:List[(List[Double],List[Double])]):Unit = chart.map{case (_,dss) ⇒ {
//    dss.zip(xys).foreach{case(ds,(xs,ys)) ⇒ {
//      ds.clear()
//      xs.zip(ys).foreach{case (x,y) => ds.add(x,y)}}}}}





}