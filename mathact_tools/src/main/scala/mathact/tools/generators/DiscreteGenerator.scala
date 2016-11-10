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

package mathact.tools.generators

import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStop, ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.tools.Tool

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.Includes._


/** Tool generate discrete timed events
  * Created by CAB on 10.11.2016.
  */

object DiscreteGenerator{
  //Definitions
  case class TimedEvent(
    time: Long)  //System time




}


abstract class DiscreteGenerator(implicit context: SketchContext)
extends Tool(context, "DG", "mathact/tools/generators/discrete_generator.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI{ import DiscreteGenerator._
  //UI definition
  private class GenUI extends SfxFrame{
    //Params
    title = "Discrete generator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    //Scene
    scene = new Scene{
      root = new HBox {
        prefWidth = 280
        children = new Button{
          text = "Say Hi!"
          onAction = handle{ sendEvent(???)}


        }}}
    //Commands reactions
    def onCommand = {
      case c ⇒

    }}
  //UI registration
  UI(new GenUI)



  //

  protected def onStart(): Unit = {
    println("FFFFFFFFFFFFFFFFFFFF")
  }

  protected def onStop(): Unit = {}

  private val outflow = new Outflow[TimedEvent]{


  }


  //Output
  val out = Outlet[TimedEvent](outflow)

}
