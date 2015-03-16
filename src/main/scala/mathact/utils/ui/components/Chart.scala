package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.{Component, Color, BorderPanel}
import info.monitorenter.gui.chart.IAxis.AxisTitle
import info.monitorenter.gui.chart.Chart2D
import info.monitorenter.gui.chart.traces.Trace2DLtd
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport
import info.monitorenter.util.Range


/**
 * Chart recorder
 * Created by CAB on 16.03.2015.
 */

class Chart(
  uiParams:UIParams.Chart,
  width:Int,
  height:Int,
  axisXName:String,
  axisYName:String,
  maxTSize:Int)
extends BorderPanel with UIComponent {
  //Variables
  private var traces = List[Trace2DLtd]()
  //Construction
  private val chart = new Chart2D
  chart.getAxisX.setAxisTitle(new AxisTitle(axisXName))
  chart.getAxisY.setAxisTitle(new AxisTitle(axisYName))
  chart.getAxisX.setPaintGrid(true)
  chart.getAxisY.setPaintGrid(true)
  chart.setGridColor(uiParams.gridColor)
  chart.setBackground(uiParams.chartBackgroundColor)
  layout(Component.wrap(chart)) = BorderPanel.Position.Center
  preferredSize = new Dimension(width, height)
  //Methods
  def setTraces(colors:List[Color], lower:Double, upper:Double):Unit = {
    chart.removeAllTraces()
    traces = colors.map(c ⇒ {
      val t = new Trace2DLtd(maxTSize)
      t.setName(null)
      t.setColor(c)
      chart.addTrace(t)
      t})
    chart.getAxisY.setRangePolicy(new RangePolicyMinimumViewport(new Range(lower, upper)))}
  def update(xys:List[Option[(Double,Double)]]):Unit = {
    xys.zip(traces).foreach{
      case (Some((x,y)),trace) ⇒ trace.addPoint(x,y)
      case _ ⇒}}}
