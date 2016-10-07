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

package mathact.core.plumbing.fitting

import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.Pump


/** Base class for Outlet and Inlet
  * Created by CAB on 09.05.2016.
  */

private [mathact] trait Pipe[H]{
  //Variables
//  private var pump: Option[Pump] = None
//  private var pipeId: Option[Int] = None
//  private var pipeName: Option[String] = None
//  //Service methods
//  private[plumbing] def injectPump(pump: Pump, pipeId: Int, pipeName: String): Unit = this.pump match{
//    case Some(_) ⇒
//      this.pump.foreach(_.log.debug(s"[Pipe.injectPump] Pump is already injected to $this")) //For example one Pipe bind as several sockets and plugs
//    case None ⇒
//      pump.log.debug(s"[Pipe.injectPump] Injected to $this.")
//      this.pump = Some(pump)
//      this.pipeId = Some(pipeId)
//      this.pipeName = pipeName match{case "" ⇒ None; case s ⇒ Some(s)}}
  //
  //Abstract params
  protected val pump: Pump
  protected val toolId: Int
  protected val pipeId: Int
  protected val pipeName: Option[String]
  //Logging methods
  object log {
    def debug(msg: String): Unit = pump.log.debug(s"[$this] $msg")
    def info(msg: String): Unit = pump.log.info(s"[$this] $msg")
    def warning(msg: String): Unit = pump.log.warning(s"[$this] $msg")
    def error(msg: String): Unit = pump.log.error(s"[$this] $msg")}
  //Service methods
//  private[plumbing] def getPump: Pump = pump match{
//    case Some(p) ⇒ p
//    case None ⇒ throw new IllegalStateException("[Pipe.getPump] Pump not injected.")}
  def pushUserData(value: H): Unit = pump.pushUserMessage(M.UserData[H](pipeId, value))
  def connect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Unit = pump.connect(out, in)


//    (pump, pipeId) match{  //Return: (drive, pipe ID)
//    case (Some(p), Some(i)) ⇒ PipeData(p.drive, p.toolName, i, pipeName.getOrElse(this.toString))
//    case s ⇒ throw new IllegalStateException(s"[Pipe.getPump] Pump not injected, state: $s.")}

//
//  private[plumbing] def pushUserData(value: T): Unit = (pump, pipeId) match{
//    case (Some(p), Some(outletId)) ⇒ this match{
//      case _: Outlet[T] ⇒ p.pushUserMessage(M.UserData[T](outletId = outletId, value))
//      case t ⇒ p.log.error(s"[Pipe.pushUserData] This pipe is not an Outlet[T], class name ${this.getClass.getName}.")}
//    case s ⇒ throw new IllegalStateException(s"[Pipe.pushUserData] Pump not injected, state: $s.")}










  //  //Messaging methods
//  private[mathact] def connect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Unit = pump match{
//    case Some(p) ⇒
//
//    case None ⇒
//      throw new IllegalStateException("[Pipe.disconnect] Pump not injected.")
//
//  }
//
//
//
//  private[mathact] def disconnect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Unit = pump match{
//    case Some(p) ⇒
//
//    case None ⇒
//      throw new IllegalStateException("[Pipe.disconnect] Pump not injected.")
//
//  }


//  private[plumbing] def connectPlug(plug: ⇒Plug[T]): Unit = {}
//  private[plumbing] def disconnectPlug(plug: ⇒Plug[T]): Unit = {}
//  private[plumbing] def connectJack(socket: ⇒Socket[T]): Unit = {}
//  private[plumbing] def disconnectJack(socket: ⇒Socket[T]): Unit = {}






  //

  //!!! Здесь должны быть служебные методы для работы с Pump (которая должна остатся приватной по возможности),
  //    в частности метод отправки пользовательского сообщения по def pour(v).





}
