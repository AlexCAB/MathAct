package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.ui.{UIParams, ToyComponent}
import scala.swing._
import scala.swing.event.ButtonClicked


/**
 * Start, Stop, Step buttons panel
 * Created by CAB on 11.03.2015.
 */

abstract class ExecuteButtons(uiParams:UIParams.Executor) extends GridPanel(1,3) with ToyComponent{
  //Constructions
  val initHeight = uiParams.executorButtonsSize
  val initWidth = initHeight * 3
  preferredSize = new Dimension(initWidth, initHeight)
  //Buttons
  val startBtn:Button = new Button{
    icon = uiParams.startEnabledIcon
    disabledIcon = uiParams.startDisableIcon
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      stopBtn.enabled = true
      stepBtn.enabled = false
      start()}}}
  val stopBtn:Button = new Button{
    icon = uiParams.stopEnabledIcon
    disabledIcon = uiParams.stopDisableIcon
    enabled = false
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      startBtn.enabled = true
      stepBtn.enabled = true
      stop()}}}
  val stepBtn:Button = new Button{
    icon = uiParams.stepEnabledIcon
    disabledIcon = uiParams.stepDisableIcon
    reactions += {case ButtonClicked(_) ⇒{
      step()}}}
  contents ++= List(startBtn, stopBtn, stepBtn)
  //Abstract methods
  def start()
  def stop()
  def step()
  //Methods
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}

