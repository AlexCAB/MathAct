package mathact.core.bricks.plumbing

/** Contain method to call on start
  * Created by CAB on 14.05.2016.
  */

private[mathact] trait OnStart { _: ObjFitting ⇒
  protected def onStart(): Unit
  private[core] def doStart(): Unit = onStart()}
