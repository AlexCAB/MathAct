package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.Colors
import mathact.utils.ui.components.{BorderFrame,Chart,VariablesBar}
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Simple chart recorder
 * Created by CAB on 16.03.2015.
 */

abstract class YChartRecorder (
  name:String = "",
  minRange:Double = -1,
  maxRange:Double = 1,
  autoRange:Boolean = false,
  autoUpdate:Boolean = false,
  axisXName:String = "",
  axisYName:String = "",
  maxTraceSize:Int = 100,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool with Colors{
  //Variables
  private var traces = List[Trace]()
  private var xCounter = .0
  //Classes
  protected case class Trace(name:Option[String], color:Color, proc:()⇒Double){
    def of(proc: ⇒Double):Trace = {
      val trace = Trace(name, color, ()⇒proc)
      traces :+= trace
      trace}}
  //DSL Methods
  def trace(name:String = "", color:Color = new Color(0,0,0)):Trace =
    Trace(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, color, () ⇒ 0.0)
  def update() = {
    val xys = traces.map{case Trace(_,_,data) ⇒ Some(xCounter, data())}
    //Chart
    chart.update(xys)
    xCounter += 1
    //VariablesBar
    varBar.update(xys.map{case Some((_,y)) ⇒ y; case _ ⇒ Double.NaN})
    updated()}
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "YChartRecorder")
  //UI
  private val chart = new Chart(environment.params.YChartRecorder, screenW, screenH, axisXName, axisYName, maxTraceSize)
  private val varBar = new VariablesBar(environment.params.YChartRecorder)
  private val frame = new BorderFrame(
      environment.layout, environment.params.YChartRecorder, helper.toolName, south = Some(varBar), center = Some(chart)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      chart.setTraces(traces.map{case Trace(_,c,_) ⇒ c}, minRange, maxRange)
      varBar.setVars(traces.zipWithIndex.map{
        case (Trace(Some(n),c,_),_) ⇒ (n,c)
        case (Trace(None,c,_),i) ⇒ ("T" + i,c)})
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = if(autoUpdate){
      helper.thisTool.update()}
    def stop() = {
      frame.hide()}}}
