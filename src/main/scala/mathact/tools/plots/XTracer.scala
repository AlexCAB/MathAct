package mathact.tools.plots
import java.awt.Color
import mathact.utils.dsl.Colors
import mathact.utils.{ToolHelper, Environment, Tool}
import mathact.utils.clockwork.{VisualisationGear, Gear}
import mathact.utils.ui.components.{XYsPlot, MinMaxAvgPane, BorderFrame}


/**
 * Trace given function in laid diapason.
 * Created by CAB on 12.03.2015.
 */

abstract class XTracer(
  name:String = "",
  val a:Double = -1,
  val b:Double = 1,
  val step:Double = 0.05,
  minRange:Double = -1,
  maxRange:Double = 1,
  autoRange:Boolean = false,
  drawPoints:Boolean = false,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool with Colors{
  //Variables
  private var traces = List[XTrace]()
  //Classes
  protected case class XTrace(name:Option[String], color:Color, proc:Double⇒Double){
    def of(proc:Double⇒Double):XTrace = {
      val trace = XTrace(name, color, proc)
      traces :+= trace
      trace}}
  //DSL Methods
  def trace(name:String = "", color:Color = new Color(0,0,0)):XTrace =
     XTrace(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, color, x ⇒ 0.0)
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "XTracer")
  //UI
  private val plot = new XYsPlot(environment.params.XTracer, screenW, screenH, drawPoints)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.XTracer)
  private val frame = new BorderFrame(
    environment.layout, environment.params.XTracer, helper.toolName, south = Some(minMaxAvg), center = Some(plot)){
    def closing() = {gear.endWork()}}
  //Functions
  private def doTrace() = {
    val xs = (a to b by step).toList
    val yss = traces.map{case XTrace(_,_,proc) ⇒ {xs.map(x ⇒ proc(x))}}
    plot.update(xs, yss)
    minMaxAvg.update(yss.flatMap(e ⇒ e))}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Prepare plot
      val lines = traces.zipWithIndex.map{
        case (XTrace(Some(n), c, _), _) ⇒ (n, c)
        case (XTrace(None, c, _), i) ⇒ ("L" + i, c)}
      plot.setLines(lines, minRange, maxRange, autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      doTrace()
      updated()}
    def stop() = {
      frame.hide()}}}




































