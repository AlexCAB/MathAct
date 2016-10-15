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

import akka.actor.ActorRef
import mathact.core.model.data.pipes.{OutletData, InletData}
import mathact.core.model.messages.{StateMsg, M}
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}


/** Drive definitions
  * Created by CAB on 15.10.2016.
  */

object Drive {
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
  case object DriveFail extends StateMsg

}
