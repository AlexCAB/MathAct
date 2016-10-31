package mathact.core.bricks.ui

/** Set predefined of UI events
  * Created by CAB on 31.10.2016.
  */

object E {
  //Frame events
  case object ShowFrame extends UIEvent
  case object HideFrame extends UIEvent
  case class SetFrameTitle(text: String) extends UIEvent}
