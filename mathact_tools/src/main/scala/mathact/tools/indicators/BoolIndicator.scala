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

package mathact.tools.indicators

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.core.bricks.ui.BlockUI
import mathact.data.discrete.TimedValue
import mathact.parts.ui.Colors
import mathact.tools.Tool

import scalafx.scene.paint.Color


/** Boolean indicator
  * Created by CAB on 24.12.2016.
  */

class BoolIndicator(implicit context: BlockContext)
extends Tool(context, "BI", "mathact/tools/indicators/boolean_indicator.png")
with ObjWiring with BlockUI with LinkIn[TimedValue] with Colors{








  //Inlets
  def in: Socket[Boolean] = Inlet(buildLine(name = "Line",color = nextColor))
  def line(name: String = "Line", color: Color = nextColor): Socket[Boolean] = Inlet(buildLine(name, color))}
