package mathact.clockwork
import mathact.clockwork.ui.Layout
import scala.collection.mutable.{ListBuffer => MutList}


/**
 * Implementation ot internal machinery
 * Created by CAB on 08.03.2015.
 */

class Clockwork(val layout:Layout) {
  //Variables
  private val gears = MutList[Gear]()
  //Functions

  //Methods
  def start():Unit = {
    println("====== start =======")
    gears.toList.foreach(g ⇒ {
      g.work = true
      g.start()})
  }

  def stop():Unit = {
    gears.toList.foreach(g ⇒ {
      g.work = false
      gears -= g
      g.stop()})
    System.exit(0)

  }


  def addGear(gear:Gear):Unit = {gears += gear}
  def delGear(gear:Gear):Unit = {stop()}

}
