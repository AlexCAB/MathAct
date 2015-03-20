package mathact.tools.doers
import java.awt.Dimension
import mathact.utils.clockwork.CalculationGear
import mathact.utils.ui.components.BorderFrame
import mathact.utils.ui.components.{NumberSpinner, AutoManButton, HorizontalSlider}
import mathact.utils.{Environment, Tool, ToolHelper}


/**
 * Calc with auto/manual switch
 * Created by CAB on 16.03.2015.
 */

abstract class CalcWithManual(
  name:String = "",
  min:Double = -1,
  max:Double = 1,
  value:Double = 0,
  initIsAuto:Boolean = false,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool{
  //Variables
  private var authProcs:List[Double⇒Unit] = List()
  private var manProcs:List[Double⇒Unit] = List()
  private var currentIsAuth = initIsAuto
  private var currentValue = value
  //Helpers
  private val helper = new ToolHelper(this, name, "CalcWithManual")
  //DSL Methods
  def auto(proc:Double⇒Unit) = {authProcs +:= proc}
  def manual(proc:Double⇒Unit) = {manProcs +:= proc}
  //UI
  private val slider = new HorizontalSlider(environment.params.AuthManPot, min, max, value){
    def valueChanged(v:Double) = {
      spinner.setCurrentValue(v)
      currentValue = v}}
  private val spinner:NumberSpinner = new NumberSpinner(environment.params.AuthManPot, min, max, value){
    def valueChanged(v:Double) = {
      slider.setCurrentValue(v)
      currentValue = v}}
  private val authManBtn = new AutoManButton(environment.params.AuthManPot, initIsAuto){
    def stateChanged(isAuth:Boolean) = {
      currentIsAuth = isAuth}}
  private val frame:BorderFrame = new BorderFrame(
      environment.layout, environment.params.AuthManPot, helper.toolName,
      west = Some(slider), center = Some(spinner),east = Some(authManBtn)){
    def closing() = gear.endWork()}
  spinner.preferredSize = new Dimension(150,spinner.preferredSize.getHeight.toInt)
  frame.setTitleAdd(s" - [$min,$max]")
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = 1){
    def start() = {
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = currentIsAuth match{
      case true ⇒ {authProcs.foreach(p ⇒ p(currentValue))}
      case false ⇒ manProcs.foreach(p ⇒ p(currentValue))}
    def stop() = {
      frame.hide()}}}
