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

package mathact.core.sketch.infrastructure.controller

import mathact.core.bricks.WorkbenchLike
import mathact.core.model.messages.Msg


/** Sketch controller
  * Created by CAB on 16.10.2016.
  */

private [mathact] object SketchController {
  //Enums
  object State extends Enumeration {
    val Init = Value
    val Creating = Value          //Build UI
    val Constructing = Value      //Build sketch instance
    val Building = Value          //Build plumping
    val Built = Value             //Wait fro start
    val Starting = Value          //Starting of plumping
    val Working = Value           //Working (wait for stop)
    val Stopping = Value          //Stopping of plumping
    val Stopped = Value           //Plumping stopped (wait for close command)
    val SketchFailed = Value      //Waiting for shutdown (fro close command) after error
    val Shutdown = Value}         //Sketch close button hit (may happen anytime, UI will hide if no errors)
  type State = State.Value





//  case object SketchDestructed extends Msg




}
