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

package mathact.core.bricks.plumbing

import mathact.core.bricks.blocks.BlockLike
import mathact.core.bricks.plumbing.fitting.{Socket, Plug, OutflowLike, InflowLike}
import mathact.core.plumbing.fitting.{InPipe, OutPipe}


/** Contains definition for creating of inlets/outlets in object style
  * Created by CAB on 14.05.2016.
  */

trait ObjFitting { _: BlockLike ⇒
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
        throw new IllegalStateException(s"[Flange.getPipe] OutPipe not injected, look like Outlet not registered.")
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
  private def registerOutlet[H](out: Outflow[H], name: Option[String]): Plug[H] = Option(pump) match{
    case Some(p) ⇒ new OutPipe(out, name, p)
    case None ⇒ throw new IllegalStateException("[ObjFitting.registerOutlet] Pump not set.")}
  private def registerInlet[H](in: Inflow[H], name: Option[String]): Socket[H] = Option(pump) match{
    case Some(p) ⇒ new InPipe(in, name , p)
    case None ⇒ throw new IllegalStateException("[ObjFitting.registerInlet] Pump not set.")}
  //Registration if Outlet
  protected object Outlet{
    def apply[H](out: Outflow[H]): Plug[H] = registerOutlet(out, None)
    def apply[H](out: Outflow[H], name: String ): Plug[H] = registerOutlet(out, Some(name))}
  //Registration if Inlet
  protected object Inlet{
    def apply[H](in: Inflow[H]): Socket[H] = registerInlet(in, None)
    def apply[H](in: Inflow[H], name: String): Socket[H] = registerInlet(in, Some(name))}}
