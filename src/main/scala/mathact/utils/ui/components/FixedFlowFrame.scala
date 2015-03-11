package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.ui.Alignment
import scala.swing._


/**
 * Flow frame with fixed size
 * Created by CAB on 11.03.2015.
 */

abstract class FixedFlowFrame(environment:Environment, windowTitle:String, components:List[Component with Alignment]) extends Frame {
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
    //Alignment
    val height = components.map(_.initHeight).max
    println(height)
    components.map(c ⇒ c.setNewSize(c.initWidth, height))
    //Add components
    panel.contents ++= components
    //Show
    resizable = false
    visible = true
    pack()
    //Locate
    location = environment.layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}
  def setTitleAdd(add:String) = {
    title = windowTitle + add}}
