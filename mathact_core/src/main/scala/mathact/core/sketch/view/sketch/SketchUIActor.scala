/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package mathact.core.sketch.view.sketch

import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.holders.SketchControllerRef
import mathact.core.model.messages.M


/** The sketch (workbench) window
  * Created by CAB on 23.05.2016.
  */

private[core] class SketchUIActor(
  config: SketchUIConfigLike,
  sketchController: SketchControllerRef)
extends WorkerBase with JFXInteraction { import SketchUIElement._, SketchUiElemState._
  //Construction
  private val window = runNow{
    val stg = new SketchUIViewAndController(config, sketchController.ref, log)
    stg.resizable = false
    stg.sizeToScene()
    stg}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowSketchUI ⇒
      runAndWait(window.show())
      sketchController ! M.SketchUIChanged(isShow = true)
    //Update UI state
    case M.UpdateSketchUIState(newState) ⇒ newState.foreach{
      case (LogBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.logBtn.setState(s)}
      case (VisualisationBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.visualisationBtn.setState(s)}
      case (RunBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.runBtn.setState(s)}
      case (LayoutFillBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.layoutFillBtn.setState(s)}
      case (LayoutStairsBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.layoutStairsBtn.setState(s)}
      case (ShowAllBlocksUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.showAllBlocksUiBtn.setState(s)}
      case (HideAllBlocksUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.hideAllBlocksUiBtn.setState(s)}
      case (SkipAllTimeoutTaskBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.skipAllTimeoutTaskBtn.setState(s)}
      case (StopSketchBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒runAndWait{
        window.stopSketchBtn.setState(s)}
      case (element, state) ⇒
        log.error(s"[SketchUI @ UpdateSketchUIState] Unknown combination of element: $element and state: $state")}
    //Update title
    case M.UpdateSketchUITitle(newTitle: String) ⇒ runAndWait{
      window.title = newTitle}
    //Update status string
    case M.SetSketchUIStatusString(message, color) ⇒ runAndWait{
      window.stateString.text = message
      window.stateString.fill = color}
    //Hide UI
    case M.HideSketchUI ⇒
      runAndWait(window.hide())
      sketchController ! M.SketchUIChanged(isShow = false)}
  //Cleanup
  def cleanup(): Unit = {
    log.debug(s"[SketchUIActor.cleanup] Actor stopped, close UI.")
    runLater(window.close())}}
