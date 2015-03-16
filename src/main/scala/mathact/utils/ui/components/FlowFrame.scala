package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.ui.{UIParams, Layout}
import scala.swing._


/**
 * Flow frame
 * Created by CAB on 11.03.2015.
 */

abstract class FlowFrame(
  layout:Layout,
  uiParams:UIParams.FlowFrame,
  windowTitle:String,
  components:List[Component with UIComponent])
extends Frame {
  //Construction
  title = windowTitle
  private val panel = new FlowPanel(FlowPanel.Alignment.Center)()
  panel.background = uiParams.backgroundColor
  panel.vGap = 2
  panel.hGap = 1
  contents = panel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def show(defX:Int, defY:Int) = {
    val height = components.map(_.preferredSize.getHeight).max
    components.map(c ⇒ c.preferredSize = new Dimension(c.preferredSize.getWidth.toInt, height.toInt))
    //Add components
    panel.contents ++= components
    //Show
    pack()
    visible = true
//    resizable = false //todo Brake layout
    //Locate
    location = layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}
  def setTitleAdd(add:String) = {
    title = windowTitle + add}}
