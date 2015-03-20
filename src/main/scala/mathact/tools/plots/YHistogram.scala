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

abstract class YHistogram(
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
  private var datas = List[(Color, ()⇒Array[Double])]()
  //DSL Methods
  def black(bar: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{Array(bar)})}
  def white(bar: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{Array(bar)})}
  def red(bar: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{Array(bar)})}
  def lime(bar: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{Array(bar)})}
  def blue(bar: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{Array(bar)})}
  def yellow(bar: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{Array(bar)})}
  def cyan(bar: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{Array(bar)})}
  def magenta(bar: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{Array(bar)})}
  def silver(bar: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{Array(bar)})}
  def gray(bar: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{Array(bar)})}
  def maroon(bar: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{Array(bar)})}
  def olive(bar: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{Array(bar)})}
  def green(bar: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{Array(bar)})}
  def purple(bar: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{Array(bar)})}
  def teal(bar: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{Array(bar)})}
  def navy(bar: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{Array(bar)})}
  def randColor(bar: ⇒Double):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{Array(bar)})}
  def blackArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,0),()⇒{bars})}
  def whiteArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(255,255,255),()⇒{bars})}
  def redArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(255,0,0),()⇒{bars})}
  def limeArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,255,0),()⇒{bars})}
  def blueArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,255),()⇒{bars})}
  def yellowArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(255,255,0),()⇒{bars})}
  def cyanArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,255,255),()⇒{bars})}
  def magentaArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(255,0,255),()⇒{bars})}
  def silverArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(192,192,192),()⇒{bars})}
  def grayArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(128,128,128),()⇒{bars})}
  def maroonArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(128,0,0),()⇒{bars})}
  def oliveArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(128,128,0),()⇒{bars})}
  def greenArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,128,0),()⇒{bars})}
  def purpleArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(128,0,128),()⇒{bars})}
  def tealArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,128,128),()⇒{bars})}
  def navyArray(bars: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,128),()⇒{bars})}
  def randColorArray(bars: ⇒Array[Double]):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{bars})}
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "YHistogram")
  //UI
  private val histogram = new HistPlot(
    environment.params.YHistogram, screenW, screenH, minRange, maxRange, autoRange, targetLower, targetUpper)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.YHistogram)
  private val frame = new BorderFrame(
      environment.layout, environment.params.YHistogram,
      helper.toolName, south = Some(minMaxAvg), center = Some(histogram)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      val bars = datas.flatMap{case ((c, d)) ⇒ d().map(v ⇒ (c, v))}
      histogram.updateY(bars)
      minMaxAvg.update(bars.map(_._2))
      updated()}
    def stop() = {
      frame.hide()}}}
