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

import examples.common.SimplePidExample
import examples.linking.CompactLinking
import examples.tools.generators.{AnalogGeneratorExample, DiscreteGeneratorExample}
import examples.tools.math.TimedMathExample
import examples.tools.plots.{ChartRecorderExample, SimpleScopeExample}
import examples.tools.pots.{AnalogPotExample, SettingDialExample, TimedValuesPotExample}
import examples.tools.time.TimeLoopExample
import examples.ui.BlocksWithUiExample
import examples.wiring.{FunWiringExample, FunWiringZipExample, ObjWiringExample}
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
    logger = true)
  SketchOf[AnalogPotExample](
    name = "Using of AnalogPot",
    description = "Example of using simple analog potentiometer.",
    logger = true)
  SketchOf[SimpleScopeExample](
    name = "Using of SimpleScope",
    description = "Example of using simple oscilloscope tool.")
  SketchOf[TimeLoopExample](
    name = "Using of TimeLoop",
    description = "Example of using TimeLoop.",
    logger = true)
  SketchOf[SettingDialExample](
    name = "Using of SettingDial",
    description = "Example of using SettingDial tool.",
    logger = true)
  SketchOf[TimedMathExample](
    name = "Using of TimedMath",
    description = "Example timed math operators.",
    logger = true)
  SketchOf[SimplePidExample](
    name = "Simple PID example",
    description = "Simple PID regulated model example.",
    autorun = true)





  //TODO Add more

}



