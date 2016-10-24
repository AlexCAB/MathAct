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

/** Base trite for Plug and Socket.
  * Created by CAB on 13.05.2016.
  */

private[core] trait Flange[H]{
//  //Variables
//  private var pipe: Option[Pipe[H]] = None
//  //Internal methods
//  private[plumbing] def injectPipe(pipe: Pipe[H]): Unit = this.pipe match{
//    case Some(p) ⇒
//      p.log.debug(s"[Flange.injectPipe] Pipe is already injected to $this") //For example one Pipe bind as socket and plug
//    case None ⇒
//      pipe.log.debug(s"[Flange.injectPipe] Injected to $this.")
//      this.pipe = Some(pipe)}
//  private[plumbing] def getPipe: Pipe[H] = pipe match{
//    case Some(p) ⇒ p
//    case None ⇒ throw new IllegalStateException(s"[Flange.getPipe] Pipe not injected.")}








  //!!! Аргубенты должны быть как socket: ⇒Socket[T], для поддержки перехрёстного связания инструментов
//
//  def connect(connector: ⇒Flange[T]): Unit = {
//
////    (this, connector) match{
////      case (s: Socket[T], p: Plug[T]) ⇒
////
////
////
////      case (p: Plug[T], s: Socket[T]) ⇒
////
////
////      case (c1,c2) ⇒ log.error(
////        s"[Flange.connect] Only Socket-Plug and Socket-Plug connecting acceptable, " +
////        s"currently connector 1: $c1, and connector 2: $c2")}
//
//
//  }
//
//
//
//
//
//
//
//
//  def disconnect(connector: ⇒Flange[T]): Unit = {
//
//
//
//
//
//
//
//  }




}