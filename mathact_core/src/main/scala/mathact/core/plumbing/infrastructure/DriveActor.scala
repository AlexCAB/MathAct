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

package mathact.core.plumbing.infrastructure

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.core.model.config.DriveConfigLike
import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.enums._
import mathact.core.model.messages.{M, StateMsg, Msg}
import mathact.core.{IdGenerator, StateActorBase}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.fitting._
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}


/** Manage tool
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

private [mathact] class DriveActor(
  val config: DriveConfigLike,
  val toolId: Int,
  val pump: PumpLike,
  val pumping: ActorRef,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends StateActorBase(ActorState.Init) with IdGenerator with DriveBuilding with DriveConnectivity
with DriveStartStop with DriveMessaging with DriveUIControl{ import ActorState._, TaskKind._
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Throwable ⇒ Resume }
  //Definitions
  case class SubscriberData(
    id: (ActorRef, Int),
    inlet: InletData,
    var inletQueueSize: Int = 0)
  case class OutletState(
    outletId: Int,
    name: Option[String],
    pipe: OutPipe[_],
    subscribers: MutMap[(ActorRef, Int), SubscriberData] = MutMap(),  //((subscribe tool drive, inlet ID), SubscriberData)
    var pushTimeout: Option[Long] = None)
  case class InletState(
    inletId: Int,
    name: Option[String],
    pipe: InPipe[_],
    taskQueue: MutQueue[M.RunTask[_]] = MutQueue(),
    publishers: MutMap[(ActorRef, Int), OutletData] = MutMap(),  // ((publishers tool drive, outlet ID), SubscriberData)
    var currentTask: Option[M.RunTask[_]] = None)
  //Variables
  val outlets = MutMap[Int, OutletState]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletState]()    //(Inlet ID, OutletData)
  var visualisationLaval: VisualisationLaval = VisualisationLaval.None
  //On start
  val impeller = context.actorOf(Props(new ImpellerActor(self, config.impellerMaxQueueSize)), "ImpellerOf_" + pump.toolName)
  context.watch(impeller)
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    case (M.BuildDrive, Init) ⇒
      state = Building
      doConnectivity()
    case (M.StartDrive, Built) ⇒
      state = Starting
      doStarting()
    case (M.StopDrive, Working) ⇒
      state = Stopping
      doStopping()
    case (M.TerminateDrive, Built | BuildingFailed | Starting | Stopping) ⇒
      state = Terminating
      doTerminating()}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all pipes connected in Building state, if so switch to Starting, send DriveBuilt and ToolBuilt
    case (_: M.PipesConnected | M.BuildDrive, Building) ⇒ isAllConnected match{
      case true ⇒
        log.debug(
          s"[DriveActor.postHandling @ Building] All pipes connected, send M.DriveBuilt, and switch to Working mode.")
        state = Built
        pumping ! M.DriveBuilt
        buildAndSendToolBuiltInfo()
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Building] Not all pipes connected.")}
    //This drive fail building
    case (M.DriveBuildingError, Init | Building) ⇒
      state = BuildingFailed
      buildingFailed()
    //Check if user start function executed in Starting state
    case (M.StartDrive | _: M.TaskDone | _: M.TaskFailed, Starting) ⇒ isStarted match{
      case true ⇒
        log.debug(
          s"[DriveActor.postHandling @ Starting] Started, send M.DriveStarted, " +
            s"run message processing and switch to Working mode.")
        state = Working
        startUserMessageProcessing()
        pumping ! M.DriveStarted
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Starting] Not started yet.")}
    //Check if user stop function executed in Stopping state
    case (M.StopDrive | _: M.TaskDone | _: M.TaskFailed, Stopping) ⇒ isStopped match{
      case true ⇒
        log.debug(s"[DriveActor.postHandling @ Stopping] Stopped, send M.DriveStopped")
        pumping ! M.DriveStopped
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Stopping] Not stopped yet.")}
    //Check if all message queues is empty in Terminating, and if so do terminating
    case (M.TerminateDrive | _: M.TaskDone | _: M.TaskFailed, Terminating) ⇒ isAllMsgProcessed match{
      case true ⇒
        log.debug(s"[DriveActor.postHandling @ Terminating] Terminated, send M.DriveTerminated, and PoisonPill")
        pumping ! M.DriveTerminated
        self ! PoisonPill
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Terminating] Not terminated yet.")}}
  /** Actor reaction on messages */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //Construction, adding pipes, ask from object
    case (M.AddOutlet(pipe, name), state) ⇒ sender ! addOutletAsk(pipe, name, state)
    case (M.AddInlet(pipe, name), state) ⇒ sender ! addInletAsk(pipe, name, state)
    //Connectivity, ask from object
    case (message: M.ConnectPipes, state) ⇒ sender ! connectPipesAsk(message, state)
    //Connectivity, internal
    case (M.AddConnection(id, initiator, inletId, outlet), Building) ⇒ addConnection(id, initiator, inletId, outlet)
    case (M.ConnectTo(id, initiator, outletId, inlet), Building) ⇒ connectTo(id, initiator, outletId, inlet)
    case (M.PipesConnected(id, inletId, outletId), Building) ⇒ pipesConnected(id, inletId, outletId)
    //Starting
    case (M.TaskDone(Start, _, time, _), Starting) ⇒ startingTaskDone(time)
    case (M.TaskTimeout(Start, _, time), Starting) ⇒ startingTaskTimeout(time)
    case (M.TaskFailed(Start, _, time, error), Starting) ⇒ startingTaskFailed(time, error)
    //Messaging, ask from object
    case (M.UserData(outletId, value), state) ⇒ sender ! userDataAsk(outletId, value, state)
    //Messaging
    case (M.UserMessage(outletId, inletId, value), state) ⇒ userMessage(outletId, inletId, value, state)
    case (M.DriveLoad(sub, outId, queueSize), Starting | Working | Stopping) ⇒ driveLoad(sub, outId, queueSize)
    case (M.TaskDone(Massage, inletId, time, _), Working | Stopping | Terminating) ⇒ messageTaskDone(inletId, time)
    case (M.TaskTimeout(Massage, inId, time), Working | Stopping | Terminating) ⇒ messageTaskTimeout(inId, time)
    case (M.TaskFailed(Massage, inId, t, err), Working | Stopping | Terminating) ⇒ messageTaskFailed(inId, t, err)
    //Stopping
    case (M.TaskDone(Stop, _, time, _), Stopping) ⇒ stoppingTaskDone(time)
    case (M.TaskTimeout(Stop, _, time), Stopping) ⇒ stoppingTaskTimeout(time)
    case (M.TaskFailed(Stop, _, time, error), Stopping) ⇒ stoppingTaskFailed(time, error)
    //Managing
    case (M.SkipTimeoutTask, _) ⇒ impeller ! M.SkipAllTimeoutTask
    case (M.SetVisualisationLaval(laval), _) ⇒ visualisationLaval = laval
    //UI control
    case (M.ShowToolUi, Built | Starting | Working | Stopping) ⇒ showToolUi()
    case (M.TaskDone(ShowUI, _, time, _), _) ⇒ showToolUiTaskDone(time)
    case (M.TaskTimeout(ShowUI, _, time), _) ⇒ showToolUiTaskTimeout(time)
    case (M.TaskFailed(ShowUI, _, time, error), _) ⇒ showToolUiTaskFailed(time, error)
    case (M.HideToolUi, Built | Starting | Working | Stopping) ⇒ hideToolUi()
    case (M.TaskDone(HideUI, _, time, _), _) ⇒ hideToolUiTaskDone(time)
    case (M.TaskTimeout(HideUI, _, time), _) ⇒ hideToolUiTaskTimeout(time)
    case (M.TaskFailed(HideUI, _, time, error), _) ⇒ hideToolUiTaskFailed(time, error)}}