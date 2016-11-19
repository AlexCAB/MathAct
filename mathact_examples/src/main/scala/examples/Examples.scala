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

import examples.linking.CompactLinking
import examples.tools.generators.{AnalogGeneratorExample, DiscreteGeneratorExample}
import examples.tools.plots.ChartRecorderExample
import examples.tools.pots.TimedValuesPotExample
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
  SketchOf[CompactLinking](
    name = "Compact linking",
    description = "Example of using of compact linking DSL.",
    logger = true,
    visualisation = true)
  SketchOf[BlocksWithUiExample](
    name = "Using of BlockUI",
    description = "Example of using of BlockUI trait.")
  SketchOf[DiscreteGeneratorExample](
    name = "Using of DiscreteGenerator",
    description = "Simple example of using of DiscreteGenerator tool.",
    logger = true)
  SketchOf[TimedValuesPotExample](
    name = "Using of TimedValuesPot",
    description = "Example of using of TimedValuesPot tool.",
    logger = true)
  SketchOf[ChartRecorderExample](
    name = "Using of ChartRecorder",
    description = "Example of using of simple chart recorder tool.")
  SketchOf[AnalogGeneratorExample](
    name = "Using of AnalogGenerator",
    description = "Example of using analog generator tool.",
    logger = true,
    autorun = true)







  //TODO Add more

}



