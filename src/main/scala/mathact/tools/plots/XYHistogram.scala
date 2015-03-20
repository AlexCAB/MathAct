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
  def black(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,0,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def white(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(255,255,255),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def red(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(255,0,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def lime(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,255,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def blue(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,0,255),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def yellow(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(255,255,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def cyan(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,255,255),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def magenta(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(255,0,255),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def silver(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(192,192,192),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def gray(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(128,128,128),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def maroon(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(128,0,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def olive(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(128,128,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def green(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,128,0),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def purple(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(128,0,128),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def teal(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,128,128),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def navy(bar: ⇒(Double,Double)):Unit = {datas :+= (new Color(0,0,128),()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def randColor(bar: ⇒(Double,Double)):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{val d = bar; (Array(d._1),Array(d._2))})}
  def blackArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,0),()⇒{bars})}
  def whiteArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,255,255),()⇒{bars})}
  def redArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,0,0),()⇒{bars})}
  def limeArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,255,0),()⇒{bars})}
  def blueArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,255),()⇒{bars})}
  def yellowArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,255,0),()⇒{bars})}
  def cyanArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,255,255),()⇒{bars})}
  def magentaArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(255,0,255),()⇒{bars})}
  def silverArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(192,192,192),()⇒{bars})}
  def grayArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,128,128),()⇒{bars})}
  def maroonArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,0,0),()⇒{bars})}
  def oliveArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,128,0),()⇒{bars})}
  def greenArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,128,0),()⇒{bars})}
  def purpleArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(128,0,128),()⇒{bars})}
  def tealArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,128,128),()⇒{bars})}
  def navyArray(bars: ⇒(Array[Double],Array[Double])):Unit = {datas :+= (new Color(0,0,128),()⇒{bars})}
  def randColorArray(bars: ⇒(Array[Double],Array[Double])):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{bars})}
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
