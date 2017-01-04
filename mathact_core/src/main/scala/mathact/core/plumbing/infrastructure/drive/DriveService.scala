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

import akka.actor.{ActorRef, Props}
import mathact.core.model.messages.M


/** Drive service methods
  * Created by CAB on 02.11.2016.
  */

private[core] trait DriveService { _: DriveActor ⇒
  //Methods
  /** User log info
    * @param message - String, user text */
  def userLogInfo(message: String): Unit = {
    log.debug("[DriveService.userLogInfo] Re send info message to user log actor.")
    userLogging ! M.LogInfo(Some(blockId), blockName.getOrElse(blockClassName), message)}
  /** User log warn
    * @param message - String, user text */
  def userLogWarn(message: String): Unit = {
    log.debug("[DriveService.userLogWarn]  Re send warn message to user log actor.")
    userLogging ! M.LogWarning(Some(blockId), blockName.getOrElse(blockClassName), message)}
  /** User log error
    * @param error - Option[Throwable], user exception
    * @param message - String, user text */
  def userLogError(error: Option[Throwable], message: String): Unit = {
    log.debug("[DriveService.userLogError] Re send error message to user log actor.")
    userLogging ! M.LogError(Some(blockId), blockName.getOrElse(blockClassName), error.toSeq, message)}
  /** Creating of new user actor
    * @param props - Props, new actor props
    * @param name - String, new actor name
    * @param sender - ActorRef, object that run ask request to response with new actor ref */
  def newUserActor(props: Props, name: Option[String], sender: ActorRef): Unit = {
    log.debug(
      s"[DriveService.newUserActor] Send CreateUserActor to userActorsRoot actor, props: $props, " +
      s"name: $name, sender: $sender.")
    userActorsRoot ! M.CreateUserActor(props, name, sender)}}
