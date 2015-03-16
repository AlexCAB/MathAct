package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{HistPlot, BorderFrame, MinMaxAvgPane}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.math.random


/**
 * Simple histogram.
 * Created by CAB on 15.03.2015.
 */


abstract class XYHistogram(
  name:String = "",
  minRange:Double = -1,
  maxRange:Double = 1,
  autoRange:Boolean = false,
  targetLower:Double = 0,
  targetUpper:Double = 0,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool{
  //Variables
  private var datas = List[(Color, ()⇒(Array[Double],Array[Double]))]()
  //DSL Methods
  def black(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,0),()⇒{line})}
  def white(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,255,255),()⇒{line})}
  def red(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,0,0),()⇒{line})}
  def lime(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,255,0),()⇒{line})}
  def blue(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,255),()⇒{line})}
  def yellow(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,255,0),()⇒{line})}
  def cyan(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,255,255),()⇒{line})}
  def magenta(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,0,255),()⇒{line})}
  def silver(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(192,192,192),()⇒{line})}
  def gray(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,128,128),()⇒{line})}
  def maroon(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,0,0),()⇒{line})}
  def olive(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,128,0),()⇒{line})}
  def green(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,128,0),()⇒{line})}
  def purple(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,0,128),()⇒{line})}
  def teal(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,128,128),()⇒{line})}
  def navy(line: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,128),()⇒{line})}
  def randColor(line: ⇒(Array[Double],Array[Double])):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{line})}
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "XYHistogram")
  //UI
  private val histogram = new HistPlot(
    environment.params.XYHistogram, screenW, screenH, minRange, maxRange, autoRange, targetLower, targetUpper)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.XYHistogram)
  private val frame = new BorderFrame(
      environment.layout, environment.params.XYHistogram,
      helper.toolName, south = Some(minMaxAvg), center = Some(histogram)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      val bars = datas.flatMap{case ((color, proc)) ⇒ {
        val (xs,ys) = proc()
        xs.zip(ys).map{case d ⇒ (color, d)}}}
      histogram.updateXY(bars)
      minMaxAvg.update(bars.map{case ((_,(_,y))) ⇒ y})
      updated()}
    def stop() = {
      frame.hide()}}}
