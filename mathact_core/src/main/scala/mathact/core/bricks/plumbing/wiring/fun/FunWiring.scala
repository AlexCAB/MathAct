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

package mathact.core.bricks.plumbing.wiring.fun

import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.plumbing.fitting.flows.{OutflowLike, InflowLike}
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.BlockLike

import scala.language.implicitConversions


/** Contains definition for creating of inlets/outlets in functional style
  * Created by CAB on 24.10.2016.
  */

trait FunWiring { _: BlockLike ⇒
  //Definitions
  /** Internal socket mark type */
  protected trait FunSocket[H] extends Socket[H] { _: InPipe[H] ⇒ }
  /** Internal plug mark type */
  protected trait FunPlug[H] extends Plug[H] { _: OutPipe[H] ⇒ }
  /** Values  source */
  protected trait Source[T] {
    //Variables
    private var linkedDrains = List[Drain[T]]()
    //Functions
    private def addDrain[R <: Drain[T]](drain: R): R = {
      drain.link(this)
      linkedDrains +:= drain
      drain}
    //Flow methods
    protected def push(v: T): Unit = linkedDrains.foreach(_.pass(v))
    //Connection methods
    def next[D >: T, H](flow: Flow[D, H]): Source[H] = addDrain(flow)
    def next[D >: T](drain: Drain[D]): Unit = addDrain(drain)
    def >>[D >: T, H](flow: Flow[D, H]): Source[H] = addDrain(flow)
    def >>[D >: T](drain: Drain[D]): Unit = addDrain(drain)}
  /** Values drain */
  protected trait Drain[-H]{
    //Variables
    private var linkedSource: Option[Source[_]] = None
    //Flow methods
    protected def pop(v: H): Unit
    //Internal methods
    private[core] def link(drain: Source[_]): Unit = linkedSource match{
      case None ⇒
        linkedSource = Some(drain)
      case Some(d) ⇒
        throw new IllegalStateException(
          s"[FunWiring.Drain.link] Source[H] $d already set up, wiring graph can't be cyclic.")}
    private[core] def pass(v: H): Unit = pop(v)}
  /** Drain creation helper object */
  protected object Drain{
    def apply[H](proc: H⇒Unit): Drain[H] = new Drain[H]{ protected def pop(v: H): Unit = proc(v) }}
  /** Flow, compose Drain and Source */
  protected trait Flow[-H,T] extends Drain[H] with Source[T]
  /** Flow creation helper object */
  protected object Flow{
    def apply[H,T](proc: (H,T⇒Unit)⇒Unit): Flow[H,T] = new Flow[H,T]{
      protected def pop(v: H): Unit = proc(v, r ⇒ push(r))}}
  /** Inflow to Source adapter */
  private class Inflow[H] extends InflowLike[H] with Source[H]{
    private[core] def processValue(v: Any): Unit = push(v.asInstanceOf[H])}
  /** Drain to Outflow adapter */
  private class Outflow[H] extends OutflowLike[H] with Drain[H]{
    //Variables
    @volatile private var pipe: Option[OutPipe[H]] = None
    //Internal methods
    private[core] def injectOutPipe(p: OutPipe[H]): Unit = { pipe = Some(p) }
    override def link(drain: Source[_]): Unit = {}
    //Flow methods
    protected def pop(v: H): Unit = pipe match{
      case Some(p) ⇒ p.pushUserData(v)
      case _ ⇒ throw new IllegalStateException(s"[FunWiring.Outflow.pop] OutPipe not injected.")}}
  //Variables
  @volatile private var inflowsMap = Map[Int, Inflow[_]]() //(Inlet ID, Inflow)
  @volatile private var outflowsMap = Map[Int, Outflow[_]]() //(Outlet ID, Outflow)
  //Conversion
  protected implicit def socket2Source[T](socket: FunSocket[T]): Source[T] = socket match{
    case in: InPipe[_] ⇒ inflowsMap(in.inletId).asInstanceOf[Source[T]]
    case _ ⇒ throw new IllegalArgumentException(s"[FunWiring.Socket2Source] $socket is not an instance of InPipe[T].")}
  protected implicit def plug2Drain[T](plug: FunPlug[T]): Drain[T] = plug match{
    case out: OutPipe[_] ⇒ outflowsMap(out.outletId).asInstanceOf[Drain[T]]
    case _ ⇒ throw new IllegalArgumentException(s"[FunWiring.Socket2Source] $plug is not an instance of OutPipe[T].")}
  protected implicit def source2DefDrain[T](source: Source[T]): Drain[T]⇒Option[T]⇒Option[T] = drain ⇒ {
    source.next(drain)
    v ⇒ v}
  protected implicit def socket2DefDrain[T](socket: FunSocket[T]): Drain[T]⇒Option[T]⇒Option[T] =
    source2DefDrain(socket2Source(socket))
  //DSL
  /** Buffer, store input values and on all logic */
  private class BufferOnAll[T,H](sourceT: Source[T], sourceH: Source[H]) extends Source[(T, H)]{
    //Variables
    @volatile private var valueT: Option[T] = None
    @volatile private var valueH: Option[H] = None
    //Functions
    private def checkReady(): Option[(T, H)] = (valueT, valueH) match{
      case (Some(t), Some(h)) ⇒
        valueT = None
        valueH = None
        Some(Tuple2(t,h))
      case _ ⇒
        None}
    private def setT(v: T): Option[(T, H)] = synchronized{
      valueT = Some(v)
      checkReady()}
    private def setH(v: H): Option[(T, H)] = synchronized{
      valueH = Some(v)
      checkReady()}
    //Drains
    sourceT.next(Drain[T]{ v ⇒ setT(v).foreach(r ⇒ push(r)) })
    sourceH.next(Drain[H]{ v ⇒ setH(v).foreach(r ⇒ push(r)) })}
  /** Buffer, store input values and on each logic */
  private class BufferOnEach[T,H,R,S](srcT: Drain[T]⇒Option[T]⇒R, srcH: Drain[H]⇒Option[H]⇒S) extends Source[(R, S)]{
    //Variables
    @volatile private var valueT: Option[T] = None
    @volatile private var valueH: Option[H] = None
    //Drains
    private val toR = srcT(Drain[T]{ v ⇒
      valueT = Some(v)
      doPush()})
    private val toS = srcH(Drain[H]{ v ⇒
      valueH = Some(v)
      doPush()})
    //Functions
    private def doPush(): Unit = push(Tuple2(toR(valueT), toS(valueH)))}
  /** Zip all helpers object */
  protected object ZipAll{
    def apply[T,H](srcT: Source[T], srcH: Source[H]):  Source[(T, H)] =
      new BufferOnAll(srcT, srcH)}
  /** Zip each helpers object */
  protected object ZipEach{
    def apply[T,H,R,S](srcT: Drain[T]⇒Option[T]⇒R, srcH: Drain[H]⇒Option[H]⇒S): Source[(R, S)] =
      new BufferOnEach(srcT, srcH)}
  /** Flows DSL methods*/
  protected implicit class FlowDSL[T](source: Source[T]) {
    def map[H](f: T⇒H): Source[H] =
      source.next(Flow[T,H]{ (v, p) ⇒ p(f(v)) })
    def foreach(f: T⇒Unit): Unit =
      source.next(Flow[T,Unit]{ (v, _) ⇒ f(v) })
    def unfold[H](f: T⇒Traversable[H]): Source[H] =
      source.next(Flow[T,H]{ (v, p) ⇒ f(v).foreach(r ⇒ p(r)) })
    def filter(c: T⇒Boolean): Source[T] =
      source.next(Flow[T,T]{ (v, p) ⇒ if(c(v)) p(v) })
    def zipAll[H](other: Source[H]): Source[(T, H)] =
      new BufferOnAll[T,H](source, other)
    def ?(default: T): Drain[T]⇒Option[T]⇒T = drain ⇒ {
      source.next(drain)
      v ⇒ v.getOrElse(default)}
    def zipEach[H,R](other: Drain[H]⇒Option[H]⇒R): Source[(Option[T], R)] =
      new BufferOnEach(source2DefDrain(source),other)}
  /** Source with default DSL methods*/
  protected implicit class SourceWithDefaultDSL[T,R](source: Drain[T]⇒Option[T]⇒R) {
    def zipEach[H,S](other: Drain[H]⇒Option[H]⇒S): Source[(R, S)] =
      new BufferOnEach(source, other)}
  /** Socket DSL, the same as FlowDSL but with conversion */
  protected implicit class FunSocketDSL[T](socket: FunSocket[T]) {
    def map[H](f: T⇒H): Source[H] = socket2Source(socket).map(f)
    def foreach(f: T⇒Unit): Unit = socket2Source(socket).foreach(f)
    def unfold[O](f: T⇒Traversable[O]): Source[O] = socket2Source(socket).unfold(f)
    def filter(p: T⇒Boolean): Source[T] = socket2Source(socket).filter(p)
    def zipAll[H](s: Source[H]): Source[(T, H)] = socket2Source(socket).zipAll(s)
    def ?(default: T): Drain[T]⇒Option[T]⇒T = socket2Source(socket).?(default)
    def zipEach[H,R](other: Drain[H]⇒Option[H]⇒R): Source[(Option[T], R)] = socket2Source(socket).zipEach(other)}
  //Objects
  /** Registration of Inlet */
  protected object In{
    //Functions
    private def newInlet[H](name: Option[String]): Socket[H] with FunSocket[H] = Option(pump) match{
      case Some(p) ⇒
        val inflow = new Inflow[H]
        val inPipe = new InPipe[H](inflow, name , p) with FunSocket[H]
        inflowsMap += (inPipe.inletId → inflow)
        inPipe
      case None ⇒
        throw new IllegalStateException("[FunWiring.newInlet] Pump not created.")}
    //Methods
    def apply[H]: Socket[H] with FunSocket[H] = newInlet(None)
    def apply[H](name: String): Socket[H] with FunSocket[H] = newInlet(Some(name))}
  /** Registration of Outlet */
  protected object Out{
    //Functions
    private def newOutlet[H](name: Option[String]): Plug[H] with FunPlug[H] = Option(pump) match{
        case Some(p) ⇒
          val outflow = new Outflow[H]
          val outPipe = new OutPipe[H](outflow, name, p) with FunPlug[H]
          outflowsMap += (outPipe.outletId → outflow)
          outPipe
        case None ⇒
          throw new IllegalStateException("[FunWiring.newOutlet] Pump not created.")}
    //Methods
    def apply[H]: Plug[H] with FunPlug[H] = newOutlet(None)
    def apply[H](name: String): Plug[H] with FunPlug[H] = newOutlet(Some(name))}}
