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

package mathact.core.sketch.infrastructure.controller

import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M

/** SketchControllerActor UI actions processing
  * Created by CAB on 07.09.2016.
  */

private[core] trait SketchControllerUIActions { _: SketchControllerActor ⇒ import SketchUIElement._, SketchUiElemState._
  //Methods
  /** Show all blocks UI btn hit */
  def showAllBlocksUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.showAllBlocksUiBtn] Update UI and send ShowAllBlockUi.")
    sketchUi ! M.UpdateSketchUIState(Map(ShowAllBlocksUiBtn → ElemEnabled))
    plumbing ! M.ShowAllBlockUi}
  /** Hide all blocks UI btn hit */
  def hideAllBlocksUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.hideAllBlocksUiBtn] Update UI and send HideAllBlockUi.")
    sketchUi ! M.UpdateSketchUIState(Map(HideAllBlocksUiBtn → ElemEnabled))
    plumbing ! M.HideAllBlockUi}
  /** Skip all timeout task btn hit */
  def skipAllTimeoutTaskBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.skipAllTimeoutTaskBtn] Update UI and send SkipAllTimeoutTask.")
    sketchUi ! M.UpdateSketchUIState(Map(SkipAllTimeoutTaskBtn → ElemEnabled))
    plumbing ! M.SkipAllTimeoutTask}
  /** Change user logging state
    * @param act - SketchUiElemState */
  def logBtnHit(act: SketchUiElemState): Unit = {
    log.debug(s"[SketchControllerUIActions.logBtnHit] act: $act ")
    act match{
      case SketchUiElemState.ElemShow ⇒
        userLogging ! M.ShowUserLoggingUI
      case SketchUiElemState.ElemHide ⇒
        userLogging ! M.HideUserLoggingUI}}
  /** Change visualisation
    * @param act - SketchUiElemState */
  def visualisationBtnHit(act: SketchUiElemState): Unit = {
    log.debug(s"[SketchControllerUIActions.logBtnHit] act: $act ")
    act match{
      case SketchUiElemState.ElemShow ⇒
        visualization ! M.ShowVisualizationUI
      case SketchUiElemState.ElemHide ⇒
        visualization ! M.HideVisualizationUI}}}
