package mathact.utils.clockwork
import scala.collection.mutable.{ListBuffer => MutList}


/**
 * Implementation ot internal machinery
 * Created by CAB on 08.03.2015.
 */

class Clockwork{
  //Variables
  private val gears = MutList[Gear]()
  //Functions

  //Methods
  def start():Unit = {
    println("====== start =======")
    try{
      gears.toList.foreach(g ⇒ {
        g.work = true
        g.start()})}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      System.exit(-1)


    }}

//
//    Thread.sleep(2000)
//    gears.toList.foreach(g ⇒ {g.tick()})

  }

  def stop():Unit = {
    gears.toList.foreach(g ⇒ {
      g.work = false
      gears -= g
      g.stop()})
    System.exit(0)

  }


  def addGear(gear:Gear):Unit = {gears += gear}
  def gearChanged(gear:Gear):Unit = {gears.foreach(_.doUpdate())}
  def delGear(gear:Gear):Unit = {stop()}

}
