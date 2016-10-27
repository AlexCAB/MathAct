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

import akka.actor._
import mathact.core.model.config.PlumbingConfigLike
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.drive.DriveActor
import mathact.core.ControllerBase


/** Supervisor for all Pumps
  * Created by CAB on 15.05.2016.
  */

private [core] class PlumbingActor(
  val config: PlumbingConfigLike,
  val controller: ActorRef,
  val sketchName: String,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends ControllerBase(Plumbing.State.Init)
with PlumbingLife{ import Plumbing._, Plumbing.State._, Plumbing.DriveState._
  //Creators functions
  def createDriveActor(blockId: Int, blockPump: PumpLike): ActorRef = newController(
    new DriveActor(config.drive, blockId, blockPump, self, userLogging, visualization),
    "DriveOf_" + blockPump.getClass.getTypeName + "_" + UUID.randomUUID)
  //Message handling
  def reaction: PartialFunction[(Msg, State), State] = {
    //Creating of drive for new block instance
    case (M.NewDrive(blockPump), _) ⇒
      newDrive(blockPump, state, createDriveActor)
      state
    //Run block constructing, connectivity and turning on, on sketch start
    case (M.BuildPlumbing, Init) ⇒ isDrivesEmpty match{
      case true ⇒
        noDrivesOnBuild()
        TurnedOff
      case false ⇒
        sendToEachDrive(M.ConstructDrive)
        Constructing}
    //Check if all drives constructed, and run wiring if so
    case (M.DriveConstructed, Constructing) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveConstructed) match{
      case true ⇒
        sendToEachDrive(M.ConnectingDrive)
        Connecting
      case false ⇒
        state}
    //Check if all drives connected, verify, and turning on if so
    case (M.DriveConnected, Connecting) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveConnected) match{
      case true ⇒
        verifyGraphStructure()
        sendToEachDrive(M.TurnOnDrive)
        TurningOn
      case false ⇒
        state}
    //Check if all drives turned on, and if so switch to TurnedOn and wait for start command
    case (M.DriveTurnedOn, TurningOn) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveTurnedOn) match{
      case true ⇒
        allDrivesBuilt()
        TurnedOn
      case false ⇒
        state}
    //Run user start functions on hit UI "START" of by auto-run
    case (M.StartPlumbing, TurnedOn) ⇒
      sendToEachDrive(M.StartDrive)
      Starting
    //Check if all drives started, and if so switch to Working
    case (M.DriveStarted, Starting) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveWorking) match{
      case true ⇒
        allDrivesStarted()
        Working
      case false ⇒
        state}
    //Run user stop functions and turning off, on hit UI "STOP"
    case (M.StopPlumbing, Working) ⇒
      sendToEachDrive(M.StopDrive)
      Stopping
    //Check if all drives stopped and run turning off if so
    case (M.DriveStopped, Stopping) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveStopped) match{
      case true ⇒
        sendToEachDrive(M.TurnOffDrive)
        TurningOff
      case false ⇒
        state}
    //Check if all drives turned off and if so switch to TurnedOff and wait for termination
    case (M.DriveTurnedOff, TurningOff) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveTurnedOff) match{
      case true ⇒
        allDrivesStopped()
        TurnedOff
      case false ⇒
        state}
    //DriveVerification
    case (M.DriveVerification(verificationData), _) ⇒
      driveVerification(verificationData)
      state
    //Skip timeout task for all drives
    case (M.SkipAllTimeoutTask, _) ⇒
      sendToEachDrive(M.SkipTimeoutTask)
      state
    //Show block UI for all drives
    case (M.ShowAllBlockUi, _) ⇒
      sendToEachDrive(M.ShowBlockUi)
      state
    //Hide block UI for all drives
    case (M.HideAllBlockUi, _) ⇒
      sendToEachDrive(M.HideBlockUi)
      state}
  //Cleanup
  //TODO Очистка ресурсов, (пока вероятно зедсь ничего не будет, но проверить)
  def cleanup(): Unit = {}}
