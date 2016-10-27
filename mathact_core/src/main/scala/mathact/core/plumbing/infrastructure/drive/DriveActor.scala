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
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.impeller.ImpellerActor
import mathact.core.plumbing.infrastructure.user.actors.UserActorsRoot
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
  val plumbing: ActorRef,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends ControllerBase(Drive.State.Init) with IdGenerator with DriveLife with DriveConnectivity
with DriveMessaging with DriveUIControl{ import Drive.State._, Drive._, TaskKind._
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
    //Construct drive, just switch state to Created to disable future adding of inlets, outlets and connections
    case (M.ConstructDrive, Init) ⇒
      blockName = pump.block.blockName
      blockImagePath = pump.block.blockImagePath
      constructDrive()
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
    case (M.StartDrive, TurnedOn) ⇒ doStarting() match{
      case true ⇒
        Working
      case false ⇒
        Starting}
    //Started
    case (M.TaskDone(Start, _, time, _), Starting) ⇒
      startingTaskDone(time)
      Working
    //Starting failed, only log to user logger and keep working
    case (M.TaskFailed(Start, _, time, error), Starting) ⇒
      startingTaskFailed(time, error)
      Working
    //Starting timeout, only log to user logger and keep waiting
    case (M.TaskTimeout(Start, _, time), Starting) ⇒
      startingTaskTimeout(time)
      state
    //Stop drive, run user stopping function
    case (M.StopDrive, Working) ⇒ doStopping() match{
      case true ⇒
        Stopped
      case false ⇒
        Stopping}
    //Stopped
    case (M.TaskDone(Stop, _, time, _), Stopping) ⇒
      stoppingTaskDone(time)
      Stopped
    //Stopping failed, only log to user logger and keep working
    case (M.TaskFailed(Stop, _, time, error), Stopping) ⇒
      stoppingTaskFailed(time, error)
      Stopped
    //Stopping timeout, only log to user logger and keep waiting
    case (M.TaskTimeout(Stop, _, time), Stopping) ⇒
      stoppingTaskTimeout(time)
      state
    ///Turning off, done if no messages to process
    case (M.TurnOffDrive, Stopped) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        TurningOff}
    //Turning off, if no more messages after TaskDone
    case (M.TaskDone(Massage, inletId, time, _), TurningOff) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        messageTaskDone(inletId, time)
        state}
    //Turning off, if no more messages after TaskTimeout
    case (M.TaskTimeout(Massage, inId, time), TurningOff) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        messageTaskTimeout(inId, time)
        state}
    //Turning off, if no more messages after TaskFailed
    case (M.TaskFailed(Massage, inId, t, err), TurningOff) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        messageTaskFailed(inId, t, err)
        state}
    //Messaging, ask from object
    case (M.UserData(outletId, value), state) ⇒
      sender ! userDataAsk(outletId, value, state)
      state
    //Messaging, user message
    case (M.UserMessage(outletId, inletId, value), st) ⇒
      userMessage(outletId, inletId, value, st)
      state
    //Messaging, drive load
    case (M.DriveLoad(sub, outId, queueSize), st) if st != Init && st != Constructed && st != Connecting ⇒
      driveLoad(sub, outId, queueSize)
      state
    //Messaging, task done
    case (M.TaskDone(Massage, inletId, time, _), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      messageTaskDone(inletId, time)
      state
    //Messaging, task timeout
    case (M.TaskTimeout(Massage, inId, time), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      messageTaskTimeout(inId, time)
      state
    //Messaging, task failed
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
    //UI control
    case (M.ShowBlockUi, st) if st != Init ⇒
      showBlockUi()
      state
//    //UI control
//    case (M.TaskDone(ShowUI, _, time, _), _) ⇒
//      showBlockUiTaskDone(time)
//    //UI control
//    case (M.TaskTimeout(ShowUI, _, time), _) ⇒
//      showBlockUiTaskTimeout(time)
//    case (M.TaskFailed(ShowUI, _, time, error), _) ⇒
//      showBlockUiTaskFailed(time, error)
    //UI control
    case (M.HideBlockUi, st)  if st != Init ⇒
      hideBlockUi()
      state
//    case (M.TaskDone(HideUI, _, time, _), _) ⇒
//      hideBlockUiTaskDone(time)
//    case (M.TaskTimeout(HideUI, _, time), _) ⇒
//      hideBlockUiTaskTimeout(time)
//    case (M.TaskFailed(HideUI, _, time, error), _) ⇒
//      hideBlockUiTaskFailed(time, error)
    //User logging info
    case (M.UserLogInfo(message), st)  if st != Init && st != TurnedOff ⇒
      userLogInfo(message)
      state
    //User logging warn
    case (M.UserLogWarn(message), st)  if st != Init && st != TurnedOff ⇒
      userLogWarn(message)
      state
    //User logging error
    case (M.UserLogError(error, message), st)  if st != Init && st != TurnedOff ⇒
      userLogError(error, message)
      state




  }
  //Cleanup
  def cleanup(): Unit = {

    println("[DriveActor.cleanup] TODO")  //TODO Очистка ресурсов, в частности UI

  }

    //TODO Add more

}