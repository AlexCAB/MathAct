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

package mathact.tools.math.logic.bool

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.fun.{FunOnStart, FunWiring}
import mathact.tools.Tool


/** NOT operator
  * Created by CAB on 26.12.2016.
  */

class Not(implicit context: BlockContext)
extends Tool(context, "NOT", "mathact/tools/math/logic/bool/not.png")
with FunWiring with FunOnStart with LinkThrough[Boolean, Boolean]{
  //Connection points
  val in = In[Boolean]
  val out = Out[Boolean]
  //Processing
  start.map(_ ⇒ true).next(out)
  in.map(v ⇒ ! v).next(out)}
