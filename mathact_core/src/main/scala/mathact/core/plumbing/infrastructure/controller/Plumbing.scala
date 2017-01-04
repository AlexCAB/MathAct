/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

import mathact.core.model.data.visualisation.BlockInfo
import mathact.core.model.holders.DriveRef


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
    val DriveTurnedOff = Value}
  type DriveState = DriveState.Value
  //Definitions
  case class DriveData(
    drive: DriveRef,
    blockId: Int,
    builtInfo: Option[BlockInfo],
    driveState: DriveState)}
