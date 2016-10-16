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

import akka.actor.{Terminated, ActorRef, Actor}
import akka.event.{Logging, LoggingAdapter}
import mathact.core.model.messages.{StateMsg, Msg}


/** Base class for infrastructure with state
  * Created by CAB on 26.08.2016.
  */

abstract class StateActorBase[S](initState: S) extends Actor{
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  log.debug(s"INIT STATE: $initState")
  //Variables
  private var currentState: S = initState
  //Getters and setters
  def state = currentState
  def state_= (s: S):Unit = {
    log.debug(s"STATE UPDATED: $currentState ===> $s")
    currentState = s}
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, S), Unit]
  /** Handling after reaction executed */
  def reaction: PartialFunction[(Msg, S), Unit]
  /** Actor reaction on messages */
  def postHandling: PartialFunction[(Msg, S), Unit]
  /** Handling of actor termination*/
  def terminationHandling: PartialFunction[(ActorRef, S), Unit]
  //Receive
  def receive: PartialFunction[Any, Unit] = {
    case message: StateMsg ⇒
      log.debug(s"STATE MESSAGE: $message, FROM: $sender, CURRENT STATE: $currentState")
      onStateMsg.applyOrElse[(StateMsg, S), Unit](
        (message, currentState),
        _ ⇒ log.error(s"LAST STATE MESSAGE NOT HANDLED: $message, STATE: $currentState"))
      postHandling.applyOrElse[(Msg, S), Unit](
        (message, currentState),
        _ ⇒ Unit)
    case message: Msg ⇒
      log.debug(s"MESSAGE: $message, FROM: $sender, STATE: $currentState")
      reaction.applyOrElse[(Msg, S), Unit](
        (message, currentState),
        _ ⇒ log.error(s"LAST MESSAGE NOT HANDLED: $message, STATE: $currentState"))
      postHandling.applyOrElse[(Msg, S), Unit](
        (message, currentState),
        _ ⇒ Unit)
    case message: Terminated ⇒
      log.debug(s"TERMINATED MESSAGE: $message, FROM: $sender, CURRENT STATE: $currentState")
      terminationHandling.applyOrElse[(ActorRef, S), Unit](
        (message.actor, currentState),
        _ ⇒ log.error(s"LAST TERMINATED MESSAGE NOT HANDLED: $message, STATE: $currentState"))
    case message: Any ⇒
      log.error(s"Receive not a Msg type: $message")}


  //TODO Add more


}