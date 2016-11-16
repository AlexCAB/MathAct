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

package mathact.tools.plots

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkIn
import mathact.core.bricks.plumbing.fitting.Socket
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStop, ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.TimedValue
import mathact.tools.Tool

import scalafx.scene.paint.Color


/** Simple Chart recorder
  * Created by CAB on 13.11.2016.
  */

class ChartRecorder(implicit context: BlockContext)
extends Tool(context, "CR", "mathact/tools/pots/chart_recorder.png")
  with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkIn[TimedValue]{


  //Definitions
  private class Line(name: String = "", color: Color) extends Inflow[TimedValue] {
    protected def drain(v: TimedValue): Unit = {


      logger.info("Line got: " + v)



    }

  }


  //Functions
  private def nextColor: Color = Color.Magenta


  //On start and on stop
  protected def onStart(): Unit = {} //{ UI.sendCommand(C.Start) }
  protected def onStop(): Unit = {} //{ UI.sendCommand(C.Stop) }


  //Inlets
  def in: Socket[TimedValue] = Inlet(new Line(name = "",color = nextColor))
  def line(name: String = "", color: Color = nextColor): Socket[TimedValue] = Inlet(new Line(name, color))}
