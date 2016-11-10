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

package mathact.tools.workbenches

import mathact.core.bricks.blocks.Workbench


/** Simple workbench
  * Created by CAB on 24.10.2016.
  */

class SimpleWorkbench extends Workbench{
  //Variables
  private var _heading: Option[String] = None
  //DSL
  def heading_=(v: String) { _heading = v match{case "" ⇒ None; case s ⇒ Some(s)} }
  def heading = _heading
  //Abstract callbacks (will called by system after sketch will constructed)
  private[mathact] def sketchTitle: Option[String] = _heading

  //TODO Add more

}
