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

package examples

import examples.ui.BlocksWithUiExample
import examples.wiring.{FunWiringZipExample, FunWiringExample, ObjWiringExample}
import mathact.tools.Sketches


/** List of Examples
  * Created by CAB on 18.06.2016.
  */

object Examples extends Sketches{
  SketchOf[ObjWiringExample](
    name = "Obj tap wiring example",
    description = "Example of tap wiring in object style.",
    logger = true,
    visualisation = true)
  SketchOf[FunWiringExample](
    name = "Fun tap wiring example",
    description = "Example of tap wiring in functional style.",
    logger = true,
    visualisation = true)
  SketchOf[FunWiringZipExample](
    name = "Using of zip functions",
    description = "Example of using of zipAll and zipEach functions.",
    logger = true,
    visualisation = true)
  SketchOf[BlocksWithUiExample](
    name = "Using of BlockUI",
    description = "Example of using of BlockUI trait.")


  //TODO Add more

}



