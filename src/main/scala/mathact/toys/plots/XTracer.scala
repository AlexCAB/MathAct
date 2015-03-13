package mathact.toys.plots
import java.awt.Color
import mathact.utils.Environment
import mathact.utils.clockwork.Gear
import mathact.utils.ui.components.{MinMaxAvgPane, BorderFrame}
import scala.math.random


/**
 * Trace given function in laid diapason.
 * Created by CAB on 12.03.2015.
 */

abstract class XTracer(
  val a:Double = -1,
  val b:Double = 1,
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment){
  //Variables
  private var procs = List[(Color, Double⇒Double, Option[String])]()
  //DSL Methods
  def black(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,0),proc,None); proc}
  def white(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,255),proc,None); proc}
  def red(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,0),proc,None); proc}
  def lime(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,0),proc,None); proc}
  def blue(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,255),proc,None); proc}
  def yellow(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,0),proc,None); proc}
  def cyan(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,255),proc,None); proc}
  def magenta(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,255),proc,None); proc}
  def silver(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(192,192,192),proc,None); proc}
  def gray(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,128),proc,None); proc}
  def maroon(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,0),proc,None); proc}
  def olive(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,0),proc,None); proc}
  def green(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,0),proc,None); proc}
  def purple(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,128),proc,None); proc}
  def teal(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,128),proc,None); proc}
  def navy(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,128),proc,None); proc}
  def randColor(proc:Double⇒Double):Double⇒Double = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    procs :+= (color,proc,None)
    proc}
  protected implicit class SecondOperator(name:String){
    def black(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,0),proc,Some(name)); proc}
    def white(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,255),proc,Some(name)); proc}
    def red(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,0),proc,Some(name)); proc}
    def lime(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,0),proc,Some(name)); proc}
    def blue(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,255),proc,Some(name)); proc}
    def yellow(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,0),proc,Some(name)); proc}
    def cyan(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,255),proc,Some(name)); proc}
    def magenta(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,255),proc,Some(name)); proc}
    def silver(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(192,192,192),proc,Some(name)); proc}
    def gray(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,128),proc,Some(name)); proc}
    def maroon(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,0),proc,Some(name)); proc}
    def olive(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,0),proc,Some(name)); proc}
    def green(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,0),proc,Some(name)); proc}
    def purple(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,128),proc,Some(name)); proc}
    def teal(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,128),proc,Some(name)); proc}
    def navy(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,128),proc,Some(name)); proc}
    def randColor(proc:Double⇒Double):Double⇒Double = {
      val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
      procs :+= (color,proc,Some(name))
      proc}}
  //Helpers
  private val thisXTracer = this
  private val xTracerName = environment.skin.titleFor(name, thisXTracer, "XTracer")
  //UI
//  private val plot =




  private val minMaxAvg = new MinMaxAvgPane(environment.skin.XTracer)
  private val frame = new BorderFrame(environment, xTracerName, south = Some(minMaxAvg)){
    def closing() = {gear.endWork()}}





  //Gear
  private val gear:Gear = new Gear(environment.clockwork){
    def start() = {
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {}
    def stop() = {
      frame.hide()}}







}




































