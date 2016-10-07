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

import mathact.core.plumbing.Pump

/** Event collector
  * Created by CAB on 13.05.2016.
  */



trait Outlet[H] extends Incut{
  //Variables
  private var pipes = List[OutPipe[H]]()
  //Internal methods
  private[plumbing] def injectOutPipe(pipe: OutPipe[H]): Unit = {pipes +:= pipe}





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
  protected def pour(value: H): Unit = pipes match{
    case Nil ⇒
      throw new IllegalStateException(s"[Flange.getPipe] OutPipe not injected, look like Outlet not registered.")
    case ps ⇒
      ps.foreach(_.pushUserData(value))}


















  val testVal = 0

}


