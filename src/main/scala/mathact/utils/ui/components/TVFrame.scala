package mathact.utils.ui.components
import mathact.utils.ui.{UIParams, Layout}
import scala.swing.{FlowPanel, BorderPanel, Frame, Component}
import BorderPanel.Position._

/**
 * One big component in the center and several at bottom in line
 * Created by CAB on 09.09.2015.
 */

abstract class TVFrame (
  layout:Layout,
  uiParams:UIParams.TVFrame,
  windowTitle:String,
  center:Option[Component with UIComponent] = None,
  bottom:List[Component with UIComponent] = List())
extends Frame {
  //Construction
  title = windowTitle
  private val flowPanel = new FlowPanel(FlowPanel.Alignment.Left)(){
    vGap = 2
    hGap = 2
  }
  private val borderPanel = new BorderPanel{
    layout(flowPanel) = South}
  flowPanel.background = uiParams.backgroundColor
  borderPanel.background = uiParams.backgroundColor
  contents = borderPanel
  peer.setDefaultCloseOperation(0)
  override def closeOperation() {closing()}
  //Abstract methods
  def closing()
  //Methods
  def show(defX:Int, defY:Int, defW:Int, defH:Int) = {
    //Placing of components
    flowPanel.contents ++= bottom
    center.foreach(c ⇒ borderPanel.layout(c) = Center)
    //Calc size
    val (w,h) = (center, bottom.map(c ⇒ (c.preferredSize.getWidth.toInt, c.preferredSize.getHeight.toInt)).unzip) match{
      case (Some(c), (ws, hs)) if ws.nonEmpty ⇒ {
        val cw = List(ws.sum + (ws.size - 1) * flowPanel.vGap + 4, c.preferredSize.getWidth.toInt).max
        val ch = hs.max +  flowPanel.hGap * 2
        val mch = if(c.preferredSize.getWidth.toInt < cw){
          ((cw.toDouble / c.preferredSize.getWidth) * c.preferredSize.getHeight).toInt + ch}
        else{
           c.preferredSize.getHeight.toInt + ch}
        (cw,mch)}
      case (_, (ws, hs)) if ws.nonEmpty ⇒
        (ws.max, hs.max)
      case (Some(c), _) ⇒ (c.preferredSize.getWidth.toInt, c.preferredSize.getHeight.toInt)
      case _ ⇒
        (100,100)}

    //Show
    if(defW == Int.MaxValue && defH == Int.MaxValue){
      borderPanel.preferredSize = new swing.Dimension(
        if(defW != Int.MaxValue) defW else w,
        if(defH != Int.MaxValue) defH else h)}
    pack()
    visible = true
    //Locate
    location = layout.occupyLocation(size, defX, defY)}
  def hide() = {
    visible = false}
  def setTitleAdd(add:String) = {
    title = windowTitle + add}}