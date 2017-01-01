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

package manual.sketches

import mathact.tools.Sketches


/** My sketches
  * Created by CAB on 31.12.2016.
  */

object MySketches extends Sketches{
  SketchOf[MyFirstSketch](
    name = "My first sketch",
    description = "The first sketch that I define but not implemented.",
    logger = true,
    visualisation = true)
  SketchOf[MySecondSketch](
    name = "My second sketch",
    description = "The second sketch that I wrote.")
  SketchOf[MyThirdSketch](
    name = "My third sketch",
    description = "The third complex sketch.",
    logger = true)

  //TODO Add more

}
