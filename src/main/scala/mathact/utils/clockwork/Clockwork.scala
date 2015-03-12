package mathact.utils.clockwork


/**
 * Implementation ot internal machinery
 * Created by CAB on 08.03.2015.
 */

class Clockwork{
  //Variables
  private var gears = Set[Gear]()
  private var unInitGears = Set[Gear]()
  private var work = false
  //Functions
  private def initGears() = {
    try{
      unInitGears.foreach(_.doStart())
      gears ++= unInitGears
      unInitGears = Set()}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      System.exit(-1)}}}
  private def updateAllGears() = {
    initGears()
    try{
      gears.foreach(_.doUpdate())}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      stop(-1)}}}
  private def stopAllGears() = {
    gears.foreach(g ⇒ {
      gears -= g
      try{g.doStop()}catch{case e:Throwable ⇒ {e.printStackTrace()}}})}
  //Methods
  def start():Unit = {
    println("====== clockwork start =======")
    if(unInitGears.size == 0){
      println("No components found.")
      stop(0)}
    work = true
    initGears()}
  def stop(code:Int):Unit = {
    println("====== clockwork stop ========")
    work = false
    stopAllGears()
    System.exit(code)}
  //Gear methods
  def gearCreated(gear:Gear):Unit = {unInitGears += gear}
  def gearChanged(gear:Gear):Unit = {updateAllGears()}
  def gearStopped(gear:Gear):Unit = {stop(0)}}
