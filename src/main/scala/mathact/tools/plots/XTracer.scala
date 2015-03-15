package mathact.tools.plots
import java.awt.Color
import mathact.utils.{ToolHelper, Environment, Tool}
import mathact.utils.clockwork.{VisualisationGear, Gear}
import mathact.utils.ui.components.{XYsPlot, MinMaxAvgPane, BorderFrame}
import scala.math.random


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
extends Tool{
  //Variables
  private var procs = List[(Color, Double⇒Double, Option[String])]()
  //DSL Methods
  def black(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,0),proc,None); proc}
  def white(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,255),proc,None); proc}
  def red(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,0),proc,None); proc}
  def lime(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,0),proc,None); proc}
  def blue(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,255),proc,None); proc}
  def yellow(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,0),proc,None); proc}
  def cyan(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,255),proc,None); proc}
  def magenta(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,255),proc,None); proc}
  def silver(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(192,192,192),proc,None); proc}
  def gray(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,128),proc,None); proc}
  def maroon(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,0),proc,None); proc}
  def olive(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,0),proc,None); proc}
  def green(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,0),proc,None); proc}
  def purple(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,128),proc,None); proc}
  def teal(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,128),proc,None); proc}
  def navy(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,128),proc,None); proc}
  def randColor(proc:Double⇒Double):Double⇒Double = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    procs :+= (color,proc,None)
    proc}
  protected implicit class SecondOperator(name:String){
    def black(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,0),proc,Some(name)); proc}
    def white(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,255),proc,Some(name)); proc}
    def red(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,0),proc,Some(name)); proc}
    def lime(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,0),proc,Some(name)); proc}
    def blue(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,255),proc,Some(name)); proc}
    def yellow(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,0),proc,Some(name)); proc}
    def cyan(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,255),proc,Some(name)); proc}
    def magenta(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,255),proc,Some(name)); proc}
    def silver(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(192,192,192),proc,Some(name)); proc}
    def gray(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,128),proc,Some(name)); proc}
    def maroon(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,0),proc,Some(name)); proc}
    def olive(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,0),proc,Some(name)); proc}
    def green(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,0),proc,Some(name)); proc}
    def purple(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,128),proc,Some(name)); proc}
    def teal(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,128),proc,Some(name)); proc}
    def navy(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,128),proc,Some(name)); proc}
    def randColor(proc:Double⇒Double):Double⇒Double = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      procs :+= (color,proc,Some(name))
      proc}}
  //Helpers
  private val helper = new ToolHelper(this, name, "XTracer")
  //UI
  private val plot = new XYsPlot(environment.params.XTracer, screenW, screenH, drawPoints)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.XTracer)
  private val frame = new BorderFrame(environment, helper.toolName, south = Some(minMaxAvg), center = Some(plot)){
    def closing() = {gear.endWork()}}
  //Functions
  private def trace() = {
    val xs = (a to b by step).toList
    val yss = procs.map{case ((_,proc,_)) ⇒ {xs.map(x ⇒ proc(x))}}
    plot.update(xs, yss)
    minMaxAvg.update(yss.flatMap(e ⇒ e))}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Prepare plot
      val lines = procs.zipWithIndex.map{
        case ((c, _, Some(n)), _) ⇒ (n, c)
        case ((c, _, None), i) ⇒ ("L" + i, c)}
      plot.setLines(lines, minRange, maxRange,autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      trace()}
    def stop() = {
      frame.hide()}}}




































