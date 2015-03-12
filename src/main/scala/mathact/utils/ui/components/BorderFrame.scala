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
  north:Option[Component],
  south:Option[Component],
  west:Option[Component],
  east:Option[Component],
  center:Option[Component])
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
    Seq((north, North),(south, South),(west, West),(east, East),(center, Center))
      .filter{case(c,_) ⇒ c.nonEmpty}
      .map{case(c,p) ⇒ panel.layout(c.get) = p}
    //Show
    if(defW == Int.MaxValue && defH == Int.MaxValue){
      panel.preferredSize = new swing.Dimension(
        if(defW != Int.MaxValue) defW else 200,
        if(defH != Int.MaxValue) defH else 100)}
    pack()
    visible = true
    //Locate
    location = environment.layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}}