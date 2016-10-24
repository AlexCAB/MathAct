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
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.collection.mutable.{Map => MutMap, Queue => MutQueue}


/** Drive definitions
  * Created by CAB on 15.10.2016.
  */

private[core] object Drive {
  //enums
  object State extends Enumeration {
    val Init = Value
    val Constructed = Value
    val Connecting = Value
    val Connected = Value
    val TurnedOn = Value
    val Starting = Value
    val Working = Value
    val Stopping = Value
    val Stopped = Value
    val TurningOff = Value
    val TurnedOff = Value}
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
    subscribers: MutMap[(ActorRef, Int), SubscriberData] = MutMap(),  //((subscribe block drive, inlet ID), SubscriberData)
    var pushTimeout: Option[Long] = None)
  case class InletState(
    inletId: Int,
    name: Option[String],
    pipe: InPipe[_],
    taskQueue: MutQueue[M.RunTask[_]] = MutQueue(),
    publishers: MutMap[(ActorRef, Int), OutletData] = MutMap(),  // ((publishers block drive, outlet ID), SubscriberData)
    var currentTask: Option[M.RunTask[_]] = None)
  //Messages


  //TODO Add more

}
