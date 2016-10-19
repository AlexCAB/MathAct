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

import scala.collection.mutable.{Map => MutMap}


/** Manage tool
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

//TODO Добавить трайт UI, для котрого сдесь реализовать:
//TODO Если инструмент имеет трайт UI то при постройке вызвать метод "показать UI", а при терминировании "закрыть UI".
//TODO Эти мотоды можно вызывать в контексте потока актора (а ни импелера), та как там не будут кода пользователя,
//TODO и он только отправить собщение потоку UI но фактически ничего не будет делать.
//TODO В IU трайте должен быть флаг "показать UI" на старте или нет.
//TODO Так же не стоит забывать о сообщениях ShowToolUi и HideToolUi
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
  val pendingConnections = MutMap[Int, M.ConnectPipes]()
  var visualisationLaval: VisualisationLaval = VisualisationLaval.None
  //On start
  val impeller = newWorker(new ImpellerActor(self, config.impellerMaxQueueSize), "Impeller_" + pump.toolName)
  val userActorsRoot  = newWorker(new UserActorsRoot(self), "UserActorsRoot_" + pump.toolName)
  //Message handling
  def reaction: PartialFunction[(Msg, Drive.State), Unit] = {
    //Construct drive, just switch state to Created to disable future adding of inlets, outlets and connections
    case (M.ConstructDrive, Init) ⇒
      constructDrive()
      Created
    //Build drive, connect connections from pending list
    case (M.BuildDrive, Created) ⇒
      isAllConnected match{
        case true ⇒
          buildingSuccess()
          Built
        case false ⇒
          doConnectivity()
          Building}
    //Check if all pipes connected in Building state, if so switch to Starting, send DriveBuilt and ToolBuilt
    case (_: M.PipesConnected, Building) ⇒
      isAllConnected match{
        case true ⇒
          buildingSuccess()
          startUserMessageProcessing()
          Built
        case false ⇒
          state}
    //Start drive, run user starting function
    case (M.StartDrive, Built) ⇒ doStarting() match{
      case true ⇒
        doStarting()
        Starting
      case false ⇒

        Working
    }



      state = Starting






    //Check if user start function executed in Starting state
    case (M.StartDrive | _: M.TaskDone | _: M.TaskFailed, Starting) ⇒ isStarted match{
      case true ⇒
        log.debug(
          s"[DriveActor.postHandling @ Starting] Started, send M.DriveStarted, " +
            s"run message processing and switch to Working mode.")
        state = Working
        startUserMessageProcessing()
        startingSuccess()
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Starting] Not started yet.")}





    //Stop drive, run user stopping function
    case (M.StopDrive, Working) ⇒
      state = Stopping
      doStopping()
    //Terminate drive, stop actor clear resources
    case (M.TerminateDrive, Init | Created | Built | Stopped) ⇒
      state = Terminating
      doTerminating()



    //Check if user start function executed in Starting state
    case (M.StartDrive | _: M.TaskDone | _: M.TaskFailed, Starting) ⇒ isStarted match{
      case true ⇒
        log.debug(
          s"[DriveActor.postHandling @ Starting] Started, send M.DriveStarted, " +
            s"run message processing and switch to Working mode.")
        state = Working
        startUserMessageProcessing()
        startingSuccess()
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Starting] Not started yet.")}
    //Check if user stop function executed in Stopping state
    case (M.StopDrive | _: M.TaskDone | _: M.TaskFailed, Stopping) ⇒ isStopped match{
      case true ⇒
        log.debug(s"[DriveActor.postHandling @ Stopping] Stopped, send M.DriveStopped")
        state = Stopped
        stoppingSuccess()
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Stopping] Not stopped yet.")}
    //Check if all message queues is empty in Terminating, and if so do terminating
    case (M.TerminateDrive | _: M.TaskDone | _: M.TaskFailed, Terminating) ⇒ isAllMsgProcessed match{
      case true ⇒
        log.debug(s"[DriveActor.postHandling @ Terminating] Terminate impeller actor")
        impeller ! PoisonPill
      case false ⇒
        log.debug(s"[DriveActor.postHandling @ Terminating] Not terminated yet.")}





    //Construction, adding pipes, ask from object
    case (M.AddOutlet(pipe, name), state) ⇒ sender ! addOutletAsk(pipe, name, state)
    case (M.AddInlet(pipe, name), state) ⇒ sender ! addInletAsk(pipe, name, state)
    //Connectivity, ask from object
    case (message: M.ConnectPipes, state) ⇒ sender ! connectPipesAsk(message, state)
    //Connectivity, internal
    case (M.AddConnection(id, initiator, inletId, outlet), Building | Built) ⇒ addConnection(id, initiator, inletId, outlet)
    case (M.ConnectTo(id, initiator, outletId, inlet), Building | Built) ⇒ connectTo(id, initiator, outletId, inlet)
    case (M.PipesConnected(id, inletId, outletId), Building | Built) ⇒ pipesConnected(id, inletId, outletId)
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
    case (M.TaskFailed(HideUI, _, time, error), _) ⇒ hideToolUiTaskFailed(time, error)
    //Errors, (on error drive only report to plumbing actor but not stop working)
    case (DriveBuildingError(msg, error), _) ⇒ driveError(msg, error)
    case (DriveMessagingError(msg, error), _) ⇒ driveError(msg, error)




  }
  //Cleanup
  def cleanup(): Unit = {

    ???

  }












//
//
//
//
//  //Receives
//  /** Reaction on StateMsg'es */
//  def onStateMsg: PartialFunction[(StateMsg, Drive.State), Unit] = {
//
//
//
//  }
//
//
//
//
//  }
//  /** Handling of actor termination*/
//  def terminationHandling: PartialFunction[(ActorRef, Drive.State), Unit] = {
//    //If impeller terminated, self termination
//    case (`impeller`, Terminating) ⇒
//      log.debug(s"[DriveActor.terminationHandling] Impeller terminated, self termination.")
//      self ! PoisonPill
//
//  }
//  /** Actor reaction on messages */
//

    //TODO Add more

  }