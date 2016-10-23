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

import java.io.{PrintWriter, StringWriter}

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

import scala.util.Try


/** Worker actor base (should not have children actors)
  * Created by CAB on 24.05.2016.
  */

abstract class WorkerBase extends Actor{
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]
  /** Resources cleanup (called in postStop) */
  def cleanup(): Unit
  //Receive
  def receive: PartialFunction[Any, Unit] = { case m ⇒
    log.debug(s"MESSAGE: $m FROM: $sender")
    reaction.applyOrElse[Any, Unit](m, _ ⇒ log.warning(s"LAST MESSAGE NOT HANDLED: $m"))}
  //On stop
  override def postStop(): Unit = cleanup()

  //TODO Add more


}
