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

package mathact.core.plumbing.infrastructure.user

import akka.actor.ActorRef
import mathact.core.WorkerBase
import mathact.core.model.messages.M


/** Root for user actors
  * Created by CAB on 19.10.2016.
  */

private[core] class UserActorsRoot(drive: ActorRef) extends WorkerBase {
  //Messages handling
  def reaction = {
    //Creating of user actor
    case M.CreateUserActor(props, name, sender) ⇒
      log.debug(
        s"[UserActorsRoot @ CreateUserActor] Try to create actor for, props: $props, name: $name, sender: $sender")
      try{
        sender ! Right{ name match{
          case Some(n) ⇒ context.actorOf(props, n)
          case None ⇒ context.actorOf(props)}}}
      catch{ case error: Throwable ⇒
        log.error(
          error,
          s"[UserActorsRoot @ CreateUserActor] Error on creating of actor for, " +
          s"props: $props, name: $name, sender: $sender")
        sender ! Left(error)}}
  //Cleanup
  def cleanup(): Unit = {}}
