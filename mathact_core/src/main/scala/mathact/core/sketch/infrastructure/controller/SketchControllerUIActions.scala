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

import mathact.core.model.enums.SketchUiElemState
import mathact.core.model.messages.M

/** SketchControllerActor UI actions processing
  * Created by CAB on 07.09.2016.
  */

private [mathact] trait SketchControllerUIActions { _: SketchControllerActor ⇒
  //Methods
  /** Show all blocks UI btn hit */
  def showAllBlocksUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.showAllBlocksUiBtn] Send ShowAllBlockUi.")
    plumbing ! M.ShowAllBlockUi}
  /** Hide all blocks UI btn hit */
  def hideAllBlocksUiBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.hideAllBlocksUiBtn] Send HideAllBlockUi.")
    plumbing ! M.HideAllBlockUi}
  /** Skip all timeout task btn hit */
  def skipAllTimeoutTaskBtnHit(): Unit = {
    log.debug(s"[SketchControllerUIActions.skipAllTimeoutTaskBtn] Send SkipAllTimeoutTask.")
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
