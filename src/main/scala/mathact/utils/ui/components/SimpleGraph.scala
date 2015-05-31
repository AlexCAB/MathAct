package mathact.utils.ui.components
import scala.swing.{Component, Dimension, Color, BorderPanel, Point}
import mathact.utils.ui.UIParams
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import org.graphstream.ui.view.Viewer
import org.graphstream.graph.{Node,Edge}


/**
 * Simple graph visualisation panel.
 * Created by CAB on 31.05.2015.
 */

class SimpleGraph(
  uiParams:UIParams.SimpleGraph,
  width:Int,
  height:Int)
extends BorderPanel with UIComponent{
  //Functions
  private def makeColorAtr(c:Color):String =
    s"fill-color: rgb(${c.getRed().toString},${c.getGreen()},${c.getBlue()});"
  private def makeSizeAtr(name:String, w:Int, h:Int):String =
    s"$name: ${w}px, ${h}px;"
  private def makeArrowSizeAtr(size:Int):String = size match{
    case s if s != 0 ⇒ {
      s"arrow-size: ${((size * 10) / math.sqrt(size)).toInt}px, ${((size * 5) / math.sqrt(size)).toInt}px;"}
    case _ ⇒
      "arrow-size: 2px, 2px;"}
  //Construction
  preferredSize = new Dimension(width, height)
  private val box = new SpringBox(false)
  private val graph = new MultiGraph("MV")
  private val viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
  viewer.enableAutoLayout(box)
  box.setStabilizationLimit(1)
  box.setGravityFactor(0.05)
  graph.addAttribute("ui.stylesheet", "graph {" + makeColorAtr(uiParams.backgroundColor) + "}")
  graph.setAutoCreate(false)
  //Show
  layout(Component.wrap(viewer.addDefaultView(false))) = BorderPanel.Position.Center
  //Methods
  def addNode(id:String, name:Option[String], color:Option[Color], size:Option[Int], position:Option[Point]) = {
    val n = graph.addNode[Node](id)
    name.foreach(nn ⇒ n.addAttribute("ui.label", nn))
    n.addAttribute("ui.style", makeColorAtr(color.getOrElse(uiParams.defaultNodeColor)))
    size.foreach(ns ⇒ n.addAttribute("ui.style", makeSizeAtr("size",ns, ns)))
    position.foreach(p ⇒ {
      n.addAttribute("layout.frozen")
      n.addAttribute("xy", new Integer(p.x), new Integer(-p.y))})}
  def delNode(id:String) = {
    graph.removeNode(id.toString)}
  def addEdge(id:String, sourceId:String, targetId:String, idDirected:Boolean,
    name:Option[String], color:Option[Color], size:Option[Int])
  = {
    val e = graph.addEdge[Edge](id, sourceId, targetId, idDirected)
    name.foreach(en ⇒ e.addAttribute("ui.label", en))
    e.addAttribute("ui.style", makeColorAtr(color.getOrElse(uiParams.defaultNodeColor)))
    size.foreach(es ⇒ {
      e.addAttribute("ui.style", makeSizeAtr("size", es, es))
      e.addAttribute("ui.style", makeArrowSizeAtr(es))})}
  def delEdge(id:String) = {
    graph.removeEdge(id)}
  def clear() = {graph.clear()}
  def updateNode(
    id:String,
    name:Option[String],
    color:Option[Color],
    size:Option[Int],
    position:Option[(Boolean,Point)])
  = {
    val n = graph.getNode[Node](id)
    name.foreach(nn ⇒ n.addAttribute("ui.label", nn))
    color.foreach(nc ⇒ n.addAttribute("ui.style", makeColorAtr(nc)))
    size.foreach(ns ⇒ n.addAttribute("ui.style", makeSizeAtr("size", ns, ns)))
    position match{
      case Some((true, p)) ⇒ {
        n.addAttribute("layout.frozen")
        n.addAttribute("xy", new Integer(p.x), new Integer(-p.y))}
      case Some((false, _)) ⇒ {
        n.removeAttribute("layout.frozen")}
      case _ ⇒ }}
  def updateEdge(id:String, name:Option[String], color:Option[Color], size:Option[Int]) = {
    val e = graph.getEdge[Edge](id)
    name.foreach(en ⇒ e.addAttribute("ui.label", en))
    color.foreach(ec ⇒ e.addAttribute("ui.style", makeColorAtr(ec)))
    size.foreach(es ⇒ {
      e.addAttribute("ui.style", makeSizeAtr("size", es, es))
      e.addAttribute("ui.style", makeArrowSizeAtr(es))})}}
