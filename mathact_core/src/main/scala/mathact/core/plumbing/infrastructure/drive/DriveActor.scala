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

package mathact.core.plumbing.infrastructure.drive

import akka.actor._
import mathact.core.model.config.DriveConfigLike
import mathact.core.model.enums._
import mathact.core.model.holders.{LayoutRef, VisualizationRef, UserLoggingRef, PlumbingRef}
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.impeller.ImpellerActor
import mathact.core.plumbing.infrastructure.user.UserActorsRoot
import mathact.core.{IdGenerator, ControllerBase}

import scala.collection.mutable.{Map ⇒ MutMap}

/** Manage block
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

private[core] class DriveActor(
  val config: DriveConfigLike,
  val blockId: Int,
  val pump: PumpLike,
  val layout: LayoutRef,
  val plumbing: PlumbingRef,
  val userLogging: UserLoggingRef,
  val visualization: VisualizationRef)
extends ControllerBase(Drive.State.Init) with IdGenerator with DriveLife with DriveConnectivity
with DriveMessaging with DriveUIControl with DriveService{ import Drive.State._, Drive._, TaskKind._
  //Fields
  val blockClassName =  pump.block.getClass.getTypeName
  //Variables
  val outlets = MutMap[Int, OutletState]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletState]()    //(Inlet ID, OutletData)
  var visualisationLaval = VisualisationLaval.None
  //User parameters, will setup on constructed
  var blockName: Option[String] = None
  var blockImagePath: Option[String] = None
  //On start
  val impeller = newWorker(new ImpellerActor(self, config.impellerMaxQueueSize), "Impeller_" + blockClassName)
  val userActorsRoot  = newWorker(new UserActorsRoot(self), "UserActorsRoot_" + blockClassName)
  //Message handling
  def reaction: PartialFunction[(Msg, Drive.State), Drive.State] = {
    //Construction, adding outlet, ask from object
    case (M.AddOutlet(pipe, name), state) ⇒
      sender ! addOutletAsk(pipe, name, state)
      state
    //Construction, adding inlet, ask from object
    case (M.AddInlet(pipe, name), state) ⇒
      sender ! addInletAsk(pipe, name, state)
      state
    //Construct drive, disable future adding of inlets, outlets and connections
    case (M.ConstructDrive, Init) ⇒
      blockName = pump.block.blockName
      blockImagePath = pump.block.blockImagePath
      initBlockUi() match{
        case true ⇒
          Construction
        case false ⇒
          driveConstructed()
          Constructed}
    case (M.TaskDone(UiInit, _, time, _), Construction) ⇒
      blockUiInitialized(error = None, time)
      driveConstructed()
      Constructed
    case (M.TaskFailed(UiInit, _, time, error), Construction) ⇒
      blockUiInitialized(Some(error), time)
      driveConstructed()
      Constructed
    //Build drive, connect connections from pending list
    case (M.ConnectingDrive, Constructed) ⇒
      isPendingConListEmpty match{
        case true ⇒
          connectingSuccess()
          Connected
        case false ⇒
          doConnectivity()
          Connecting}
    //Connectivity, ask from object
    case (message: M.ConnectPipes, state) ⇒
      sender ! connectPipesAsk(message, state)
      state
    //Connectivity, add connection
    case (M.AddConnection(id, initiator, outlet, inlet), Constructed | Connecting | Connected) ⇒
      addConnection(id, initiator, outlet, inlet)
      state
    //Connectivity, connect to
    case (M.ConnectTo(id, initiator, outlet, inlet), Constructed | Connecting | Connected) ⇒
      connectTo(id, initiator, outlet, inlet)
      state
    //Check if all pipes connected in Building state, if so switch to Starting, send DriveBuilt and BlockConstructedInfo
    case (M.PipesConnected(connectionId, initiator, outlet, inlet), Connecting) ⇒
      pipesConnected(connectionId, initiator, outlet, inlet)
      isPendingConListEmpty match{
        case true ⇒
          connectingSuccess()
          Connected
        case false ⇒
          state}
    //Stating of user messages handling
    case (M.TurnOnDrive, Connected) ⇒
      startUserMessageProcessing()
      sendPendingMessages()
      TurnedOn
    //Start drive, run user starting function
    case (M.StartDrive, TurnedOn) ⇒
      createBlockUi() match{
        case true ⇒
          CreatingUI
        case false ⇒  //Not have UI
          doStarting() match{
            case true ⇒ Working
            case false ⇒ Starting}}
    case (M.TaskDone(UiCreate, _, time, _), CreatingUI) ⇒
      blockUiCreated(error = None, time)
      doStarting() match{
        case true ⇒ Working
        case false ⇒ Starting}
    case (M.TaskFailed(UiCreate, _, time, error), CreatingUI) ⇒
      blockUiCreated(Some(error), time)
      doStarting() match{
        case true ⇒ Working
        case false ⇒ Starting}
    case (M.TaskDone(Start, _, time, _), Starting) ⇒
      startingTaskDone(time)
      Working
    case (M.TaskFailed(Start, _, time, error), Starting) ⇒
      startingTaskFailed(time, error)
      Working
    case (M.TaskTimeout(Start, _, time, _), Starting) ⇒
      startingTaskTimeout(time)
      state
    //Stop drive, run user stopping function
    case (M.StopDrive, Working) ⇒ doStopping() match{
      case true ⇒
        closeBlockUi() match{
          case true ⇒
            ClosingUI
          case false ⇒ //Not have UI
            driveStopped()
            Stopped}
      case false ⇒
        Stopping}
    case (M.TaskDone(Stop, _, time, _), Stopping) ⇒
      stoppingTaskDone(time)
      closeBlockUi() match{
        case true ⇒
          ClosingUI
        case false ⇒ //Not have UI
          driveStopped()
          Stopped}
    case (M.TaskFailed(Stop, _, time, error), Stopping) ⇒
      stoppingTaskFailed(time, error)
      closeBlockUi() match{
        case true ⇒
          ClosingUI
        case false ⇒ //Not have UI
          driveStopped()
          Stopped}
    case (M.TaskTimeout(Stop, _, time, _), Stopping) ⇒
      stoppingTaskTimeout(time)
      state
    case (M.TaskDone(UiClose, _, time, _), ClosingUI) ⇒
      blockUiClosed(error = None, time)
      driveStopped()
      Stopped
    case (M.TaskFailed(UiClose, _, time, error), ClosingUI) ⇒
      blockUiClosed(Some(error), time)
      driveStopped()
      Stopped
    //Turning off, done if no messages to process
    case (M.TurnOffDrive, Stopped) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        TurningOff}
    case (M.TaskDone(Massage, inletId, time, _), TurningOff) ⇒
      messageTaskDone(inletId, time)
      isAllMsgProcessed match{
        case true ⇒
          driveTurnedOff()
          TurnedOff
        case false ⇒
          state}
    case (M.TaskTimeout(Massage, inId, time, _), TurningOff) ⇒
      messageTaskTimeout(inId, time)
      isAllMsgProcessed match{
        case true ⇒
          driveTurnedOff()
          TurnedOff
        case false ⇒
          state}
    case (M.TaskFailed(Massage, inId, t, err), TurningOff) ⇒
      messageTaskFailed(inId, t, err)
      isAllMsgProcessed match{
        case true ⇒
          driveTurnedOff()
          TurnedOff
        case false ⇒
          state}
    //Messaging
    case (M.UserData(outletId, value), state) ⇒
      sender ! userDataAsk(outletId, value, state)
      state
    case (M.UserMessage(outletId, inletId, value), st) ⇒
      userMessage(outletId, inletId, value, st)
      state
    case (M.DriveLoad(sub, outId, queueSize), st) if st != Init && st != Constructed && st != Connecting ⇒
      driveLoad(sub, outId, queueSize)
      state
    case (M.TaskDone(Massage, inletId, time, _), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      messageTaskDone(inletId, time)
      state
    case (M.TaskTimeout(Massage, inId, time, _), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      messageTaskTimeout(inId, time)
      state
    case (M.TaskFailed(Massage, inId, t, err), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      messageTaskFailed(inId, t, err)
      state
    //Managing, skip timeout task
    case (M.SkipTimeoutTask, _) ⇒
      impeller ! M.SkipAllTimeoutTask
      state
    //Managing, set visualisation laval
    case (M.SetVisualisationLaval(laval), _) ⇒
      visualisationLaval = laval
      state
    //UI control, show
    case (M.ShowBlockUi, st) if st != Init ⇒
      showBlockUi()
      state
    case (M.TaskDone(UiShow, _, time, _), _) ⇒
      blockUiShown(error = None, time)
      state
    case (M.TaskFailed(UiShow, _, time, error), _) ⇒
      blockUiShown(Some(error), time)
      state
    //UI control, hide
    case (M.HideBlockUi, st)  if st != Init ⇒
      hideBlockUi()
      state
    case (M.TaskDone(UiHide, _, time, _), _) ⇒
      blockUiHidden(error = None, time)
      state
    case (M.TaskFailed(UiHide, _, time, error), _) ⇒
      blockUiHidden(Some(error), time)
      state
    //UI control, update window position
    case (M.SetWindowPosition(id, x, y), st) if st != Init ⇒
      updateBlockUiPosition(id, x, y)
      state
    case (M.TaskDone(UiLayout, windowId, time, _), _) ⇒
      blockUiPositionUpdate(windowId, error = None, time)
      state
    case (M.TaskFailed(UiLayout, windowId, time, error), _) ⇒
      blockUiPositionUpdate(windowId, Some(error), time)
      state
    //User UI event
    case (M.UserUIEvent(event), st)  if st != Init ⇒
      sender ! blockUiEvent(event)
      state
    case (M.TaskDone(UiEvent, _, time, _), _) ⇒
      blockUiEventTaskDone(time)
      state
    case (M.TaskTimeout(UiEvent, _, time, _), _) ⇒
      blockUiEventTaskTimeout(time)
      state
    case (M.TaskFailed(UiEvent, _, time, error), _) ⇒
      blockUiEventTaskFailed(time, error)
      state
    //User logging
    case (M.UserLogInfo(message), st)  if st != Init && st != TurnedOff ⇒
      userLogInfo(message)
      state
    case (M.UserLogWarn(message), st)  if st != Init && st != TurnedOff ⇒
      userLogWarn(message)
      state
    case (M.UserLogError(error, message), st)  if st != Init && st != TurnedOff ⇒
      userLogError(error, message)
      state
    case (M.NewUserActor(props, name), _) ⇒
      newUserActor(props, name, sender)
      state}
  //Cleanup
  def cleanup(): Unit = {}}
