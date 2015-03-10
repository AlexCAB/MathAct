package mathact.clockwork.ui.components
import mathact.clockwork.Clockwork
import scala.collection.mutable.{ListBuffer => MutList}
import scala.swing._


/**
 * Frame for replacing gridRow in one column
 * Created by CAB on 09.03.2015.
 */

abstract class GridFrame(clockwork:Clockwork, windowTitle:String) extends Frame {
  //Variables
  val componentsList = MutList[GridComponent]()
  //Construction
  title = windowTitle
  private val panel = new GridPanel(0,1)
  contents = panel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def add(components:List[GridComponent]) = {
    componentsList ++= components}
  def show() = {
    //Placing of gridRow
    componentsList.size match{
      case s if s != 0 ⇒ {
        //Calc Size
        val widths = componentsList.toList.map(_.gridRow.map(_._2.initWidth))
        val nCol = widths.map(_.size).max
        val colWidths = widths.map(l ⇒ l ++ (l.size until nCol).map(_ ⇒ 0)).transpose.map(_.max)
        val rowHeights = componentsList.toList.map(_.gridRow.map(_._2.initHeight)).map(_.max)
        //Layout
        panel.contents ++= rowHeights.zip(componentsList).map{case(rowHeight, comList) ⇒ {
          val components = colWidths.zip(comList.gridRow).map{case(colWidth, (component, alignment)) ⇒ {
            alignment.setNewSize(colWidth,rowHeight)
            component}}
          new BorderPanel{
            import BorderPanel.Position._
            layout(new FlowPanel{contents ++= components.init}) = West
            layout(components.last) = Center}}}}
      case _ ⇒ {
        preferredSize = new Dimension(300, 50)}}
    //Show
    pack()
    visible = true
    //Locate
    location = clockwork.layout.occupyLocation(size, location.x, location.y)}
  def hide() = {
    visible = false}}
