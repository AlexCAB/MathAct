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

/** Contains common enums
  * Created by CAB on 24.06.2016.
  */

package object enums {
  //SketchStatus
  object SketchStatus extends Enumeration {
    val Autorun, Ready, Ended, Failed = Value}
  type SketchStatus = SketchStatus.Value
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
    val Start, Massage, Stop, ShowUI, HideUI = Value}
  type TaskKind = TaskKind.Value
  //VisualisationLaval
  object VisualisationLaval extends Enumeration {
    val None, Basic, Load, Full = Value}
  type VisualisationLaval = VisualisationLaval.Value
  //ActorState
  object ActorState extends Enumeration {
    val Init = Value
    val Creating = Value
    val Created = Value
    val Building = Value
    val Built = Value
    val BuildingFailed = Value
    val Starting = Value
    val Started = Value
    val Working = Value
    val Stopping = Value
    val Stopped = Value
    val Destructing = Value
    val Destructed = Value
    val Terminating = Value
    val Terminated = Value}
  type ActorState = ActorState.Value
  //SketchUIElement
  object SketchUIElement extends Enumeration {
    val LogBtn = Value
    val VisualisationBtn = Value
    val RunBtn = Value
    val ShowAllToolsUiBtn = Value
    val HideAllToolsUiBtn = Value
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

//TODO Add more

}
