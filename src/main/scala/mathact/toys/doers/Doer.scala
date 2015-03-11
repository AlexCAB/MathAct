package mathact.toys.doers
import mathact.utils.Environment
import mathact.utils.clockwork.Gear
import mathact.utils.ui.components.{Potentiometer, ExecuteButtons, HorizontalSlider, FixedFlowFrame}

import scala.swing.Component


/**
 * Interactive doer (executor)
 * Created by CAB on 11.03.2015.
 */

class Doer(
  proc: ⇒Unit,
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  speedMin:Double = .2,
  speedMax:Double = 100,
  speedInit:Double = .5)
(implicit environment:Environment){
  //With out parameters constructor
  def this(p: ⇒Unit)(implicit env:Environment) = {
    this(proc = p)(env)}
  //Helpers
  private val thisDoer = this
  //UI
  private val slider = new HorizontalSlider(
    environment, speedMin, speedMax, speedInit, environment.skin.doerSliderWidth){
    def valueChanged(v:Double) = {

    }
  }

  private val execBtn = new ExecuteButtons(environment){
    def start() = {


    }
    def stop() = {


    }
    def step() = {


    }

  }



  private val frame = new FixedFlowFrame(
    environment,
    environment.skin.titleFor(name, thisDoer, "Doer"),
    List(slider, execBtn))
  {def closing() = {if(gear.work){environment.clockwork.delGear(gear)}}}






  //Gear
  private val gear:Gear = new Gear(environment.clockwork){
    def start() = {
      //Show
      frame.show(screenX, screenY)}
    def update() = {}
    def stop() = {frame.hide()}}
  environment.clockwork.addGear(gear)}