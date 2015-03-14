package mathact.utils.ui.components
import mathact.utils.Environment
import scala.swing._


/**
 * Frame with border layout
 * Created by CAB on 12.03.2015.
 */

abstract class BorderFrame (
  environment:Environment,
  windowTitle:String,
  north:Option[Component with UIComponent] = None,
  south:Option[Component with UIComponent] = None,
  west:Option[Component with UIComponent] = None,
  east:Option[Component with UIComponent] = None,
  center:Option[Component with UIComponent] = None)
extends Frame {
  //Construction
  title = windowTitle
  private val panel = new BorderPanel
  contents = panel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def show(defX:Int, defY:Int, defW:Int, defH:Int) = {
    //Placing of components
    import BorderPanel.Position._
    val components = List((north, North),(south, South),(west, West),(east, East),(center, Center))
    components.filter{case(c,_) ⇒ c.nonEmpty}.map{case(c,p) ⇒ panel.layout(c.get) = p}
    //Calc size
    val sizes = components.map{
      case (Some(c),k) ⇒ (k, (c.preferredSize.getWidth.toInt, c.preferredSize.getHeight.toInt))
      case (_,k) ⇒ (k,(0,0))}.toMap
    val width = List(sizes(North)._1,sizes(South)._1,List(sizes(West),sizes(Center),sizes(East)).map(_._1).sum).max
    val height = List(sizes(West)._2,sizes(Center)._2,sizes(East)._2).max + sizes(North)._2 + sizes(South)._2
    //Show
    if(defW == Int.MaxValue && defH == Int.MaxValue){
      panel.preferredSize = new swing.Dimension(
        if(defW != Int.MaxValue) defW else width,
        if(defH != Int.MaxValue) defH else height)}
    pack()
    visible = true
    //Locate
    location = environment.layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}}