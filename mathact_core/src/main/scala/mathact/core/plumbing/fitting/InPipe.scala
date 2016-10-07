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

package mathact.core.plumbing.fitting

import mathact.core.model.data.pipes.InletData
import mathact.core.plumbing.Pump


/** Wrapper fot Inlet
  * Created by CAB on 24.08.2016.
  */

private [mathact] class InPipe[H] (
  in: Inlet[H],
  protected val pipeName: Option[String],
  protected val pump: Pump)
extends Pipe[H] with Socket[H]{
  //Construction
  protected val (toolId, pipeId) = pump.addInlet(this, pipeName)
  //Fields
  lazy val pipeData = InletData(toolId, pump.drive, pump.toolName, pipeId, pipeName)
  //Methods
  override def toString: String = s"InPipe(in: $in, pipeName: $pipeName, pump: $pump)"
  def processValue(value: Any): Unit = in.processValue(value)







}
