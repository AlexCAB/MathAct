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


/** Sketch controller
  * Created by CAB on 16.10.2016.
  */

private [mathact] object SketchController {
  //Enums
  object State extends Enumeration {
    val Init = Value
    val Creating = Value          //Build UI
    val Constructing = Value      //Build of sketch instance
    val Building = Value          //Build plumping
    val Built = Value             //Wait fro start
    val Starting = Value          //Starting of plumping
    val Working = Value           //Working (wait for stop)
    val Stopping = Value          //Stopping of plumping
    val Ended = Value}            //Plumping stopped or sketch shutdown, wait for close command (terminal state)
  type State = State.Value
  object Mode extends Enumeration {
    val Work = Value
    val Shutdown = Value
    val Fail = Value}
  type Mode = Mode.Value}
