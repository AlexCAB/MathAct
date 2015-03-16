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
  def black(line: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{line},None)}
  def white(line: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{line},None)}
  def red(line: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{line},None)}
  def lime(line: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{line},None)}
  def blue(line: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{line},None)}
  def yellow(line: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{line},None)}
  def cyan(line: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{line},None)}
  def magenta(line: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{line},None)}
  def silver(line: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{line},None)}
  def gray(line: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{line},None)}
  def maroon(line: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{line},None)}
  def olive(line: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{line},None)}
  def green(line: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{line},None)}
  def purple(line: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{line},None)}
  def teal(line: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{line},None)}
  def navy(line: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{line},None)}
  def randColor(line: ⇒Double):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    datas :+= (color,()⇒{line},None)}
  protected implicit class SecondOperator(name:String){
    def black(line: ⇒Double):Unit = {datas :+= (new Color(0,0,0),()⇒{line},Some(name))}
    def white(line: ⇒Double):Unit = {datas :+= (new Color(255,255,255),()⇒{line},Some(name))}
    def red(line: ⇒Double):Unit = {datas :+= (new Color(255,0,0),()⇒{line},Some(name))}
    def lime(line: ⇒Double):Unit = {datas :+= (new Color(0,255,0),()⇒{line},Some(name))}
    def blue(line: ⇒Double):Unit = {datas :+= (new Color(0,0,255),()⇒{line},Some(name))}
    def yellow(line: ⇒Double):Unit = {datas :+= (new Color(255,255,0),()⇒{line},Some(name))}
    def cyan(line: ⇒Double):Unit = {datas :+= (new Color(0,255,255),()⇒{line},Some(name))}
    def magenta(line: ⇒Double):Unit = {datas :+= (new Color(255,0,255),()⇒{line},Some(name))}
    def silver(line: ⇒Double):Unit = {datas :+= (new Color(192,192,192),()⇒{line},Some(name))}
    def gray(line: ⇒Double):Unit = {datas :+= (new Color(128,128,128),()⇒{line},Some(name))}
    def maroon(line: ⇒Double):Unit = {datas :+= (new Color(128,0,0),()⇒{line},Some(name))}
    def olive(line: ⇒Double):Unit = {datas :+= (new Color(128,128,0),()⇒{line},Some(name))}
    def green(line: ⇒Double):Unit = {datas :+= (new Color(0,128,0),()⇒{line},Some(name))}
    def purple(line: ⇒Double):Unit = {datas :+= (new Color(128,0,128),()⇒{line},Some(name))}
    def teal(line: ⇒Double):Unit = {datas :+= (new Color(0,128,128),()⇒{line},Some(name))}
    def navy(line: ⇒Double):Unit = {datas :+= (new Color(0,0,128),()⇒{line},Some(name))}
    def randColor(line: ⇒Double):Unit = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      datas :+= (color,()⇒{line},Some(name))}}
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
