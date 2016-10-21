package mathact.core.plumbing.infrastructure.controller

import akka.actor.ActorRef
import mathact.core.model.data.visualisation.ToolBuiltInfo


/** Plumbing
  * Created by CAB on 16.10.2016.
  */

object Plumbing {
  //Enums
  object State extends Enumeration {
    val Init = Value
    val Constructing = Value
    val Connecting = Value
    val TurningOn = Value
    val TurnedOn = Value
    val Starting = Value
    val Working = Value
    val Stopping = Value
    val TurningOff = Value
    val TurnedOff = Value}
  type State = State.Value
  object DriveState extends Enumeration {
    val DriveInit = Value
    val DriveConstructed = Value
    val DriveConnected = Value
    val DriveTurnedOn = Value
    val DriveWorking = Value
    val DriveStopped = Value
    val TurnedOff = Value}
  type DriveState = DriveState.Value
  //Definitions
  case class DriveData(
    drive: ActorRef,
    toolId: Int,
    builtInfo: Option[ToolBuiltInfo],
    driveState: DriveState)}
