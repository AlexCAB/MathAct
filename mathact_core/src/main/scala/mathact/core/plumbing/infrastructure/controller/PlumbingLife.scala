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

package mathact.core.plumbing.infrastructure.controller

import java.util.UUID

import akka.actor.{Props, ActorRef}
import mathact.core.IdGenerator
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.controller.Plumbing.DriveState._
import mathact.core.plumbing.infrastructure.controller.Plumbing.State._
import mathact.core.plumbing.infrastructure.drive.DriveActor
import scala.collection.mutable.{ListBuffer => MutList, Map => MutMap}


/** Plumbing life cycle
  * Created by CAB on 21.10.2016.
  */

private [mathact] trait PlumbingLife extends IdGenerator{  _: PlumbingActor ⇒ import Plumbing._
  //Variables
  private val drives = MutMap[ActorRef, DriveData]()
  //Methods
  /** Creating of tew drive
    * @param toolPump - tool data received from pump
    * @param state - current state
    * @param newActor - ref of new drive actor */
  def newDrive(toolPump: PumpLike, state: State, newActor: (Int, PumpLike)⇒ActorRef)
  :Unit = state match{
    case Init | Constructing ⇒
      //New drive
      val toolId = nextIntId
      val drive = createDriveActor(toolId, toolPump)
      log.debug(s"[newDrive] New drive created, toolName: ${toolPump.toolName}, drive: $drive")
      drives += (drive → DriveData(drive, toolId, None, DriveInit))
      //Response
      sender ! Right(drive)
    case _ ⇒
      //Incorrect state
      val msg =
        s"[PlumbingActor.newDrive] Creating of drive not in Building state step, " +
          s"toolName: ${toolPump.toolName}, state: $state"
      userLogging ! M.LogError(
        None,
        "Plumbing",
        Seq(),
        s"Can not create tool drive in sate $state, request from: ${toolPump.toolName}")
      log.error(msg)
      sender ! Left(new Exception(msg))}















  def setDriveState(actor: ActorRef, state: DriveState): Unit = drives.get(actor) match{
    case Some(driveData) ⇒
      log.debug(s"[PlumbingActor.setDriveState] Set $state state for drive $actor.")
      drives += actor → driveData.copy(driveState = state)
    case None ⇒
      log.error(s"[PlumbingActor.setDriveState] Unknown drive $actor, registered drives: ${drives.values}.")}
  def callIfAllDrivesInState(state: DriveState)(proc: ⇒ Unit): Unit = drives
    .values.exists(_.driveState != state) match{
    case false ⇒
      log.debug(s"[PlumbingActor.callIfAllDrivesInState] All drives in $state state, run proc.")
      proc
    case true ⇒
      log.debug(s"[PlumbingActor.callIfAllDrivesInState] Not all drives in $state state, drives: ${drives.values}")}
  def setAndSendToDrives(state: DriveState, msg: Msg): Unit = drives.foreach{ case (key, driveData) ⇒
    log.debug(s"[PlumbingActor.setAndSendToDrives] Set $state state and send $msg to drive: $driveData")
    drives += key → driveData.copy(driveState = state)
    driveData.drive ! msg}
  def allDrivesBuilt(): Unit = {
    log.debug(s"[PlumbingActor.allDrivesBuilt] Report to controller, userLogging and visualization.")
    controller ! M.PlumbingBuilt
    userLogging ! M.LogInfo(None, "Plumbing", s"All tool successful built, ready to start!")
    visualization ! M.AllToolBuilt}
  def allDrivesStarted(): Unit = {
    log.debug(s"[PlumbingActor.allDrivesStarted] All drives started, send M.PlumbingStarted, drives: ${drives.values}.")
    drives.foreach{case (key, driveData) ⇒ drives += key → driveData.copy(driveState = DriveWorking)}
    controller ! M.PlumbingStarted
    userLogging ! M.LogInfo(None, "Plumbing", s"All tools started, working!")}
  def allDrivesStopped(): Unit = {
    log.debug(s"[PlumbingActor.allDrivesStopped] All drives stopped, send M.PlumbingStopped, drives: ${drives.values}.")
    controller ! M.PlumbingStopped
    userLogging ! M.LogInfo(None, "Plumbing", s"All tools stopped, ready to shutdown.")}
  def doShutdown(state: Plumbing.State): Plumbing.State = state match{
    case Creating | Building | Starting | Stopping | Terminating ⇒
      log.debug(s"[PlumbingActor.doShutdown] In the sate $state do nothing, wait for end of operation.")
      state
    case Init | Built | Stopped ⇒
      log.debug(s"[PlumbingActor.doShutdown] In the sate $state just run all drive termination.")
      setAndSendToDrives(DriveTerminating,  M.TerminateDrive)
      Terminating
    case Working ⇒
      log.debug(s"[PlumbingActor.doShutdown] In the sate $state run all drive stopping.")
      setAndSendToDrives(DriveStopping,  M.StopDrive)
      Stopping
    case _ ⇒
      log.error(s"[PlumbingActor.doShutdown] Unknown state: $state.")
      state}













}
