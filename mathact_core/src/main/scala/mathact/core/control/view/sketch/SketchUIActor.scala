/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.core.control.view.sketch

import akka.actor.{ActorRef, PoisonPill}
import mathact.core.ActorBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M


/** The sketch (workbench) window
  * Created by CAB on 23.05.2016.
  */

private [mathact] class SketchUIActor(
  config: SketchUIConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction { import SketchUIElement._, SketchUiElemState._
  //Construction
  private val window = runNow{
    val stg = new SketchUIViewAndController(config, workbenchController, log)
    stg.resizable = false
    stg.sizeToScene()
    stg}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowSketchUI ⇒
      runAndWait(window.show())
      workbenchController ! M.SketchUIChanged(isShow = true)
    //Update UI state
    case M.UpdateSketchUIState(newState) ⇒ newState.foreach{
      case (LogBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.logBtn.setState(s)}
      case (VisualisationBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.visualisationBtn.setState(s)}
      case (RunBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.runBtn.setState(s)}
      case (ShowAllToolsUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.showAllToolsUiBtn.setState(s)}
      case (HideAllToolsUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.hideAllToolsUiBtn.setState(s)}
      case (SkipAllTimeoutTaskBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.skipAllTimeoutTaskBtn.setState(s)}
      case (StopSketchBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒runAndWait{
        window.stopSketchBtn.setState(s)}
      case (element, state) ⇒
        log.error(s"[SketchUI] Unknown combination of element: $element and state: $state")}
    //Update status string
    case M.SetSketchUIStatusString(message, color) ⇒ runAndWait{
      window.stateString.text = message
      window.stateString.fill = color}
    //Hide UI
    case M.HideSketchUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.SketchUIChanged(isShow = false)
    //Terminate UI
    case M.TerminateSketchUI ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}}

