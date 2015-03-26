package mathact.tools.doers
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import mathact.utils.clockwork.CalculationGear
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{SelectionBar, FlowFrame, ExecuteButtons, HorizontalSlider}
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Execute one "step{}" per one time tick
 * Created by CAB on 26.03.2015.
 */

abstract class Stepper(
  name:String = "",
  speedMin:Double = .1,
  speedMax:Double = 100,
  speedInit:Double = 1,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
  (implicit environment:Environment)
extends Tool{
  //Variables
  private var procs = List[(String, ()⇒Unit)]()
  private var nextStepIndex = 0
  //DSL Methods
  def step(proc: ⇒Unit) = {
    val name = "S_" + procs.size
    procs :+= (name, ()⇒proc)}
  protected implicit class SecondOperator(name:String){
    def step(proc: ⇒Unit) = {procs :+= (name, ()⇒proc)}}
  //Helpers
  private val helper = new ToolHelper(this, name, "Stepper")
  //Check parameters
  if(speedMax > 1000 || speedMin <= 0 || speedInit > speedMax || speedInit < speedMin){throw new SyntaxException(s"""
    |Incorrect parameters for Stepper with name ${helper.toolName}:
    | speedMin($speedMin) <= speedInit($speedInit) <= speedMax($speedMax)
    | and speedMax($speedMax) <= 1000 and speedMin($speedMin) > 0
    | n of steps == 0 is false
    |""".stripMargin)}
  //Functions
  private def nextStep() = {
    procs(nextStepIndex)._2()
    nextStepIndex = if(nextStepIndex < procs.size - 1) nextStepIndex + 1 else 0
    dropList.setItem(nextStepIndex)}
  //UI
  private val dropList = new SelectionBar(environment.params.Stepper, "Next step: ", environment.params.Stepper.listInitWidth){
    def selected(item:String, index:Int) = {nextStepIndex = index}}
  private val slider = new HorizontalSlider(environment.params.Stepper, speedMin, speedMax, speedInit){
    def valueChanged(v:Double) = {
      frame.setTitleAdd(s" - $v/second")
      timer.setDelay((1000 / v).toInt)}}
  private val execBtn = new ExecuteButtons(environment.params.Stepper){
    def start() = {
      timer.start()}
    def stop() = {
      timer.stop()}
    def step() = {
      nextStep()
      gear.changed()}}
  private val frame:FlowFrame = new FlowFrame(
    environment.layout, environment.params.Stepper, helper.toolName, List(dropList, slider, execBtn)){
    def closing() = gear.endWork()}
  frame.setTitleAdd(s" - $speedInit/second")
  //Timer
  private val timer = new Timer((1000 / speedInit).toInt, new ActionListener() {
    def actionPerformed(evt:ActionEvent) = {
      nextStep()
      gear.changed()}})
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = -1){
    def start() = {
      if(procs.size == 0){ throw new SyntaxException(s"""
        |Incorrect definition of Stepper with name ${helper.toolName}: Have 0 steps""")}
      dropList.setList(procs.map(_._1))
      dropList.setItem(nextStepIndex)
      frame.show(screenX, screenY)}
    def update() = {}
    def stop() = {
      timer.stop()
      frame.hide()}}}