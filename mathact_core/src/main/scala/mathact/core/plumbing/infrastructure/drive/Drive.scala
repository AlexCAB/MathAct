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

import akka.actor.ActorRef
import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.collection.mutable.{Map => MutMap, Queue => MutQueue}


/** Drive definitions
  * Created by CAB on 15.10.2016.
  */

object Drive {
  //enums
  object State extends Enumeration {
    val Init = Value
    val Construct = Value
    val Connecting = Value
    val Connected = Value
    val TurnedOn = Value
    val Starting = Value
    val Working = Value
    val Stopping = Value
    val Stopped = Value
    val TurnOff = Value
    val TurnedOff = Value

  }
  type State = State.Value
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
  //Messages
  case class DriveBuildingError(message: String, error: Option[Throwable]) extends Msg
  case class DriveMessagingError(message: String, error: Option[Throwable]) extends Msg

  //TODO Add more

}
