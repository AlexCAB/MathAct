package mathact.core.bricks.plumbing.fitting

import mathact.core.plumbing.fitting.{Flange, InPipe}

/** Event receiver must be implemented by Inlet
  * Created by CAB on 17.05.2016.
  */

trait Socket[H] extends Flange[H] { _: InPipe[H] ⇒
  //Methods
  /** Connecting of this Socket to given Plug
    * @param plug - Plug[T] */
  def plug(plug: ⇒Plug[H]): Unit = pump.connect(()⇒plug, ()⇒this)
  def <~ (plug: ⇒Plug[H]): Unit = pump.connect(()⇒plug, ()⇒this)}
