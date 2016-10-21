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

package mathact.core

import java.util.UUID

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import mathact.core.model.messages.Msg

import scala.reflect.ClassTag


/** Controller base actor (have a children workers) with state (state machine)
  * Created by CAB on 26.08.2016.
  */

abstract class ControllerBase[S](initState: S) extends Actor{
  //Strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  log.debug(s"INIT STATE: $initState")
  //Variables
  private var currentState: S = initState
  //Getters
  def state = currentState
  //Abstract
  /** Handling after reaction executed */
  def reaction: PartialFunction[(Msg, S), S]
  /** Resources cleanup (called in postStop) */
  def cleanup(): Unit
  //Methods
  /** Helper for creating of new worker
    * @param creator - creating proc
    * @param name - name for new worker (will be unique by adding random string)
    * @tparam T - < WorkerBase
    * @return - new ActorRef */
  def newWorker[T <: WorkerBase: ClassTag](creator: ⇒ T, name: String): ActorRef = {
    val actor = context.actorOf(Props(creator), name + "_" + UUID.randomUUID)
    context.watch(actor)
    actor}
  /** Helper for creating of new controller
    * @param creator - creating proc
    * @param name - name for new worker should be unique
    * @tparam T - < ControllerBase
    * @return - new ActorRef */
  def newController[T <: ControllerBase[S]: ClassTag](creator: ⇒ T, name: String): ActorRef = {
    val actor = context.actorOf(Props(creator), name)
    context.watch(actor)
    actor}
  //Receive
  def receive: PartialFunction[Any, Unit] = {
    //Message processing
    case message: Msg ⇒
      log.debug(s"MESSAGE: $message, FROM: $sender, STATE: $currentState")
      val newState = reaction.applyOrElse[(Msg, S), S](
        (message, currentState),
        _ ⇒ {
          log.error(s"LAST MESSAGE NOT HANDLED: $message, STATE: $currentState")
          currentState})
      if(newState != currentState) log.debug(s"STATE CHANGED: $currentState ===> $newState")
      currentState = newState
    //Termination processing
    case Terminated(actor) ⇒  //Not sends if child died on normal stopping (controller ! PoisonPill)
      log.error(s"[ControllerBase @ Terminated] Controller crashed on child actor termination, actor: $actor")
      self ! PoisonPill
    //Unknown message
    case message: Any ⇒
      log.error(s"Receive not a Msg type: $message")}


  //TODO Add more


}