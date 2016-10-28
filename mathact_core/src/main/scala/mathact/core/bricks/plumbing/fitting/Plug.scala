package mathact.core.bricks.plumbing.fitting

import mathact.core.plumbing.fitting.{Flange, OutPipe}

/** Source of events, must be implemented by Outlet
  * Created by CAB on 17.05.2016.
  */

trait Plug[H] extends Flange[H] { _: OutPipe[H] ⇒
  //Methods
  /** Connecting of this Plug to given Socket
    * @param socket - Socket[T] */
  def attach(socket: ⇒Socket[H]): Unit = pump.connect(()⇒this, ()⇒socket)
  def ~> (socket: ⇒Socket[H]): Unit = pump.connect(()⇒this, ()⇒socket)}
