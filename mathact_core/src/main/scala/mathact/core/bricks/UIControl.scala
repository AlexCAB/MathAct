package mathact.core.bricks

import mathact.core.plumbing.Fitting

/** Contain UI control methods
  * Created by CAB on 31.08.2016.
  */

trait UIControl { _: Fitting ⇒
  protected def onShowUI(): Unit
  private[mathact] def doShowUI(): Unit = onShowUI()
  protected def onHideUI(): Unit
  private[mathact] def doHideUI(): Unit = onHideUI()}
