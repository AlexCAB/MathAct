package mathact.tools.loggers
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{TextLinePane, BorderFrame}
import mathact.utils.{ToolHelper, Environment, Tool}
import scala.math.random


/**
 * Simple logger
 * Created by CAB on 14.03.2015.
 */

class Logger(
  name:String = "",
  maxSize:Int = 1000,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool{
  //Functions
  private def add(msg:String, color:Color) = {
    textPane.addLine(msg, environment.params.Logger.defaultColor)
    updated()}
  //DSL Methods
  def log(msg:String):Unit = {add(msg, environment.params.Logger.defaultColor)}
  def black(msg:String):Unit = {add(msg, new Color(0,0,0))}
  def white(msg:String):Unit = {add(msg, new Color(255,255,255))}
  def red(msg:String):Unit = {add(msg, new Color(255,0,0))}
  def lime(msg:String):Unit = {add(msg, new Color(0,255,0))}
  def blue(msg:String):Unit = {add(msg, new Color(0,0,255))}
  def yellow(msg:String):Unit = {add(msg, new Color(255,255,0))}
  def cyan(msg:String):Unit = {add(msg, new Color(0,255,255))}
  def magenta(msg:String):Unit = {add(msg, new Color(255,0,255))}
  def silver(msg:String):Unit = {add(msg, new Color(192,192,192))}
  def gray(msg:String):Unit = {add(msg, new Color(128,128,128))}
  def maroon(msg:String):Unit = {add(msg, new Color(128,0,0))}
  def olive(msg:String):Unit = {add(msg, new Color(128,128,0))}
  def green(msg:String):Unit = {add(msg, new Color(0,128,0))}
  def purple(msg:String):Unit = {add(msg, new Color(128,0,128))}
  def teal(msg:String):Unit = {add(msg, new Color(0,128,128))}
  def navy(msg:String):Unit = {add(msg, new Color(0,0,128))}
  def randColor(msg:String):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    add(msg, color)}
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "Logger")
  //UI
  private val textPane = new TextLinePane(environment.params.Logger, maxSize, screenW, screenH)
  private val frame = new BorderFrame(environment.layout, environment.params.Logger, helper.toolName, center = Some(textPane)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {}
    def stop() = {
      frame.hide()}}}
