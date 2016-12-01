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

import mathact.tools.workbenches.SimpleWorkbench


/** Simple PID regulated model example.
  * Created by CAB on 01.12.2016.
  */

class SimplePidExample extends SimpleWorkbench {
  //Blocks

  val timedLoop = new TimeLoop{
    timeout = 10.mills
  }

  val setPoint = new Pot{
    min = 0.1
    max = 0.9
    init = 0.5}

  val pPoint = new Pot{
    min = 0.1
    max = 0.9
    init = 0.5}
  val iPoint = new Pot{
    min = 0.1
    max = 0.9
    init = 0.5}
    init = 0.5}
  val dPoint = new Pot{
    min = 0.1
    max = 0.9
    init = 0.5}

  val tap = new Pot{
    min = -1.0
    max = 0.0
    init = -0.5}

  val tank = new {
    //Blocks
    private val summer = new BufferedSummer
    private val integrator = new Integrator
    //Connecting
    summer.out ~> integrator.in
    //Pins
    val tap = summer.in
    val effect = summer.in
    val level = integrator.out}

  val controller = new {
    //Blocks
    private val subtractor = new BufferedSubtractor
    private val integrator = new Integrator
    private val differentiator = new Differentiator
    private val pMultiplier = new BufferedMultiplier
    private val iMultiplier = new BufferedMultiplier
    private val dMultiplier = new BufferedMultiplier
    private val summer = new BorderSummer
    //Connecting
    subtractor                   ~> pMultiplier ~> summer
    subtractor ~> integrator     ~> iMultiplier ~> summer
    subtractor ~> differentiator ~> dMultiplier ~> summer
    //Pins
    val r = subtractor.plaus
    val y = subtractor.minus
    val u = summer.out
    val p = pMultiplier.in
    val i  = iMultiplier.in
    val d = dMultiplier.in}

  val chart = new ChartRecorder{


  }


  //Connecting
  tap ~> tank.tap
  setPoint ~> controller.r
  pPoint ~> controller.p
  iPoint ~> controller.i
  dPoint ~> controller.d
  tank.level ~> controller.y
  controller.u ~> timedLoop ~> tank.effect
  tank.tap ~> chart.line("tap")
  tank.effect ~> chart.line("effect")
  tank.level ~> chart.line("level")



}
