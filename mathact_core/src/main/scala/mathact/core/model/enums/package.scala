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

package mathact.core.model


/** Contains common (global) enums
  * Created by CAB on 24.06.2016.
  */

package object enums {
  //SketchStatus
  object SketchStatus extends Enumeration {
    val Ready, Ended, Failed = Value}
  type SketchStatus = SketchStatus.Value
  //BlockType
  object BlockType extends Enumeration {
    val Workbench, Block = Value}
  type BlockType = BlockType.Value
  //StepMode
  object StepMode extends Enumeration {
    val HardSynchro, SoftSynchro, Asynchro, None = Value}
  type StepMode = StepMode.Value
  //WorkMode
  object WorkMode extends Enumeration {
    val Paused, Runned, Stopping = Value}
  type WorkMode = WorkMode.Value
  //TaskKind
  object TaskKind extends Enumeration {
    val Start, Massage, Stop, UiInit, UiCreate, UiShow, UiHide, UiClose, UiLayout, UiEvent = Value}
  type TaskKind = TaskKind.Value
  //VisualisationLaval
  object VisualisationLaval extends Enumeration {
    val None, Basic, Load, Full = Value}
  type VisualisationLaval = VisualisationLaval.Value
  //SketchUIElement
  object SketchUIElement extends Enumeration {
    val LogBtn = Value
    val VisualisationBtn = Value
    val RunBtn = Value
    val LayoutFillBtn = Value
    val LayoutStairsBtn = Value
    val ShowAllBlocksUiBtn = Value
    val HideAllBlocksUiBtn = Value
    val SkipAllTimeoutTaskBtn = Value
    val StopSketchBtn = Value
    val CloseBtn = Value}
  type SketchUIElement = SketchUIElement.Value
  //SketchUiElemState
  object SketchUiElemState extends Enumeration {
    val ElemDisabled = Value
    val ElemEnabled = Value
    val ElemShow = Value
    val ElemHide = Value}
  type SketchUiElemState = SketchUiElemState.Value
  //WindowsLayoutKind
  object WindowsLayoutKind extends Enumeration {
    val FillScreen = Value
    val WindowsStairs = Value}
  type WindowsLayoutKind = WindowsLayoutKind.Value
  //DequeueAlgo
  object DequeueAlgo extends Enumeration {
    val Queue = Value
    val Last = Value}
  type DequeueAlgo = DequeueAlgo.Value

//TODO Add more

}
