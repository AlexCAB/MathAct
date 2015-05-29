package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.Colors
import mathact.utils.ui.components.{BorderFrame, MinMaxAvgPane, XYsPlot}
import mathact.utils.{ToolHelper, Environment, Tool}
import scala.math.random


/**
* Simple XY plot.
* Created by CAB on 12.03.2015.
*/

abstract class XYPlot(
  name:String = "",
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
  private var lines = List[Line]()
  //Classes
  protected case class Line(name:Option[String], color:Color, vals:()⇒(Array[Double],Array[Double])){
    def of(vals: ⇒(Array[Double], Array[Double])):Line = {
      val line = Line(name, color, ()⇒vals)
      lines :+= line
      line}}
  //DSL Methods
  def line(name:String = "", color:Color = new Color(0,0,0)):Line =
    Line(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, color, ()⇒(Array(0.0),Array(0.0)))
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "XYPlot")
  //UI
  private val plot = new XYsPlot(environment.params.XYPlot, screenW, screenH, drawPoints)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.XYPlot)
  private val frame = new BorderFrame(
      environment.layout, environment.params.XYPlot, helper.toolName, south = Some(minMaxAvg), center = Some(plot)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Prepare plot
      val ls = lines.zipWithIndex.map{
        case (Line(Some(n), c, _), _) ⇒ (n, c)
        case (Line(None, c, _), i) ⇒ ("L" + i, c)}
      plot.setLines(ls, minRange, maxRange,autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      val xys = lines.map{case Line(_,_,line) ⇒ {val (xs,ys) = line(); (xs.toList,ys.toList)}}
      plot.update(xys)
      minMaxAvg.update(xys.map(_._2).flatMap(e ⇒ e))
      updated()}
    def stop() = {
      frame.hide()}}}