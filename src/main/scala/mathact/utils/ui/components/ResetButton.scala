package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.event.ButtonClicked
import scala.swing.{Button, GridPanel}


/**
 * Reset button component.
 * Created by CAB on 09.09.2015.
 */

abstract class ResetButton (uiParams:UIParams.ResetButton) extends Button with UIComponent{
  //Constructions
  preferredSize = new Dimension(
    uiParams.resetButtonSize,
    uiParams.resetButtonSize)
  icon = uiParams.resetButtonIcon
  reactions += {case ButtonClicked(_) ⇒ reset()}
  //Abstract methods
  def reset()}
