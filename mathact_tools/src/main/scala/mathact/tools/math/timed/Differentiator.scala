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

package mathact.tools.math.timed

import mathact.core.bricks.blocks.BlockContext
import mathact.tools.math.TimedMath

/** Differentiator
  * Created by CAB on 03.12.2016.
  */

class Differentiator(implicit context: BlockContext)
  extends TimedMath(context, "+", "mathact/tools/math/timed/differentiator.png"){
  //Variables
  private var pastValue = 0.0
  //Evaluation
  protected def eval(timedInput: Vector[Double], singleInput: Vector[Double]): Double = {
    val sum = timedInput.sum + singleInput.sum
    val diff = pastValue - sum
    pastValue = sum
    diff}}