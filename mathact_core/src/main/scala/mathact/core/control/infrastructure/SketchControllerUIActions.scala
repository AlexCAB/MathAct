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

package mathact.core.control.infrastructure

import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M

/** SketchController UI actions processing
  * Created by CAB on 07.09.2016.
  */

private [mathact] trait SketchControllerUIActions { _: SketchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Methods
  /** Hit run button */
  def hitRunBtn() = {
    log.debug(s"[SketchControllerUIActions.hitRunBtn] Try to run plumbing.")
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemDisabled))}
  /** Show all tools UI btn hit */
  def showAllToolsUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.showAllToolsUiBtn] Send ShowAllToolUi.")
    pumping ! M.ShowAllToolUi}
  /** Hide all tools UI btn hit */
  def hideAllToolsUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.hideAllToolsUiBtn] Send HideAllToolUi.")
    pumping ! M.HideAllToolUi}
  /** Skip all timeout task btn hit */
  def skipAllTimeoutTaskBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.skipAllTimeoutTaskBtn] Send SkipAllTimeoutTask.")
    pumping ! M.SkipAllTimeoutTask}
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
        visualization ! M.HideVisualizationUI}}
  /** Window close btn hit, for now just send ShutdownSketchController to self */
  def closeBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.closeBtnHit] Send ShutdownSketchController to self")
    self ! M.ShutdownSketchController}}
