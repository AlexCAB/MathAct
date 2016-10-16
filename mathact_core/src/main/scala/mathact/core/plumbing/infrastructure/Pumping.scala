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

/** Pumping
  * Created by CAB on 16.10.2016.
  */

object Pumping {
  //Enums
  object State extends Enumeration {
    val Init = Value
    val Creating = Value
    val Building = Value
    val Built = Value
    val Starting = Value
    val Working = Value
    val Stopping = Value
    val Stopped = Value
    val Terminating = Value}
  type State = State.Value
  object DriveState extends Enumeration {
    val DriveInit = Value
    val DriveCreating = Value
    val DriveCreated = Value
    val DriveBuilding = Value
    val DriveBuilt = Value
    val DriveStarting = Value
    val DriveStarted = Value
    val DriveWorking = Value
    val DriveStopping = Value
    val DriveStopped = Value
    val DriveTerminating = Value
    val DriveTerminated = Value}
  type DriveState = DriveState.Value










}
