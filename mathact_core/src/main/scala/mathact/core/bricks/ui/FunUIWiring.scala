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

package mathact.core.bricks.ui

import mathact.core.bricks.plumbing.wiring.fun.FunWiring
import mathact.core.bricks.ui.interaction.{UIEvent, UICommand}
import mathact.core.sketch.blocks.BlockLike

import scala.language.implicitConversions


/** Inlets and outlets wiring in functional style
  * Created by CAB on 01.11.2016.
  */

trait FunUIWiring { _: BlockLike with FunWiring with BlockUI ⇒
  //Conversion
  protected implicit def ui2Flow(ui: UI.type): Flow[UICommand, UIEvent] = new Flow[UICommand, UIEvent]{
    ui.onEvent{ case e ⇒ push(e)}
    protected def pop(c: UICommand): Unit = ui.sendCommand(c)}
  protected implicit def ui2Source(ui: UI.type): Source[UIEvent] = new Source[UIEvent]{
    ui.onEvent{ case e ⇒ push(e)}}
  //DSL
  protected implicit class UIFlowDSL(ui: UI.type) {
    def map[H](f: UIEvent⇒H): Source[H] = ui2Source(ui).map(f)
    def foreach(f: UIEvent⇒Unit): Unit = ui2Source(ui).foreach(f)
    def unfold[O](f: UIEvent⇒Traversable[O]): Source[O] = ui2Source(ui).unfold(f)
    def filter(p: UIEvent⇒Boolean): Source[UIEvent] = ui2Source(ui).filter(p)
    def zipAll[H](s: Source[H]): Source[(UIEvent, H)] = ui2Source(ui).zipAll(s)
    def ?(default: UIEvent): Drain[UIEvent]⇒Option[UIEvent]⇒UIEvent = ui2Source(ui).?(default)
    def zipEach[H,R](other: Drain[H]⇒Option[H]⇒R): Source[(Option[UIEvent], R)] = ui2Source(ui).zipEach(other)}}
