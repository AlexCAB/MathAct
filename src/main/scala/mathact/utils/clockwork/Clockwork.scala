package mathact.utils.clockwork


/**
 * Implementation ot internal machinery
 * Created by CAB on 08.03.2015.
 */

class Clockwork{
  //Variables
  private var visualisationGears = List[VisualisationGear]()
  private var calculationGears = List[CalculationGear]()
  private var unInitGears = List[Gear]()
  private var work = false
  //Functions
  private def initGears() = {
    try{
      unInitGears.foreach(_.doStart())
      visualisationGears ++= unInitGears
        .filter{case _:VisualisationGear ⇒ true; case _ ⇒ false}
        .map(_.asInstanceOf[VisualisationGear])
      calculationGears ++= unInitGears
        .filter{case _:CalculationGear ⇒ true; case _ ⇒ false}
        .map(_.asInstanceOf[CalculationGear])
        .sortBy(_.updatePriority)
      unInitGears = List()}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      System.exit(-1)}}}
  private def updateAllGears() = {
    initGears()
    try{
      calculationGears.foreach{
        case g if g.updatePriority >= 0 ⇒ g.doUpdate()
        case _ ⇒}
      visualisationGears.foreach(_.doUpdate())}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      stop(-1)}}}
  private def stopAllGears() = {
    (visualisationGears ++ calculationGears).foreach(g ⇒ {
      try{g.doStop()}catch{case e:Throwable ⇒ {e.printStackTrace()}}})}
  //Methods
  def start():Unit = {
    println("====== clockwork start =======")
    if(unInitGears.size == 0){
      println("No components found.")
      stop(0)}
    work = true
    initGears()
    updateAllGears()} //Firs update
  def stop(code:Int):Unit = {
    println("====== clockwork stop ========")
    work = false
    stopAllGears()
    System.exit(code)}
  //Gear methods
  def gearCreated(gear:Gear):Unit = {unInitGears +:= gear}
  def gearNeedUpdate(gear:Gear):Unit = {
    try{
      gear.doUpdate()}
    catch{case e:Throwable ⇒ {
      e.printStackTrace()
      System.exit(-1)}}}
  def gearChanged(gear:Gear):Unit = {updateAllGears()}
  def gearStopped(gear:Gear):Unit = {stop(0)}}
