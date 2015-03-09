package mathact.clockwork.ui
import mathact.clockwork.Clockwork
import scala.swing._


/**
 * Frame for replacing components in one column
 * Created by CAB on 09.03.2015.
 */

class OneColumnFrame(clockwork:Clockwork) extends Frame {
  //Construction
  private val panel = new GridPanel(0,1)
  contents = panel
  peer.setDefaultCloseOperation(0)
  //Methods
  def add(components:List[Component]) = {panel.contents ++= components}
  def show(defX:Int,defY:Int) = {
    if(panel.contents.size == 0){preferredSize = new Dimension(300, 50)}
    pack()
    visible = true
    location = clockwork.layout.occupyLocation(size, defX, defY)}
  def hide() = {visible = false}}
