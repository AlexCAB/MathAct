package mathact.pots
import mathact.clockwork.{Clockwork, Gear}
import scala.collection.mutable.{ListBuffer ⇒ MutList}
import javax.swing.JSlider
import javax.swing.SwingConstants._
import javax.swing.event.{ChangeEvent, ChangeListener}
import scala.swing._
import Alignment._


/**
 * Interactive potentiometer board
 * Created by CAB on 08.03.2015.
 */

abstract class PotBoard
(x:Int = Int.MaxValue, y:Int = Int.MaxValue, defMin:Double = -1, defMax:Double = 1)
(implicit clockwork:Clockwork) {
  //Variables
  private var idCounter = 0
  private val pots = MutList[(Int,Double,Double,Double)]() //(ID, min, max, value)
  //Functions
  private def addPot(min:Double = defMin, max:Double = defMax, value:Double = 0):Int = {
    idCounter += 1
    pots += ((idCounter, min, max, 0.0))
    idCounter}
  //UI
  private val potBoardName = getClass.getCanonicalName match{
    case null ⇒ "PotBoard"
    case n ⇒ n.split("[.]").last.replace("$","")}
  private val panel = new GridPanel(0,1)
  private val frame = new Frame{
    override def closeOperation() {if(gear.work){clockwork.delGear(gear)}}
    peer.setDefaultCloseOperation(0)
    contents = panel
    title = potBoardName}
  //Methods
  def zero:Double = addPot()
  def in(min:Double, max:Double):Double = addPot(min, max)
  def init(value:Double):Double = addPot(value = value)
  implicit class DoubleEx(value:Double){
    def in(min:Double, max:Double):Double = addPot(min, max, value)}
  //Gear
  private val gear:Gear = new Gear{
    def start() = {


      //Show
      if(pots.size == 0){frame.preferredSize = new Dimension(300, 50)}
      frame.pack()
      frame.visible = true
      frame.location = clockwork.layout.occupyLocation(frame.size, x, y)}
    def tick() = {










    }
    def stop() = {
      frame.visible = false}}
  clockwork.addGear(gear)}
