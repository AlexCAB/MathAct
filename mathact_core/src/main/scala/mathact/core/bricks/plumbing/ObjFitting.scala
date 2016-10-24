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
import mathact.core.bricks.plumbing.fitting.{Socket, Plug, OutletLike, InletLike}
import mathact.core.plumbing.fitting.{InPipe, OutPipe}

import scala.concurrent.Future


/** Contains definition for creating of inlets/outlets in object style
  * Created by CAB on 14.05.2016.
  */

trait ObjFitting { _: BlockLike ⇒
  //Definitions
  protected trait Outlet[T] extends OutletLike[T]{
    //Variables
    private var pipes = List[OutPipe[T]]()
    //Internal methods
    private[core] def injectOutPipe(pipe: OutPipe[T]): Unit = {pipes +:= pipe}





    //    this.pipe match{
    //    case Some(p) ⇒
    //      p.log.debug(s"[Flange.injectOutPipe] OutPipe is already injected to $this") //For example one Pipe bind as socket and plug
    //    case None ⇒
    //      pipe.log.debug(s"[Flange.injectPipe] Injected to $this.")
    //      this.pipe = Some(pipe)}
    //

    //
    //  private[plumbing] def getPipe: Pipe[H] = pipe match{
    //    case Some(p) ⇒ p
    //    case None ⇒ throw new IllegalStateException(s"[Flange.getPipe] Pipe not injected.")}
    //






    //Methods
    /** Send value to connected inlets
      * @param value - */
    protected def pour(value: T): Unit = pipes match{
      case Nil ⇒
        throw new IllegalStateException(s"[Flange.getPipe] OutPipe not injected, look like Outlet not registered.")
      case ps ⇒
        ps.foreach(_.pushUserData(value))}


















    val testVal = 0

  }

  protected trait Inlet[T] extends InletLike[T]{   //Методы обьявдены protected чтобы из не вызывали из вне, но пользователь может реализовть свой методв и оставить его доступным из вне

    private[core] def processValue(value: Any): Unit = drain(value.asInstanceOf[T])





    //  drain(value)


    protected def drain(value: T): Unit    //Вызыватеся каждый раз при получении нового значения из Flange



    //  protected def disconnect(flange: Flange[_]): Boolean = ???    //Отключение указаного Flange, true если было выполенео, false если не найдено
    //  protected def disconnectAll: Boolean = ???    //Отключение dct[ Flange, true если было выполенео, false если нет ни одного


    //??? Нужны ли методы ниже.
    protected def lastValue: Option[T] = ???      //Возвращает последнее полученое значение
    protected def nextValue: Future[T] = ???      //Ожыдание следующего значения




    //    println(in())









    //}
    //  def disconnect(in:()⇒Flange[Double]): Unit = {
    //
    //  ???
    //
    //}


  }










//  type Plug[T] = fitting.Plug[T]
//  type Socket[T] = fitting.Socket[T]
//  protected type Outlet[T] = fitting.Outlet[T]
//  protected type Inlet[T] = fitting.Inlet[T]




  //!!! Не забывать о том что инструменты могут динамически создаватся и разрушатся
  //Нужно добавить метод разрушения в Pump и сюда (метод регистрации не нужен, так как Pump регистрируется при создании).


//  protected object Collect{
//    def apply[T](in: Plug[T]*): Plug[T] = {
//
//
//      in.head
//    }
//
//  }
//
//  protected object Convert{
//    def apply[T,H](in: Plug[T])(transformer: T⇒H): Plug[H] = {     //Приобразователь типа событий
//
//        //!!!Нужна версия для нескольких входов, чтобы можно было агрегировать с их значения
//        //???Обдумать как синхронизоровать нескольхо входов (нужна проговая синхронизаия, и ваоление по каждому событию)
//        //Варианты синхронизации: 1) потоковая, 2) значения по умолчнию, 3) опциональные значения.
//
//        //Convert это статическае связание входов и выходов, нужно ещё динамическоке (чтобы можно было добвлять и удалять входы)
//
//
//      ???
//
//    }
//
//  }


  //Helper classes
  trait Filler[H]{ def fill(value: H): Unit }
  //Functions
  private def registerOutlet[H](out: Outlet[H], name: String): Plug[H] = Option(pump) match{
    case Some(p) ⇒
      new OutPipe(out, name match{ case "" ⇒ None; case n ⇒ Some(n) }, p)
    case None ⇒
      throw new IllegalStateException("[ObjFitting.registerOutlet] Pump not set.")}
  private def registerInlet[H](in: Inlet[H], name: String): Socket[H] = Option(pump) match{
    case Some(p) ⇒
      new InPipe(in, name match{ case "" ⇒ None; case n ⇒ Some(n) }, p)
    case None ⇒
      throw new IllegalStateException("[ObjFitting.registerInlet] Pump not set.")}


  //Registration if Outlet
  protected object Outlet{
    def apply[H](out: Outlet[H], name: String = ""): Plug[H] = registerOutlet(out, name)



//    def apply[H](name: String): (Plug[H], Filler[H]) = {
//      val out = new Filler[H] with Outlet[H]{ def fill(value: H): Unit = pour(value) }
//      Option(pump).foreach(p ⇒ out.injectPump(p, p.addOutlet(out, Some(name)),name))
//      (out, out)}
//    def apply[H]: (Plug[H], Filler[H]) = {
//      val out = new Filler[H] with Outlet[H]{ def fill(value: H): Unit = pour(value) }
//      Option(pump).foreach(p ⇒ out.injectPump(p, p.addOutlet(out, None), ""))
//      (out, out)}
  }
  //Registration if Inlet
  protected object Inlet{
    def apply[H](in: Inlet[H], name: String = ""): Socket[H] = registerInlet(in, name)

//
//    {
//      Option(pump).foreach(p ⇒ in.injectPump(p, p.addInlet(in, name match{case "" ⇒ None; case n ⇒ Some(n)}), name))
//      in}


//    def apply[H](name: String)(drainFun: H⇒Unit): Socket[H] = {
//      val in = new Inlet[H]{protected def drain(value: H): Unit = drainFun(value)}
//      Option(pump).foreach(p ⇒ in.injectPump(p, p.addInlet(in, Some(name)), name))
//      in}
//    def apply[H](drainFun: H⇒Unit): Socket[H] = {
//      val in = new Inlet[H]{protected def drain(value: H): Unit = drainFun(value)}
//      Option(pump).foreach(p ⇒ in.injectPump(p, p.addInlet(in, None), ""))
//      in}
     }
}
