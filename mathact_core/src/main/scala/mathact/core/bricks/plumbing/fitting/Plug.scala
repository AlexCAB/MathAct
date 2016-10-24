package mathact.core.bricks.plumbing.fitting

import mathact.core.plumbing.fitting.{Flange, OutPipe}

/** Source of events, must be implemented by Outlet
  * Created by CAB on 17.05.2016.
  */

trait Plug[H] extends Flange[H] { _: OutPipe[H] ⇒
//  //Get Outlet
//  private val outlet = this match{
//    case out: Outlet[T] ⇒ out
//    case _ ⇒ throw new Exception(
//      s"[Plug] This trait must be implemented only with mathact.core.plumbing.fitting.Outlet, " +
//      s"found implementation: ${this.getClass.getName}")}
  //Methods
  /** Connecting of this Plug to given Socket
    * @param socket - Socket[T] */
  def attach(socket: ⇒Socket[H]): Unit = pump.connect(()⇒this, ()⇒socket)}
