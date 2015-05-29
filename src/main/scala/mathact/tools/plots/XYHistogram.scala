package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{HistPlot, BorderFrame, MinMaxAvgPane}
import mathact.utils.{ToolHelper, Tool, Environment}
import mathact.utils.dsl.Colors


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
extends Tool with Colors{
  //Variables
  private var datas = List[Data]()
  //Classes
  protected case class Data(name:Option[String], color:Color, bar:()⇒(Array[Double],Array[Double])){
    def of(value: ⇒(Double,Double)):Data = {
      val data = Data(name, color, ()⇒(Array(value._1), Array(value._2)))
      datas :+= data
      data}
    def ofArray(value: ⇒(Array[Double],Array[Double])):Data = {
      val data = Data(name, color, ()⇒(value._1, value._2))
      datas :+= data
      data}}
  //DSL Methods
  def data(name:String = "", color:Color = new Color(0,0,0)):Data =
    Data(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, color, () ⇒ (Array(0.0), Array(0.0)))
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
      val bars = datas.flatMap{case Data(_, color, proc) ⇒ {
        val (xs,ys) = proc()
        xs.zip(ys).map{case d ⇒ (color, d)}}}
      histogram.updateXY(bars)
      minMaxAvg.update(bars.map{case ((_,(_,y))) ⇒ y})
      updated()}
    def stop() = {
      frame.hide()}}}
