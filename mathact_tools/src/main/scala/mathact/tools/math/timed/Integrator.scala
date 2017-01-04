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

package mathact.tools.math.timed

import mathact.core.bricks.blocks.BlockContext
import mathact.tools.math.TimedMath

/** Integrator
  * Created by CAB on 03.12.2016.
  */

class Integrator(implicit context: BlockContext)
extends TimedMath(context, "+", "mathact/tools/math/timed/integrator.png"){
  //Variables
  private var sum = 0.0
  //Evaluation
  protected def eval(timedInput: Vector[Double], singleInput: Vector[Double]): Double = {
    sum += (timedInput.sum + singleInput.sum)
    sum}}
