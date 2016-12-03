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

package examples.common

import mathact.data.discrete.{TimedEvent, TimedValue}
import mathact.tools.flow.Border
import mathact.tools.math._
import mathact.tools.plots.ChartRecorder
//import mathact.tools.pots.SettingDial
import mathact.tools.time.TimeLoop
import mathact.tools.workbenches.SimpleWorkbench


/** Simple PID regulated model example.
  * Created by CAB on 01.12.2016.
  */

class SimplePidExample extends SimpleWorkbench {
  //Blocks









  val timeLoop = new TimeLoop[TimedValue]{
//    timeout = 10.mills
  }






//
//  val setPoint = new SettingDial{
//    min = 0.1
//    max = 0.9
//    init = 0.5}
//
//  val pPoint = new SettingDial{
//    min = 0.1
//    max = 0.9
//    init = 0.5}
//  val iPoint = new SettingDial{
//    min = 0.1
//    max = 0.9
//    init = 0.5}
//  val dPoint = new SettingDial{
//    min = 0.1
//    max = 0.9
//    init = 0.5}
//
//  val tap = new SettingDial{
//    min = -1.0
//    max = 0.0
//    init = -0.5}
//
//  val tank = new {
//    //Blocks
//    private val summer = new Adder
//    private val integrator = new Integrator
//    //Connecting
//    summer.out ~> integrator.in
//    //Pins
//    val tap = summer.in
//    val effect = summer.in
//    val level = integrator.out}
//
//  val controller = new {
//    //Blocks
//    private val subtractor = new Subtractor
//    private val integrator = new Integrator
//    private val differentiator = new Differentiator
//    private val pMultiplier = new Multiplier
//    private val iMultiplier = new Multiplier
//    private val dMultiplier = new Multiplier
//    private val border = new Border
//    private val summer = new Adder
//    //Connecting
//    subtractor                   ~> pMultiplier ~> border
//    subtractor ~> integrator     ~> iMultiplier ~> border ~> summer
//    subtractor ~> differentiator ~> dMultiplier ~> border
//    //Pins
//    val r = subtractor.plaus
//    val y = subtractor.minus
//    val u = summer.out
//    val p = pMultiplier.in
//    val i = iMultiplier.in
//    val d = dMultiplier.in}
//
//  val chart = new ChartRecorder{
//
//
//  }
//
//
//  //Connecting
//  tap ~> tank.tap
//  setPoint ~> controller.r
//  pPoint ~> controller.p
//  iPoint ~> controller.i
//  dPoint ~> controller.d
//  tank.level ~> timedLoop ~> controller.y
//  controller.u ~> tank.effect
//  tank.tap ~> chart.line("tap")
//  tank.effect ~> chart.line("effect")
//  tank.level ~> chart.line("level")



}
