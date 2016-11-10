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

package mathact.core.model

import akka.actor.ActorRef
import mathact.core.model.messages.Msg
import akka.actor.Actor


/** Value holders
  * Created by CAB on 03.11.2016.
  */

package holders {
  //Definitions
  private[core] trait RefHolder{
    private[core] val ref: ActorRef
    private[core] def !(m: Msg)(implicit sender: ActorRef = Actor.noSender): Unit = ref.tell(m, sender)}
  //Actors ref holders
  case class MainControllerRef(ref: ActorRef) extends RefHolder
  case class MainUIRef(ref: ActorRef) extends RefHolder
  case class SketchControllerRef(ref: ActorRef) extends RefHolder
  case class LayoutRef(ref: ActorRef) extends RefHolder
  case class PlumbingRef(ref: ActorRef) extends RefHolder
  case class DriveRef(ref: ActorRef) extends RefHolder
  case class SketchUIRef(ref: ActorRef) extends RefHolder
  case class UserLoggingRef(ref: ActorRef) extends RefHolder
  case class VisualizationRef(ref: ActorRef) extends RefHolder
  case class SketchInstanceRef(ref: ActorRef) extends RefHolder

 //TODO Add more

}
