package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
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
  extends Tool{
  //Variables
  private var lines = List[(Color, ()⇒(Array[Double],Array[Double]), Option[String])]()
  //DSL Methods
  def black(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,0),()⇒{line},None)}
  def white(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,255,255),()⇒{line},None)}
  def red(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,0,0),()⇒{line},None)}
  def lime(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,255,0),()⇒{line},None)}
  def blue(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,255),()⇒{line},None)}
  def yellow(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,255,0),()⇒{line},None)}
  def cyan(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,255,255),()⇒{line},None)}
  def magenta(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,0,255),()⇒{line},None)}
  def silver(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(192,192,192),()⇒{line},None)}
  def gray(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,128,128),()⇒{line},None)}
  def maroon(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,0,0),()⇒{line},None)}
  def olive(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,128,0),()⇒{line},None)}
  def green(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,128,0),()⇒{line},None)}
  def purple(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,0,128),()⇒{line},None)}
  def teal(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,128,128),()⇒{line},None)}
  def navy(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,128),()⇒{line},None)}
  def randColor(line: ⇒(Array[Double],Array[Double])):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    lines :+= (color,()⇒{line},None)}
  protected implicit class SecondOperator(name:String){
    def black(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,0),()⇒{line},Some(name))}
    def white(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,255,255),()⇒{line},Some(name))}
    def red(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,0,0),()⇒{line},Some(name))}
    def lime(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,255,0),()⇒{line},Some(name))}
    def blue(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,255),()⇒{line},Some(name))}
    def yellow(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,255,0),()⇒{line},Some(name))}
    def cyan(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,255,255),()⇒{line},Some(name))}
    def magenta(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(255,0,255),()⇒{line},Some(name))}
    def silver(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(192,192,192),()⇒{line},Some(name))}
    def gray(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,128,128),()⇒{line},Some(name))}
    def maroon(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,0,0),()⇒{line},Some(name))}
    def olive(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,128,0),()⇒{line},Some(name))}
    def green(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,128,0),()⇒{line},Some(name))}
    def purple(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(128,0,128),()⇒{line},Some(name))}
    def teal(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,128,128),()⇒{line},Some(name))}
    def navy(line: ⇒(Array[Double],Array[Double])):Unit = {lines :+= (new Color(0,0,128),()⇒{line},Some(name))}
    def randColor(line: ⇒(Array[Double],Array[Double])):Unit = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      lines :+= (color,()⇒{line},Some(name))}}
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
        case ((c, _, Some(n)), _) ⇒ (n, c)
        case ((c, _, None), i) ⇒ ("L" + i, c)}
      plot.setLines(ls, minRange, maxRange,autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      val xys = lines.map{case ((_,line,_)) ⇒ {val (xs,ys) = line(); (xs.toList,ys.toList)}}
      plot.update(xys)
      minMaxAvg.update(xys.map(_._2).flatMap(e ⇒ e))
      updated()}
    def stop() = {
      frame.hide()}}}