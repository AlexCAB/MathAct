package mathact.core.bricks

import mathact.core.plumbing.Fitting

/** Contain method to called on stop
  * Created by CAB on 14.05.2016.
  */

trait OnStop { _: Fitting ⇒
  protected def onStop(): Unit
  private[mathact] def doStop(): Unit = onStop()}
