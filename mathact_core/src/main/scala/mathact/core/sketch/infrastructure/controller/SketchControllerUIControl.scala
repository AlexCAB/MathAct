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

package mathact.core.sketch.infrastructure.controller

import mathact.core.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.core.model.messages.M


/** SketchControllerActor UI control
  * Created by CAB on 04.09.2016.
  */

private[core] trait SketchControllerUIControl { _: SketchControllerActor ⇒
  import SketchUIElement._
  import SketchUiElemState._
  //Variables
  private var isSketchUiShowed = false
  private var isUserLogShowed = false
  private var isVisualisationShowed = false
  //Methods
  /** Show all UI, and log to user logging */
  def showAllSketchUi(): Unit = {
    log.debug(
      s"[SketchControllerUIControl.showAllSketchUi] Try to show, " +
      s"showUserLogUiAtStart: ${sketchData.showUserLogUiAtStart}, " +
      s"showVisualisationUiAtStart: ${sketchData.showVisualisationUiAtStart}")
    //Sketch UI
    sketchUi ! M.ShowSketchUI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      LayoutFillBtn → ElemDisabled,
      LayoutStairsBtn → ElemDisabled,
      ShowAllBlocksUiBtn → ElemDisabled,
      HideAllBlocksUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled,
      LogBtn → (if(sketchData.showUserLogUiAtStart) ElemHide else ElemShow),
      VisualisationBtn → (if(sketchData.showVisualisationUiAtStart) ElemHide else ElemShow)))
    //User logging
    sketchData.showUserLogUiAtStart match{
      case true ⇒ userLogging ! M.ShowUserLoggingUI
      case false ⇒ log.debug("[SketchControllerUIControl.showAllSketchUi] User Logging UI stay hided.")}
    //Visualisation
    sketchData.showVisualisationUiAtStart match{
      case true ⇒ visualization ! M.ShowVisualizationUI
      case false ⇒ log.debug("[SketchControllerUIControl.showAllSketchUi] Visualization UI stay hided.")}}
  /** Check if all UI shown
    * @return - true if all shown */
  def isAllUiShowed: Boolean = {
    val res = isSketchUiShowed &&
      (isUserLogShowed || (! sketchData.showUserLogUiAtStart)) &&
      (isVisualisationShowed || (! sketchData.showVisualisationUiAtStart))
    log.debug(s"[SketchControllerUIControl.isAllUiShowed] res: $res.")
    res}
  /** Sketch UI changed
    * @param isShow - true if shown */
  def sketchUiChanged(isShow: Boolean): Unit = {
    log.debug(s"[SketchControllerUIControl.sketchUiChanged] isShow: $isShow")
    isSketchUiShowed = isShow }
  /** User logging UI changed
    * @param isShow - true if shown */
  def userLoggingUIChanged(isShow: Boolean): Unit = {
    log.debug(s"[SketchControllerUIControl.userLoggingUIChanged] isShow: $isShow")
    isUserLogShowed = isShow
    sketchUi ! M.UpdateSketchUIState(Map(LogBtn → (if(isShow) ElemHide else ElemShow)))}
  /** Visualization UI changed
    * @param isShow - true if shown */
  def visualizationUIChanged(isShow: Boolean): Unit = {
    log.debug(s"[SketchControllerUIControl.visualizationUIChanged] isShow: $isShow")
    isVisualisationShowed = isShow
    sketchUi ! M.UpdateSketchUIState(Map(VisualisationBtn → (if(isShow) ElemHide else ElemShow)))}}
