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

package mathact.core.bricks.plumbing.wiring.obj

import mathact.core.bricks.plumbing.fitting.{Plug, Socket}
import mathact.core.model.enums.DequeueAlgo
import mathact.core.plumbing.fitting.flows.{InflowLike, OutflowLike}
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.BlockLike


/** Contains definition for creating of inlets/outlets in object style
  * Created by CAB on 14.05.2016.
  */

trait ObjWiring { _: BlockLike ⇒
  //Types
  protected trait ObjPlug[H] extends Plug[H] { _: OutPipe[H] ⇒ }
  protected trait ObjSocket[H] extends Socket[H] { _: InPipe[H] ⇒ }
  //Definitions
  /** Base trait for sender implementation */
  protected trait Outflow[T] extends OutflowLike[T]{
    //Variables
    private var pipes = List[OutPipe[T]]()
    //Internal API
    private[core] def injectOutPipe(pipe: OutPipe[T]): Unit = {pipes +:= pipe}
    //Methods
    /** Send value to all connected inlets
      * @param value - T value to be send, should be immutable*/
    protected def pour(value: T): Unit = pipes match{
      case Nil ⇒
        throw new IllegalStateException(s"[ObjWiring.Outflow.pour] OutPipe not injected, look like Outlet not registered.")
      case ps ⇒
        ps.foreach(_.pushUserData(value))}}
  /** Base trait for receiver implementation */
  protected trait Inflow[T] extends InflowLike[T]{
    //Variables
    @volatile private var lastReceivedValue: Option[T] = None
    //Internal API
    private[core] def processValue(value: Any): Unit = {
      val v = value.asInstanceOf[T]
      lastReceivedValue = Some(v)
      drain(v)}
    /** Callback, called each time when new value received
      * @param value - received value T */
    protected def drain(value: T): Unit
    /** Return last received value if it was received otherwise none. */
    protected def lastValue: Option[T] = lastReceivedValue}
  //Functions
  private def registerOutlet[H](out: Outflow[H], name: Option[String]): Plug[H] with ObjPlug[H] = Option(pump) match{
    case Some(p) ⇒ new OutPipe[H](out, name, p) with ObjPlug[H]
    case None ⇒ throw new IllegalStateException("[ObjWiring.registerOutlet] Pump not set.")}
  private def registerInlet[H](in: Inflow[H], name: Option[String], dequeue: DequeueAlgo)
  :Socket[H] with ObjSocket[H] = Option(pump) match{
    case Some(p) ⇒ new InPipe[H](in, name, p, dequeue) with ObjSocket[H]
    case None ⇒ throw new IllegalStateException("[ObjWiring.registerInlet] Pump not set.")}
  /** Registration if Outlet */
  protected object Outlet{
    def apply[H](out: Outflow[H]): Plug[H] with ObjPlug[H] = registerOutlet(out, None)
    def apply[H](out: Outflow[H], name: String ): Plug[H] with ObjPlug[H] = registerOutlet(out, Some(name))}
  /** Registration if Inlet */
  protected object Inlet{
    def apply[H](in: Inflow[H]): Socket[H] with ObjSocket[H] =
      registerInlet(in, None, DequeueAlgo.Queue)
    def apply[H](in: Inflow[H], name: String): Socket[H] with ObjSocket[H] =
      registerInlet(in, Some(name), DequeueAlgo.Queue)
    def apply[H](in: Inflow[H], dequeue: DequeueAlgo): Socket[H] with ObjSocket[H] =
      registerInlet(in, None, dequeue)
    def apply[H](in: Inflow[H], name: String, dequeue: DequeueAlgo): Socket[H] with ObjSocket[H] =
      registerInlet(in, Some(name), dequeue)}}
