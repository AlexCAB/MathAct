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

import examples.common._
import examples.linking._
import examples.tools.generators._
import examples.tools.indicators._
import examples.tools.math._
import examples.tools.plots._
import examples.tools.pots._
import examples.tools.time._
import examples.ui._
import examples.wiring._
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
    description = "Simple PID regulated model example.")
  SketchOf[BoolSwitchExample](
    name = "Boolean switch example",
    description = "Example of using boolean switch tool.",
    logger = true)
  SketchOf[BoolStrobeExample](
    name = "Boolean strobe example",
    description = "Example of using boolean strobe tool.",
    logger = true)
  SketchOf[BoolIndicatorExample](
    name = "Boolean indicator example",
    description = "Example of using boolean indicator tool.")
  SketchOf[BooleanLogicExample](
    name = "Boolean logic example",
    description = "Using of boolean operators.")
  SketchOf[DTriggerExample](
    name = "D-trigger example",
    description = "Using of boolean operators to build D-trigger.")
  SketchOf[ValueIndicatorExample](
    name = "Value indicator example",
    description = "Example of using value indicator tool.")
  SketchOf[ContinuousMathExample](
    name = "Continuous math example",
    description = "Example of continuous math operators.")
  SketchOf[FeedbackExample](
    name = "Feedback example",
    description = "Example of feedback loop.")



  //TODO Add more

}



