package mathact.utils.clockwork


/**
 * Base trait for gear classes
 * Created by CAB on 08.03.2015.
 */

abstract class Gear(clockwork:Clockwork) {
  //Variables
  private var work:Boolean = false //Set after adding to Clockwork and reset after deleting.
  private var isChanged:Boolean = false //Set when component call changed() and reset when Clockwork call doUpdate()
  //Construction
  clockwork.gearCreated(this)
  //Abstract methods (should be implement in component)
  def start()
  def update()
  def stop()
  //Methods for component
  def changed() = if(work){         //Called by component when it changed
    isChanged = true
    clockwork.gearChanged(this)}
  def endWork() = if(work){         //Called by component when it end work
    stop()
    clockwork.gearStopped(this)
    work = false}
  //Methods for Clockwork
  def doStart() = if(! work){       //Called by Clockwork when component start
    work = true
    start()}
  def doUpdate() = if(work){        //Called by Clockwork when component need to be update
    if(! isChanged){update()}
    isChanged = false}
  def doStop() = if(work){          //Called by Clockwork when component stop
    stop()
    work = false}}
