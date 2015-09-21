package mathact.tools.doers
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import mathact.utils.clockwork.CalculationGear
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{SelectionBar, FlowFrame, ExecuteButtons, HorizontalSlider}
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Execute one step per one time tick
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
  private var onStart = List[Step]()
  private var onStop = List[Step]()
  private var steps = List[Step]()
  private var nextStepIndex = 0
  //Functions
  private def doStart():Unit = {
    onStart.foreach(_.proc())
    execBtn.setStarted(true)
    timer.start()}
  private def doStop():Unit = {
    timer.stop()
    execBtn.setStarted(false)
    onStop.foreach(_.proc())}
  private def doStep():Unit = {
    nextStep()
    gear.changed()}
  //DSL Methods
  protected case class Step(name:String, proc:()⇒Unit){
    def make(proc: ⇒Unit):Step = {
      val step = Step(name, ()⇒proc)
      steps :+= step
      step}}
  protected def onStart(proc: ⇒Unit):Unit = {onStart :+= Step("", ()⇒proc)}
  protected def onStop(proc: ⇒Unit):Unit = {onStop :+= Step("", ()⇒proc)}
  protected def step(name:String = ""):Step =
    Step(name match{case s if s == "" ⇒ "S_" + steps.size; case s ⇒ s}, ()⇒{})
  //Methods
  /**
   * Start stepper
   */
  def start():Unit = doStart()
  /**
   * Stop stepper
   */
  def stop():Unit = doStop()
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
    steps(nextStepIndex).proc()
    nextStepIndex = if(nextStepIndex < steps.size - 1) nextStepIndex + 1 else 0
    dropList.setItem(nextStepIndex)}
  //UI
  private val dropList = new SelectionBar(environment.params.Stepper, "Next step: ", environment.params.Stepper.listInitWidth){
    def selected(item:String, index:Int) = {nextStepIndex = index}}
  private val slider = new HorizontalSlider(environment.params.Stepper, speedMin, speedMax, speedInit){
    def valueChanged(v:Double) = {
      frame.setTitleAdd(s" - $v/second")
      timer.setDelay((1000 / v).toInt)}}
  private val execBtn = new ExecuteButtons(environment.params.Stepper){
    def start() = doStart()
    def stop() = doStop()
    def step() = doStep()}
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
      if(steps.size == 0){ throw new SyntaxException(s"""
        |Incorrect definition of Stepper with name ${helper.toolName}: Have 0 steps""")}
      dropList.setList(steps.map(_.name))
      dropList.setItem(nextStepIndex)
      frame.show(screenX, screenY)}
    def update() = {}
    def stop() = {
      timer.stop()
      frame.hide()}}}
