package mathact.utils.dsl

import java.awt.Color

import scala.math._

/**
 * Named colors trait.
 * Created by CAB on 29.05.2015.
 */

trait Colors {
  lazy val black = new Color(0,0,0)
  lazy val white = new Color(255,255,255)
  lazy val red = new Color(255,0,0)
  lazy val lime = new Color(0,255,0)
  lazy val blue = new Color(0,0,255)
  lazy val yellow = new Color(255,255,0)
  lazy val cyan = new Color(0,255,255)
  lazy val magenta = new Color(255,0,255)
  lazy val silver = new Color(192,192,192)
  lazy val gray = new Color(128,128,128)
  lazy val maroon = new Color(128,0,0)
  lazy val olive = new Color(128,128,0)
  lazy val green = new Color(0,128,0)
  lazy val purple = new Color(128,0,128)
  lazy val teal = new Color(0,128,128)
  lazy val navy = new Color(0,0,128)
  def randColor = new Color((random * 255).toInt, (random * 255).toInt, (random * 255).toInt)}
