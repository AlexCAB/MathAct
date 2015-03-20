package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.event.ButtonClicked
import scala.swing.{Button, GridPanel}


/**
 * Auth/manual switch
 * Created by CAB on 16.03.2015.
 */

abstract class AutoManButton(uiParams:UIParams.AutoManButtons, initIsAuto:Boolean)
extends GridPanel(1,2) with UIComponent{
  //Construction
  val authBtn:Button = new Button{
    icon = uiParams.authEnabledIcon
    disabledIcon = uiParams.authDisableIcon
    enabled = ! initIsAuto
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      manBtn.enabled = true
      stateChanged(true)}}}
  val manBtn:Button = new Button{
    icon = uiParams.manEnabledIcon
    disabledIcon = uiParams.manDisableIcon
    enabled = initIsAuto
    reactions += {case ButtonClicked(_) ⇒{
      enabled = false
      authBtn.enabled = true
      stateChanged(false)}}}
  preferredSize = new Dimension(uiParams.authManButtonsSize * 2, uiParams.authManButtonsSize)
  contents ++= List(authBtn,manBtn)
  //Abstract methods
  def stateChanged(isAuth:Boolean)}
