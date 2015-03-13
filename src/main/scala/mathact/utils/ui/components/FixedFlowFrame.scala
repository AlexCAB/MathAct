package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.ui.components.ToyComponent
import scala.swing._


/**
 * Flow frame with fixed size
 * Created by CAB on 11.03.2015.
 */

abstract class FixedFlowFrame(environment:Environment, windowTitle:String, components:List[Component with ToyComponent]) extends Frame {
  //Construction
  title = windowTitle
  private val panel = new FlowPanel(FlowPanel.Alignment.Center)()
  panel.vGap = 2
  panel.hGap = 1
  contents = panel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def show(defX:Int, defY:Int) = {
    //ToyComponent
//    val height = components.map(_.initHeight).max
//    components.map(c ⇒ c.setNewSize(c.initWidth, height))

    val height = components.map(_.preferredSize.getHeight).max
    components.map(c ⇒ c.preferredSize = new Dimension(c.preferredSize.getWidth.toInt, height.toInt))

    //Add components
    panel.contents ++= components
    //Show
    pack()
    visible = true
//    resizable = false //todo Brake layout
    //Locate
    location = environment.layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}
  def setTitleAdd(add:String) = {
    title = windowTitle + add}}
