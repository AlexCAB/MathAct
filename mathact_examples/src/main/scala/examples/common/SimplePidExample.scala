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

package examples.common

import mathact.data.discrete.TimedValue
import mathact.tools.math.timed._
import mathact.tools.plots.ChartRecorder
import mathact.tools.pots.SettingDial
import mathact.tools.time.TimeLoop
import mathact.tools.workbenches.SimpleWorkbench


/** Simple PID regulated model example.
  * Created by CAB on 01.12.2016.
  */

class SimplePidExample extends SimpleWorkbench {
  //Blocks
  val timeLoop = new TimeLoop[TimedValue]{ initTimeout = 100; initMessage = TimedValue(0,0) }
  val chart = new ChartRecorder{ minRange = 0 }
  //Units
  val dials = new {
    val setPoint = new SettingDial{
      name = "Set point"
      min = .1
      max = .9
      init = .75}
    val drainSpeed = new SettingDial{
      name = "Drain speed"
      min = -1
      max = 0
      init = -.2}
    val pPoint = new SettingDial{
      name = "P"
      min = 0
      max = 2
      init = 1}
    val iPoint = new SettingDial{
      name = "I"
      min = 0
      max = 2
      init = .01}
    val dPoint = new SettingDial{
      name = "D"
      min = 0
      max = 2
      init = .01}}
  val tank = new {
    //Blocks
    val adder = new Adder
    val integrator = new Integrator
    val multiplier = new Multiplier
    //Connecting
    multiplier ~> adder
    adder ~> integrator
    //Pins
    val effect = adder.inF
    val feedback = multiplier.inF
    val drain = multiplier.inS
    val level = integrator.out}
  val controller = new {
    //Blocks
    val signInverter = new SignInverter
    val inAdder = new Adder
    val integrator = new Integrator
    val differentiator = new Differentiator
    val pMultiplier = new Multiplier
    val iMultiplier = new Multiplier
    val dMultiplier = new Multiplier
    val outAdder = new Adder
    //Connecting
    signInverter ~> inAdder
    inAdder ~>                pMultiplier    ~> outAdder
    inAdder ~> iMultiplier ~> integrator     ~> outAdder
    inAdder ~> dMultiplier ~> differentiator ~> outAdder
    //Pins
    val r = inAdder.inS
    val y = signInverter.inF
    val u = outAdder.out
    val p = pMultiplier.inS
    val i = iMultiplier.inS
    val d = dMultiplier.inS}
  //Connecting
  dials.setPoint ~> controller.r
  dials.drainSpeed ~> tank.drain
  dials.pPoint ~> controller.p
  dials.iPoint ~> controller.i
  dials.dPoint ~> controller.d
  tank.level ~> timeLoop.in
  timeLoop.out ~> controller.y
  timeLoop.out ~> tank.feedback
  controller.u ~> tank.effect
  controller.u ~> chart.line("effect")
  tank.level ~> chart.line("level")}
