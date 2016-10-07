package mathact.core.bricks

import mathact.core.plumbing.Fitting

/** Contain method to call on start
  * Created by CAB on 14.05.2016.
  */

trait OnStart { _: Fitting ⇒
  protected def onStart(): Unit
  private[mathact] def doStart(): Unit = onStart()}
