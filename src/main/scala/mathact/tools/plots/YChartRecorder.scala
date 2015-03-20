package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{BorderFrame,Chart,VariablesBar}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.math.random


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
extends Tool{
  //Variables
  private var datas = List[(Color, ()⇒Double, Option[String])]()
  private var xCounter = .0
  //DSL Methods
  def black(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{trace},None)}
  def white(trace: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{trace},None)}
  def red(trace: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{trace},None)}
  def lime(trace: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{trace},None)}
  def blue(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{trace},None)}
  def yellow(trace: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{trace},None)}
  def cyan(trace: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{trace},None)}
  def magenta(trace: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{trace},None)}
  def silver(trace: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{trace},None)}
  def gray(trace: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{trace},None)}
  def maroon(trace: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{trace},None)}
  def olive(trace: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{trace},None)}
  def green(trace: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{trace},None)}
  def purple(trace: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{trace},None)}
  def teal(trace: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{trace},None)}
  def navy(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{trace},None)}
  def randColor(trace: ⇒Double):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{trace},None)}
  protected implicit class SecondOperator(name:String){
    def black(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{trace},Some(name))}
    def white(trace: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{trace},Some(name))}
    def red(trace: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{trace},Some(name))}
    def lime(trace: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{trace},Some(name))}
    def blue(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{trace},Some(name))}
    def yellow(trace: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{trace},Some(name))}
    def cyan(trace: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{trace},Some(name))}
    def magenta(trace: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{trace},Some(name))}
    def silver(trace: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{trace},Some(name))}
    def gray(trace: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{trace},Some(name))}
    def maroon(trace: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{trace},Some(name))}
    def olive(trace: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{trace},Some(name))}
    def green(trace: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{trace},Some(name))}
    def purple(trace: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{trace},Some(name))}
    def teal(trace: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{trace},Some(name))}
    def navy(trace: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{trace},Some(name))}
    def randColor(trace: ⇒Double):Unit = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      datas :+= (color,()⇒{trace},Some(name))}}
  def update() = {
    val xys = datas.map{case ((_,data,_)) ⇒ Some(xCounter, data())}
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
      chart.setTraces(datas.map{case (c,_,_) ⇒ c}, minRange, maxRange)
      varBar.setVars(datas.zipWithIndex.map{
        case ((c,_,Some(n)),_) ⇒ (n,c)
        case ((c,_,None),i) ⇒ ("T" + i,c)})
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = if(autoUpdate){
      helper.thisTool.update()}
    def stop() = {
      frame.hide()}}}
