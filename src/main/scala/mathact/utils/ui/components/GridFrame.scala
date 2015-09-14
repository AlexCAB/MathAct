package mathact.utils.ui.components
import mathact.utils.ui.{UIParams, Layout}
import scala.swing._


/**
 * Frame for replacing gridRow in one column
 * Created by CAB on 09.03.2015.
 */

abstract class GridFrame(
  layout:Layout,
  uiParams:UIParams.GridFrame,
  windowTitle:String)
extends Frame {
  //Variables
  var componentsList = List[GridComponent]()
  //Construction
  title = windowTitle
  private val panel = new GridPanel(0,1)
  panel.background = uiParams.backgroundColor
  contents = panel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def add(components:List[GridComponent]) = {
    componentsList ++= components}
  def show(defX:Int, defY:Int) = {
    //Placing of gridRow
    componentsList.size match{
      case s if s != 0 ⇒ {
        //Calc Size
        val widths = componentsList.map(_.gridRow.map(_.preferredSize.getWidth.toInt))
        val nCol = widths.map(_.size).max
        val colWidths = widths.map(l ⇒ l ++ (l.size until nCol).map(_ ⇒ 0)).transpose.map(_.max)
        val rowHeights = componentsList.toList.map(_.gridRow.map(_.preferredSize.getHeight.toInt)).map(_.max)
        //Layout
        panel.contents ++= rowHeights.zip(componentsList).map{case(rowHeight, comList) ⇒ {
          val components = colWidths.zip(comList.gridRow).map{case(colWidth, component) ⇒ {
            component.preferredSize = new Dimension(colWidth,rowHeight)
            component}}
          new BorderPanel{
            import BorderPanel.Position._
            uiParams.border.foreach{case (c,i) ⇒ border = Swing.LineBorder(c, i)}
            background = uiParams.backgroundColor
            layout(new FlowPanel{
              background = uiParams.backgroundColor
              contents ++= components.init}) = West
            layout(components.last) = Center}}}}
      case _ ⇒ {
        preferredSize = new Dimension(300, 50)}}
    //Show
    pack()
    visible = true
    //Locate
    location = layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}}
