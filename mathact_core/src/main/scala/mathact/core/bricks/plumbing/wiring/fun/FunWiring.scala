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

import mathact.core.bricks.blocks.BlockLike
import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.bricks.plumbing.wiring.{InflowLike, OutflowLike}
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.language.implicitConversions

/** Contains definition for creating of inlets/outlets in functional style
  * Created by CAB on 24.10.2016.
  */

trait FunWiring { _: BlockLike ⇒

  //Definitions
  /** */
  protected trait FunSocket[H]

  /** */
  protected trait FunPlug[H]




  /** */
  protected trait Source[T] {
    //Variables
    private var linkedDrains = List[Drain[T]]()
    //Flow methods
    protected def push(v: T): Unit = linkedDrains.foreach(_.pass(v))
    //Connection methods
    def next[H](flow: Flow[T, H]): Source[H] = {
      flow.link(this)
      linkedDrains +:= flow
      flow}
    def next(drain: Drain[T]): Unit = {
      drain.link(this)
      linkedDrains +:= drain}
    def >>[H](flow: Flow[T, H]): Source[H] = next(flow)
    def >>(drain: Drain[T]): Unit = next(drain)}


  /** */
  protected trait SourceWithDefault[T] extends Source[T]{


  }


  /** */
  protected trait Drain[H]{
    //Variables
    private var linkedSource: Option[Source[H]] = None
    //Flow methods
    protected def pop(v: H): Unit
    //Internal methods
    private[core] def link(drain: Source[H]): Unit = linkedSource match{
      case None ⇒
        linkedSource = Some(drain)
      case Some(d) ⇒
        throw new IllegalStateException(
          s"[FunWiring.Drain.link] Source[H] $d already set up, wiring graph can't be cyclic.")}
    private[core] def pass(v: H): Unit = pop(v)

   }



  /** */
  protected trait Flow[H,T] extends Drain[H] with Source[T]





  /** */
  private class Inflow[H] extends InflowLike[H] with Source[H]{
    private[core] def processValue(v: Any): Unit = push(v.asInstanceOf[H])}


  /** */
  private class Outflow[H] extends OutflowLike[H] with Drain[H]{
    //Variables
    @volatile private var pipe: Option[OutPipe[H]] = None
    //Internal methods
    private[core] def injectOutPipe(p: OutPipe[H]): Unit = { pipe = Some(p) }
    private[core] override def link(drain: Source[H]): Unit = {}
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
  protected implicit def slug2Drain[T](plug: FunPlug[T]): Drain[T] = plug match{
    case out: OutPipe[_] ⇒ outflowsMap(out.outletId).asInstanceOf[Drain[T]]
    case _ ⇒ throw new IllegalArgumentException(s"[FunWiring.Socket2Source] $plug is not an instance of OutPipe[T].")}



  //DSL






  /** */
  protected implicit class FlowDSL[T](source: Source[T]) {





     def map[H](f: T⇒H): Source[H] = ???




//      addMapper(new Mapper[H,O]{ def onPush(v: H): Unit = pushOn(f(v))})



    def foreach(f: T⇒Unit): Unit = ???

//      addMapper( new Mapper[H,Unit]{ def onPush(v: H): Unit = f(v)})

    def unfold[O](f: T⇒Traversable[O]): Source[O] = ???

//      addMapper( new Mapper[H,O]{ def onPush(v: H): Unit = f(v).foreach(e ⇒ pushOn(e))})

    def filter(p: T⇒Boolean): Source[T] = ???

//      addMapper( new Mapper[H,H]{ def onPush(v: H): Unit = if(p(v)) pushOn(v)})


    def zipAll[H](source: Source[H]): Source[(T, H)] = ???


//    {
////      val mapper = new BufferAll[H, O]
////      addMapper(mapper.)
////      flow.addMapper(new Mapper[O,Unit]{ def onPush(v: O): Unit = mapper.onPushO(v) })
////      mapper
//    ???}


    def zipEach[H](source: Source[H]): Source[(Option[T], Option[H])] = ???

//    {
////      val mapper = new FlatMapperEach[H, O]
////      addMapper(new Mapper[H,Unit]{ def onPush(v: H): Unit = mapper.onPushH(v) })
////      flow.addMapper(new Mapper[O,Unit]{ def onPush(v: O): Unit = mapper.onPushO(v) })
////      mapper
//    ???}



    def zipEach[H](source: SourceWithDefault[H]): Source[(Option[T],H)] = ???




    def ?(default: T): SourceWithDefault = ???








  }


  protected implicit class SourceWithDefaultDSL[T](source: SourceWithDefault[T]) {


    def zipEach[H](source: Source[H]): Source[(Option[T], Option[H])] = ???

    def zipEach[H](source: SourceWithDefault[H]): Source[(Option[T],H)] = ???


  }







  /** Socket DSL, the same as FlowDSL but with conversion */
  protected implicit class FunSocketDSL[T](socket: FunSocket[T]) {
    def map[H](f: T⇒H): Source[H] = socket2Source(socket).map(f)
    def foreach(f: T⇒Unit): Unit = socket2Source(socket).foreach(f)
    def unfold[O](f: T⇒Traversable[O]): Source[O] = socket2Source(socket).unfold(f)
    def filter(p: T⇒Boolean): Source[T] = socket2Source(socket).filter(p)
    def zipAll[H](s: Source[H]): Source[(T, H)] = socket2Source(socket).zipAll(s)
    def zipEach[H](s: Source[H]): Source[(Option[T], Option[H])] = socket2Source(socket).zipEach(s)
    def zipEach[H](s: SourceWithDefault[H]): Source[(Option[T],H)] = socket2Source(socket).zipEach(s)
    def ?(d: T): SourceWithDefault = socket2Source(socket).?(d)}




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
