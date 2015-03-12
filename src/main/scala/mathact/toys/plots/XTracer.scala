package mathact.toys.plots
import java.awt.Color
import mathact.utils.Environment
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.BorderFrame
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
  screenW:Int = Int.MaxValue,
  screenH:Int = Int.MaxValue)
(implicit environment:Environment){
  //Variables
  private var procs = List[(Color, Double⇒Double)]()
  //DSL Methods
  def black(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,0),proc); proc}
  def white(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,255),proc); proc}
  def red(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,0),proc); proc}
  def lime(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,0),proc); proc}
  def blue(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,255),proc); proc}
  def yellow(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,255,0),proc); proc}
  def cyan(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,255,255),proc); proc}
  def magenta(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(255,0,255),proc); proc}
  def silver(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(192,192,192),proc); proc}
  def gray(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,128),proc); proc}
  def maroon(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,0),proc); proc}
  def olive(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,128,0),proc); proc}
  def green(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,0),proc); proc}
  def purple(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(128,0,128),proc); proc}
  def teal(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,128,128),proc); proc}
  def navy(proc:Double⇒Double):Double⇒Double = {procs :+= (new Color(0,0,128),proc); proc}
  def randColor(proc:Double⇒Double):Double⇒Double = {
    val color = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)
    procs :+= (color,proc)
    proc}
  //Helpers
  private val thisXTracer = this
  private val xTracerName = environment.skin.titleFor(name, thisXTracer, "XTracer")
  //UI
//  private val plot =
//  private val minMaxAvg =
//
//
//
//  private val farme = new BorderFrame()









}




































