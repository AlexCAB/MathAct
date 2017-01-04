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

package mathact.core.plumbing.infrastructure.drive

import mathact.core.model.holders.DriveRef
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}

import scala.collection.mutable.{Map => MutMap, Queue => MutQueue}


/** Drive definitions
  * Created by CAB on 15.10.2016.
  */

private[core] object Drive {
  //enums
  object State extends Enumeration {
    val Init = Value
    val Construction = Value
    val Constructed = Value
    val Connecting = Value
    val Connected = Value
    val TurnedOn = Value
    val CreatingUI = Value
    val Starting = Value
    val Working = Value
    val Stopping = Value
    val ClosingUI = Value
    val Stopped = Value
    val TurningOff = Value
    val TurnedOff = Value}
  type State = State.Value
  //Definitions
  case class PublisherData(
    id: (DriveRef, Int),
    blockDrive: DriveRef,
    blockId: Int,
    outletId: Int)
  case class SubscriberData(
    id: (DriveRef, Int),
    blockDrive: DriveRef,
    blockId: Int,
    inletId: Int,
    var inletQueueSize: Int = 0)
  case class OutletState(
    outletId: Int,
    name: Option[String],
    pipe: OutPipe[_],
    subscribers: MutMap[(DriveRef, Int), SubscriberData] = MutMap(),  //((subscribe block drive, inlet ID), SubscriberData)
    var pushTimeout: Option[Long] = None)
  case class InletState(
    inletId: Int,
    name: Option[String],
    pipe: InPipe[_],
    taskQueue: MutQueue[M.RunTask[_]] = MutQueue(),
    publishers: MutMap[(DriveRef, Int), PublisherData] = MutMap(),  // ((publishers block drive, outlet ID), PublishersData)
    var currentTask: Option[M.RunTask[_]] = None)}
