package mathact.tools.values
import java.awt.Color
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{Measurer, GridFrame}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.math.random


/**
 * Simple value shower.
 * Created by CAB on 16.03.2015.
 */

class ValuesBoard(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
  extends Tool{
  //Variables
  private var procs = List[(Color, ()⇒Double, Option[String])]()
  private var measurers:List[(Measurer, ()⇒Double)] = List()
  //DSL Methods
  def black(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,0),()⇒{proc},None)}
  def white(proc: ⇒Double):Unit = {procs :+= (new Color(255,255,255),()⇒{proc},None)}
  def red(proc: ⇒Double):Unit = {procs :+= (new Color(255,0,0),()⇒{proc},None)}
  def lime(proc: ⇒Double):Unit = {procs :+= (new Color(0,255,0),()⇒{proc},None)}
  def blue(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,255),()⇒{proc},None)}
  def yellow(proc: ⇒Double):Unit = {procs :+= (new Color(255,255,0),()⇒{proc},None)}
  def cyan(proc: ⇒Double):Unit = {procs :+= (new Color(0,255,255),()⇒{proc},None)}
  def magenta(proc: ⇒Double):Unit = {procs :+= (new Color(255,0,255),()⇒{proc},None)}
  def silver(proc: ⇒Double):Unit = {procs :+= (new Color(192,192,192),()⇒{proc},None)}
  def gray(proc: ⇒Double):Unit = {procs :+= (new Color(128,128,128),()⇒{proc},None)}
  def maroon(proc: ⇒Double):Unit = {procs :+= (new Color(128,0,0),()⇒{proc},None)}
  def olive(proc: ⇒Double):Unit = {procs :+= (new Color(128,128,0),()⇒{proc},None)}
  def green(proc: ⇒Double):Unit = {procs :+= (new Color(0,128,0),()⇒{proc},None)}
  def purple(proc: ⇒Double):Unit = {procs :+= (new Color(128,0,128),()⇒{proc},None)}
  def teal(proc: ⇒Double):Unit = {procs :+= (new Color(0,128,128),()⇒{proc},None)}
  def navy(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,128),()⇒{proc},None)}
  def randColor(proc: ⇒Double):Unit = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    procs :+= (color,()⇒{proc},None)
    proc}
  protected implicit class SecondOperator(name:String){
    def black(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,0),()⇒{proc},Some(name))}
    def white(proc: ⇒Double):Unit = {procs :+= (new Color(255,255,255),()⇒{proc},Some(name))}
    def red(proc: ⇒Double):Unit = {procs :+= (new Color(255,0,0),()⇒{proc},Some(name))}
    def lime(proc: ⇒Double):Unit = {procs :+= (new Color(0,255,0),()⇒{proc},Some(name))}
    def blue(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,255),()⇒{proc},Some(name))}
    def yellow(proc: ⇒Double):Unit = {procs :+= (new Color(255,255,0),()⇒{proc},Some(name))}
    def cyan(proc: ⇒Double):Unit = {procs :+= (new Color(0,255,255),()⇒{proc},Some(name))}
    def magenta(proc: ⇒Double):Unit = {procs :+= (new Color(255,0,255),()⇒{proc},Some(name))}
    def silver(proc: ⇒Double):Unit = {procs :+= (new Color(192,192,192),()⇒{proc},Some(name))}
    def gray(proc: ⇒Double):Unit = {procs :+= (new Color(128,128,128),()⇒{proc},Some(name))}
    def maroon(proc: ⇒Double):Unit = {procs :+= (new Color(128,0,0),()⇒{proc},Some(name))}
    def olive(proc: ⇒Double):Unit = {procs :+= (new Color(128,128,0),()⇒{proc},Some(name))}
    def green(proc: ⇒Double):Unit = {procs :+= (new Color(0,128,0),()⇒{proc},Some(name))}
    def purple(proc: ⇒Double):Unit = {procs :+= (new Color(128,0,128),()⇒{proc},Some(name))}
    def teal(proc: ⇒Double):Unit = {procs :+= (new Color(0,128,128),()⇒{proc},Some(name))}
    def navy(proc: ⇒Double):Unit = {procs :+= (new Color(0,0,128),()⇒{proc},Some(name))}
    def randColor(proc: ⇒Double):Unit = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      procs :+= (color,()⇒{proc},Some(name))
      proc}}
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "ValuesBoard")
  //UI
  private val frame = new GridFrame(environment.layout, environment.params.ValuesBoard, helper.toolName){
    def closing() = gear.endWork()}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Crate measurers
      measurers = procs.zipWithIndex
        .map{case ((c, p, Some(n)), _) ⇒ (n, c, p); case ((c, p, None), i) ⇒ ("L" + i, c, p)}
        .map{case(name, color, proc) ⇒ (new Measurer(environment.params.ValuesBoard, name, color), proc)}
      frame.add(measurers.map(_._1))
      //Show
      frame.show(screenX, screenY)}
    def update() = {
      measurers.foreach{case (m,p) ⇒ m.update(p())}
      updated()}
    def stop() = {
      frame.hide()}}}
