package mathact.clockwork

/**
 * Base trait for gear classes
 * Created by CAB on 08.03.2015.
 */

trait Gear {
  var work:Boolean = false //Set after adding to Clockwork and reset after deleting.
  def start()
  def tick()
  def stop()}
