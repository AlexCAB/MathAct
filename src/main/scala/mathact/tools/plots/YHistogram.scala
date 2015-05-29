package mathact.tools.plots
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.Colors
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
extends Tool  with Colors{
  //Variables
  private var datas = List[Data]()
  //Classes
  protected case class Data(color:Color, bar:()⇒Array[Double]){
    def of(value: ⇒Double):Data = {
      val data = Data(color, ()⇒Array(value))
      datas :+= data
      data}
    def ofArray(value: ⇒Array[Double]):Data = {
      val data = Data(color, ()⇒value)
      datas :+= data
      data}}
  //DSL Methods
  def data(color:Color = new Color(0,0,0)):Data = Data(color, () ⇒ Array(0.0))
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
      val bars = datas.flatMap{case Data(c, d) ⇒ d().map(v ⇒ (c, v))}
      histogram.updateY(bars)
      minMaxAvg.update(bars.map(_._2))
      updated()}
    def stop() = {
      frame.hide()}}}
