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
  protected trait FunPlug[H]
  /** */
  protected trait FunSocket[H]

//  /** Base trait for sender implementation */
//  protected trait Outflow[T] extends OutflowLike[T]{
//    //Variables
//    private var pipes = List[OutPipe[T]]()
//    //Internal API
//    private[core] def injectOutPipe(pipe: OutPipe[T]): Unit = {pipes +:= pipe}
//    //Methods
//    /** Send value to all connected inlets
//      * @param value - T value to be send, should be immutable*/
//    protected def pour(value: T): Unit = pipes match{
//      case Nil ⇒
//        throw new IllegalStateException(s"[Flange.getPipe] OutPipe not injected, look like Outlet not registered.")
//      case ps ⇒
//        ps.foreach(_.pushUserData(value))}}



  /** */
  private class Inflow[H] extends InflowLike[H]{
    //New flow
    private[core] val flow = new Flow[H]{}
    //Methods
    private[core] def processValue(v: Any): Unit = flow.pushOn(v.asInstanceOf[H])}


  /** */
  private class Outflow[H] extends OutflowLike[H]{
    //Variables
    @volatile private var pipe: Option[OutPipe[H]] = None
    //Methods
    def injectOutPipe(p: OutPipe[H]): Unit = { pipe = Some(p) }
    def pour(v: H): Unit = pipe match{
      case Some(p) ⇒ p.pushUserData(v)
      case _ ⇒ throw new IllegalStateException(s"[FunWiring.Outflow.pour] OutPipe not injected.")}}



  /** */
  private[core] abstract class Mapper[T, H] extends Flow[H]{ def onPush(v: T): Unit }


  /** */
  private[core] class BufferAll[H,O] extends Flow[(H,O)]{
    //Variables
    @volatile protected var vh: Option[H] = None
    @volatile protected var vo: Option[O] = None
    //Functions
    private def doPushOn(): Unit = (vh, vo) match{
      case (Some(h), Some(o)) ⇒
        pushOn(Tuple2(h,o))
        vh = None
        vo = None
      case _ ⇒}
    //Mappers
    val hMapper = new Mapper[H,Unit]{ def onPush(v: H): Unit = {
      vh = Some(v)
      doPushOn()}}
    val oMapper = new Mapper[O,Unit]{ def onPush(v: O): Unit = {
      vo = Some(v)
      doPushOn()}}}




  /** */
  private[core] class BufferEach[H,O] extends Flow[(Option[H],Option[O])]{
    //Variables
    @volatile protected var vh: Option[H] = None
    @volatile protected var vo: Option[O] = None
    //Functions
    private def doPushOn(): Unit = pushOn(Tuple2(vh, vo))
    //Mappers
    val hMapper = new Mapper[H,Unit]{ def onPush(v: H): Unit = {
      vh = Some(v)
      doPushOn()
    } }
    val oMapper = new Mapper[O,Unit]{ def onPush(v: O): Unit = {
      vo = Some(v)
      doPushOn()
    } }


   }




  //Variables
  @volatile private var inflowsMap = Map[Int, Inflow[_]]() //(Inlet ID, Inflow)
  @volatile private var outflowsMap = Map[Int, Outflow[_]]() //(Outlet ID, Outflow)



  //DSL

  protected trait FlowWithDefault[H]{


  }

  /** */
  protected trait Flow[H] {
    //Variables
    @volatile private[this] var mappers = List[Mapper[H,_]]()
    //Functions
    private[core] def addMapper[O](mapper: Mapper[H,O]): Flow[O] = {
      mappers +:= mapper
      mapper}
    //Methods
    private[core] def pushOn(v: H): Unit = mappers.foreach(_.onPush(v))
    //DSL

    def to(out1: FunPlug[H], outs: FunPlug[H]*): Unit = addMapper( new Mapper[H,H] {
      val outlets = (out1 +: outs).map{
        case out: OutPipe[_] ⇒ outflowsMap(out.outletId).asInstanceOf[Outflow[H]]
        case out ⇒ throw new IllegalArgumentException(s"[Flow.to] $out is not an instance of OutPipe[T].")}
      def onPush(v: H): Unit = outlets.foreach(_.pour(v))})

    def map[O](f: H⇒O): Flow[O] =
      addMapper(new Mapper[H,O]{ def onPush(v: H): Unit = pushOn(f(v))})

    def foreach(f: H⇒Unit): Unit =
      addMapper( new Mapper[H,Unit]{ def onPush(v: H): Unit = f(v)})

    def unfold[O](f: H⇒Traversable[O]): Flow[O] =
      addMapper( new Mapper[H,O]{ def onPush(v: H): Unit = f(v).foreach(e ⇒ pushOn(e))})

    def filter(p: H⇒Boolean): Flow[H] =
      addMapper( new Mapper[H,H]{ def onPush(v: H): Unit = if(p(v)) pushOn(v)})


    def flatMapAll[O](flow: Flow[O]): Flow[(H, O)] = {
      val mapper = new BufferAll[H, O]
      addMapper(mapper.)
      flow.addMapper(new Mapper[O,Unit]{ def onPush(v: O): Unit = mapper.onPushO(v) })
      mapper}


    def flatMapEach[O](flow: Flow[O]): Flow[(Option[H], Option[O])] = {
      val mapper = new FlatMapperEach[H, O]
      addMapper(new Mapper[H,Unit]{ def onPush(v: H): Unit = mapper.onPushH(v) })
      flow.addMapper(new Mapper[O,Unit]{ def onPush(v: O): Unit = mapper.onPushO(v) })
      mapper}



    def flatMapEach[O](flow: FlowWithDefault[O]): Flow[(Option[H],O)] = ???











  }























  //Conversion
  protected implicit def Socket2Flow[T](socket: FunSocket[T]): Flow[T] = socket match{
    case in: InPipe[_] ⇒ inflowsMap(in.inletId).asInstanceOf[Inflow[T]].flow
    case _ ⇒ throw new IllegalArgumentException(s"[FunWiring.Socket2Flow] $socket is not an instance of InPipe[T].")}



  //DSL

//
//  implicit protected class SocketWithDefaultEx[T](socket: FunSocket[T]){
//
//    def flatMapEach[O](o: FunSocket[O]): FunSocket[(T,Option[O])] = ???
//
//    def flatMapEach[O](o: SocketWithDefault[O]): FunSocket[(T,O)] = ???
//
//
//  }
//
//
//
//
//
//  implicit protected class FunSocketEx[T](socket: FunSocket[T]){
//

//
//
//
//
//
//
//    def flatMapEach[O](o: FunSocket[O]): FunSocket[(Option[T],Option[O])] = ???
//
//    def flatMapEach[O](o: SocketWithDefault[O]): FunSocket[(Option[T], O)] = ???
//
//
//
//
//
//
//    def ?[H](default: H): SocketWithDefault[H] = ???
//
//
//  }







  /** Registration if Outlet */
  protected object Outlet{
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
    def apply[H](name: String): Plug[H] with FunPlug[H] = newOutlet(Some(name))}
  /** Registration if Inlet */
  protected object Inlet{
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
    def apply[H](name: String): Socket[H] with FunSocket[H] = newInlet(Some(name))}}
