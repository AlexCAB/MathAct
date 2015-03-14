package mathact.tools.doers
import java.awt.event.{ActionListener,ActionEvent}
import javax.swing.Timer
import mathact.utils.Environment
import mathact.utils.clockwork.Gear
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{ExecuteButtons, HorizontalSlider, FlowFrame}


/**
 * Interactive doer (executor)
 * Created by CAB on 11.03.2015.
 */

abstract class Doer(
  name:String = "",
  speedMin:Double = .1,
  speedMax:Double = 100,
  speedInit:Double = 1,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment){
  //Variables
  private var procs:List[()⇒Unit] = List[()⇒Unit]()
  //DSL Methods
  def make(proc: ⇒Unit) = {procs +:= (()⇒proc)}
  //Helpers
  private val thisDoer = this
  private val doerName = environment.params.titleFor(name, thisDoer, "Doer")
  //Check parameters
  if(speedMax > 1000 || speedMin <= 0 || speedInit > speedMax || speedInit < speedMin){throw new SyntaxException(s"""
    |Incorrect parameters for Doer with name $doerName:
    | speedMin($speedMin) <= speedInit($speedInit) <= speedMax($speedMax)
    | and speedMax($speedMax) <= 1000 and speedMin($speedMin) > 0 is false
    |""".stripMargin)}
  //UI
  private val slider = new HorizontalSlider(
      speedMin, speedMax, speedInit,
      environment.params.Doer.sliderWidth, environment.params.Doer.sliderHeight, 10){
    def valueChanged(v:Double) = {
      frame.setTitleAdd(s" - $v/second")
      timer.setDelay((1000 / v).toInt)
      timer.restart()}}
  private val execBtn = new ExecuteButtons(environment.params.Doer){
    def start() = {
      timer.start()}
    def stop() = {
      timer.stop()}
    def step() = {
      procs.foreach(p ⇒ p())
      gear.changed()}}
  private val frame:FlowFrame = new FlowFrame(environment, doerName, List(slider, execBtn)){
    def closing() = gear.endWork()}
  frame.setTitleAdd(s" - $speedInit/second")
  //Timer
  private val timer = new Timer((1000 / speedInit).toInt, new ActionListener() {
    def actionPerformed(evt:ActionEvent) = {
      procs.foreach(p ⇒ p())
      gear.changed()}})
  //Gear
  private val gear:Gear = new Gear(environment.clockwork, environment.params.Doer.updatePriority){
    def start() = {
      //Show
      frame.show(screenX, screenY)}
    def update() = {}
    def stop() = {
      timer.stop()
      frame.hide()}}}
