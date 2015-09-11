package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.ui.UIParams
import scala.swing._
import scala.swing.event.ButtonClicked


/**
 * Start, Stop, Step buttons panel
 * Created by CAB on 11.03.2015.
 */

abstract class ExecuteButtons(uiParams:UIParams.Executor) extends GridPanel(1,3) with UIComponent{
  //Constructions
  preferredSize = new Dimension(
    uiParams.executorButtonsSize * 3,
    uiParams.executorButtonsSize)
  //Buttons
  private val startBtn:Button = new Button{
    icon = uiParams.startEnabledIcon
    disabledIcon = uiParams.startDisableIcon
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      stopBtn.enabled = true
      stepBtn.enabled = false
      start()}}}
  private val stopBtn:Button = new Button{
    icon = uiParams.stopEnabledIcon
    disabledIcon = uiParams.stopDisableIcon
    enabled = false
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      startBtn.enabled = true
      stepBtn.enabled = true
      stop()}}}
  private val stepBtn:Button = new Button{
    icon = uiParams.stepEnabledIcon
    disabledIcon = uiParams.stepDisableIcon
    reactions += {case ButtonClicked(_) ⇒{
      step()}}}
  private val buttons = List(startBtn, stopBtn, stepBtn)
  contents ++= buttons
  //Methods
  def setStarted(isStarted:Boolean):Unit = {
    startBtn.enabled = ! isStarted
    stopBtn.enabled = isStarted}
  def setEnable(isEnable:Boolean):Unit = isEnable match{
    case false ⇒ buttons.foreach(_.enabled = false)
    case true ⇒ {
      startBtn.enabled = true
      stopBtn.enabled = false
      stepBtn.enabled = true}}
  //Abstract methods
  def start()
  def stop()
  def step()}

