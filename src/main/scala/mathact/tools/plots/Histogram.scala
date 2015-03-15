package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{BorderFrame, MinMaxAvgPane, YHistogram}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.math.random


/**
 * Simple histogram.
 * Created by CAB on 15.03.2015.
 */


abstract class Histogram(
  name:String = "",
  minRange:Double = -1,
  maxRange:Double = 1,
  autoRange:Boolean = false,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool{
  //Variables
  private var datas = List[(Color, ()⇒Array[Double])]()
  //DSL Methods
  def blackArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,0),()⇒{line})}
  def whiteArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(255,255,255),()⇒{line})}
  def redArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(255,0,0),()⇒{line})}
  def limeArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,255,0),()⇒{line})}
  def blueArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,255),()⇒{line})}
  def yellowArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(255,255,0),()⇒{line})}
  def cyanArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,255,255),()⇒{line})}
  def magentaArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(255,0,255),()⇒{line})}
  def silverArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(192,192,192),()⇒{line})}
  def grayArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(128,128,128),()⇒{line})}
  def maroonArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(128,0,0),()⇒{line})}
  def oliveArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(128,128,0),()⇒{line})}
  def greenArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,128,0),()⇒{line})}
  def purpleArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(128,0,128),()⇒{line})}
  def tealArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,128,128),()⇒{line})}
  def navyArray(line: ⇒Array[Double]):Unit = {datas :+= (new Color(0,0,128),()⇒{line})}
  def randColorArray(line: ⇒Array[Double]):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{line})}
  def black(line: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{Array(line)})}
  def white(line: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{Array(line)})}
  def red(line: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{Array(line)})}
  def lime(line: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{Array(line)})}
  def blue(line: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{Array(line)})}
  def yellow(line: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{Array(line)})}
  def cyan(line: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{Array(line)})}
  def magenta(line: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{Array(line)})}
  def silver(line: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{Array(line)})}
  def gray(line: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{Array(line)})}
  def maroon(line: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{Array(line)})}
  def olive(line: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{Array(line)})}
  def green(line: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{Array(line)})}
  def purple(line: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{Array(line)})}
  def teal(line: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{Array(line)})}
  def navy(line: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{Array(line)})}
  def randColor(line: ⇒Double):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{Array(line)})}
  //Helpers
  private val helper = new ToolHelper(this, name, "Histogram")
  //UI
  private val histogram = new YHistogram(environment.params.Histogram, screenW, screenH)
  private val minMaxAvg = new MinMaxAvgPane(environment.params.Histogram)
  private val frame = new BorderFrame(environment, helper.toolName, south = Some(minMaxAvg), center = Some(histogram)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Prepare plot
      val bars = datas.flatMap{case ((c,d)) ⇒ d().map(_ ⇒ c)}

      histogram.setBars(bars, minRange, maxRange, autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
//      val xys = lines.map{case ((_,line,_)) ⇒ {val (xs,ys) = line(); (xs.toList,ys.toList)}}
//      plot.update(xys)
//      minMaxAvg.update(xys.map(_._2).flatMap(e ⇒ e))
    }
    def stop() = {
      frame.hide()}}}