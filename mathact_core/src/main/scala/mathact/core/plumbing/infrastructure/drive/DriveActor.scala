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

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.core.model.config.DriveConfigLike
import mathact.core.model.enums._
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.UserActorsRoot
import mathact.core.plumbing.infrastructure.impeller.ImpellerActor
import mathact.core.{IdGenerator, ControllerBase}

import scala.collection.mutable.{Map ⇒ MutMap}

/** Manage tool
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

private [mathact] class DriveActor(
  val config: DriveConfigLike,
  val toolId: Int,
  val pump: PumpLike,
  val plumbing: ActorRef,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends ControllerBase(Drive.State.Init) with IdGenerator with DriveLife with DriveConnectivity
with DriveMessaging with DriveUIControl{ import Drive.State._
 import Drive._
 import TaskKind._
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Throwable ⇒ Resume }
  //Variables
  val outlets = MutMap[Int, OutletState]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletState]()    //(Inlet ID, OutletData)
  var visualisationLaval = VisualisationLaval.None
  //On start
  val impeller = newWorker(new ImpellerActor(self, config.impellerMaxQueueSize), "Impeller_" + pump.toolName)
  val userActorsRoot  = newWorker(new UserActorsRoot(self), "UserActorsRoot_" + pump.toolName)
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
    case (M.AddConnection(id, initiator, inletId, outlet), Connecting | Connected) ⇒
      addConnection(id, initiator, inletId, outlet)
      state
    //Connectivity, connect to
    case (M.ConnectTo(id, initiator, outletId, inlet), Connecting | Connected) ⇒
      connectTo(id, initiator, outletId, inlet)
      state
    //Check if all pipes connected in Building state, if so switch to Starting, send DriveBuilt and ToolBuilt
    case (M.PipesConnected(id, inletId, outletId), Connecting) ⇒
      pipesConnected(id, inletId, outletId)
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
    //Stop of user messaging processing
    case (M.TurnOffDrive, Stopped) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        TurningOff}
    //Check if all message queues is empty in Terminating, and if so do terminating
    case (_: M.TaskDone | _: M.TaskFailed, TurningOff) ⇒ isAllMsgProcessed match{
      case true ⇒
        driveTurnedOff()
        TurnedOff
      case false ⇒
        state}
    //Messaging, ask from object
    case (M.UserData(outletId, value), state) ⇒
      sender ! userDataAsk(outletId, value, state)
      state
    //Messaging, user message
    case (M.UserMessage(outletId, inletId, value), Connected | TurnedOn | Starting | Working | Stopping | Stopped) ⇒
      userMessage(outletId, inletId, value)
      state
    //Messaging, drive load
    case (M.DriveLoad(sub, outId, queueSize), st) if st != Init && st != Constructed && st != Connecting ⇒
      driveLoad(sub, outId, queueSize)
      state
    //Messaging, task done
    case (M.TaskDone(Massage, inletId, time, _), st) if st != Init && st != Constructed && st != Connecting ⇒
      messageTaskDone(inletId, time)
      state
    //Messaging, task timeout
    case (M.TaskTimeout(Massage, inId, time), st) if st != Init && st != Constructed && st != Connecting ⇒
      messageTaskTimeout(inId, time)
      state
    //Messaging, task failed
    case (M.TaskFailed(Massage, inId, t, err), st) if st != Init && st != Constructed && st != Connecting ⇒
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
    case (M.ShowToolUi, st) if st != Init ⇒
      showToolUi()
      state
//    //UI control
//    case (M.TaskDone(ShowUI, _, time, _), _) ⇒
//      showToolUiTaskDone(time)
//    //UI control
//    case (M.TaskTimeout(ShowUI, _, time), _) ⇒
//      showToolUiTaskTimeout(time)
//    case (M.TaskFailed(ShowUI, _, time, error), _) ⇒
//      showToolUiTaskFailed(time, error)
    //UI control
    case (M.HideToolUi, st)  if st != Init ⇒
      hideToolUi()
      state
//    case (M.TaskDone(HideUI, _, time, _), _) ⇒
//      hideToolUiTaskDone(time)
//    case (M.TaskTimeout(HideUI, _, time), _) ⇒
//      hideToolUiTaskTimeout(time)
//    case (M.TaskFailed(HideUI, _, time, error), _) ⇒
//      hideToolUiTaskFailed(time, error)

  }
  //Cleanup
  def cleanup(): Unit = {

    println("[DriveActor.cleanup] TODO")  //TODO Очистка ресурсов, в частности UI

  }

    //TODO Add more

}