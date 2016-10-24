package mathact.core.bricks.plumbing

/** Contain method to called on stop
  * Created by CAB on 14.05.2016.
  */

private[mathact] trait OnStop { _: ObjFitting ⇒
  protected def onStop(): Unit
  private[core] def doStop(): Unit = onStop()}
