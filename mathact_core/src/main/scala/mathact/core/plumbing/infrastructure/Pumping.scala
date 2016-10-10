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

import java.util.UUID

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.core.model.config.PumpingConfigLike
import mathact.core.model.data.visualisation.ToolBuiltInfo
import mathact.core.model.enums.ActorState
import mathact.core.model.messages.{M, StateMsg, Msg}
import mathact.core.{IdGenerator, StateActorBase}
import mathact.core.plumbing.PumpLike
import collection.mutable.{Map ⇒ MutMap}


/** Supervisor for all Pumps
  * Created by CAB on 15.05.2016.
  */

private [mathact] class Pumping(
  config: PumpingConfigLike,
  controller: ActorRef,
  sketchName: String,
  userLogging: ActorRef,
  visualization: ActorRef)
extends StateActorBase(ActorState.Init) with IdGenerator{ import ActorState._
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Resume}
  //Definitions
  case class DriveData(
    drive: ActorRef,
    toolId: Int,
    builtInfo: Option[ToolBuiltInfo] = None,
    driveState: ActorState = Building)
  //Variables
  val drives = MutMap[ActorRef, DriveData]()
  var isDoStopping = false
  //Functions
  def createDriveActor(toolPump: PumpLike): (ActorRef, Int) = {
    val toolId = nextIntId
    val drive = context.actorOf(
      Props(new Drive(config.drive, toolId, toolPump, self, userLogging, visualization)),
      "DriveOf_" + toolPump.toolName + "_" + UUID.randomUUID)
    context.watch(drive)
    (drive, toolId)}
  def newDrive(toolPump: PumpLike, state: ActorState, actor: PumpLike⇒(ActorRef, Int))
  :Unit = state match{
    case Init | Building ⇒
      //New drive
      val (drive, toolId) = createDriveActor(toolPump)
      log.debug(s"[newDrive] New drive created, toolName: ${toolPump.toolName}, drive: $drive")
      drives += (drive → DriveData(drive, toolId))
      //Response
      sender ! Right(drive)
    case s ⇒
      //Incorrect state
      val msg = s"[newDrive] Creating of drive not in Building state step, toolName: ${toolPump.toolName}, state: $s"
      log.error(msg)
      sender ! Left(new Exception(msg))}
  def setSenderDriveState(state: ActorState): Unit = drives.get(sender) match{
    case Some(driveData) ⇒
      log.debug(s"[setSenderDriveState] Set $state state for drive $sender.")
      drives += sender → driveData.copy(driveState = state)
    case None ⇒
      log.error(s"[setSenderDriveState] Unknown drive $sender, registered drives: ${drives.values}.")}
  def callIfAllDrivesInState(state: ActorState)(proc: ⇒ Unit): Unit = drives
    .values.exists(_.driveState != state) match{
      case false ⇒
        log.debug(s"[callIfAllDrivesInState] All drives in $state state, run proc.")
        proc
      case true ⇒
        log.debug(s"[callIfAllDrivesInState] Not all drives in $state state, drives: ${drives.values}")}
  def setAndSendToDrives(state: ActorState, msg: Msg): Unit = drives.foreach{ case (key, driveData) ⇒
    log.debug(s"[setAndSendToDrives] Set $state state and send $msg to drive: $driveData")
    drives += key → driveData.copy(driveState = state)
    driveData.drive ! msg}
  def allDrivesStarted(): Unit = {
    log.debug(s"[allDrivesStarted] All drives started, send M.PumpingStarted, drives: ${drives.values}.")
    drives.foreach{case (key, driveData) ⇒ drives += key → driveData.copy(driveState = Working)}
    controller ! M.PumpingStarted
    visualization ! M.AllToolBuilt}
  def allDrivesTerminated(): Unit = {
    log.debug(s"[allDrivesTerminated] All drives started, send M.PumpingStarted, drives: ${drives.values}.")
    drives.foreach{case (key, driveData) ⇒ drives += key → driveData.copy(driveState = Terminated)}
    controller ! M.PumpingStopped
    self ! PoisonPill}
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    //Switch to Starting, send BuildDrive to all drives
    case (M.StartPumping, Init) ⇒
      state = Building
      setAndSendToDrives(Building,  M.BuildDrive)
    //Switch to Stopping, send StopDrive to all drives
    case (M.StopPumping, Working) ⇒
      state = Stopping
      setAndSendToDrives(Stopping,  M.StopDrive)
    case (M.StopPumping, Building | Starting) ⇒
      isDoStopping = true}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all drive built, if so send StartDrive to all drives
    case (M.DriveBuilt, Building) ⇒ callIfAllDrivesInState(Built){
      isDoStopping match{
        case false ⇒
          state = Starting
          setAndSendToDrives(Starting,  M.StartDrive)
        case true ⇒
          state = Terminating
          setAndSendToDrives(Stopped,  M.TerminateDrive)}}
    //Check if all drive started, if so switch to Working and send PumpingStarted
    case (M.StartPumping | M.DriveStarted, Starting) ⇒ callIfAllDrivesInState(Started){
      isDoStopping match{
        case false ⇒
          state = Working
          allDrivesStarted()
        case true ⇒
          state = Stopping
          setAndSendToDrives(Stopping,  M.StopDrive)}}
    //Check if all drive stopped, if so send TerminateDrive to all drives
    case (M.DriveStopped, Stopping) ⇒ callIfAllDrivesInState(Stopped){
      state = Terminating
      setAndSendToDrives(Stopped,  M.TerminateDrive)}
    //Check if all drive terminate, if so switch to Terminating, send PumpingStopped and terminating
    case (M.StopPumping | M.DriveTerminated, Terminating) ⇒ callIfAllDrivesInState(Terminated){
      state = Terminated
      allDrivesTerminated()}}
  /** Actor reaction on messages */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //Creating of new drive for tool (ask request)
    case (M.NewDrive(toolPump), state) ⇒ newDrive(toolPump, state, createDriveActor)
    //Updates of driveState
    case (M.DriveBuilt, Building) ⇒ setSenderDriveState(Built)
    case (M.DriveStarted, Starting) ⇒ setSenderDriveState(Started)
    case (M.DriveStopped, Stopping) ⇒ setSenderDriveState(Stopped)
    case (M.DriveTerminated, Terminating) ⇒ setSenderDriveState(Terminated)
    //Re send SkipAllTimeoutTask to all drives
    case (M.SkipAllTimeoutTask, _) ⇒ drives.values.foreach(_.drive ! M.SkipTimeoutTask)
    case (M.ShowAllToolUi, _) ⇒ drives.values.foreach(_.drive ! M.ShowToolUi)
    case (M.HideAllToolUi, _) ⇒ drives.values.foreach(_.drive ! M.HideToolUi)}}
