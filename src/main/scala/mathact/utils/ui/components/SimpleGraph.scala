package mathact.utils.ui.components
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
import scala.swing.{Component, Dimension, Color, BorderPanel, Point}
import scala.collection.mutable.{Map ⇒ MutMap}
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
  height:Int,
  resizeByWeight:Boolean,
  showLabels:Boolean)
extends BorderPanel with UIComponent{
  //Helpers
  private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
  decimal.applyPattern(uiParams.numberFormat)
  //Classes
  private case class LabelData(name:Option[String], weight:Option[String], vars:Option[String])
  //Variables
  private val labelsData = MutMap[String, LabelData]()
  //Functions
  private def makeColorAtr(c:Color):String =
    s"fill-color: rgb(${c.getRed().toString},${c.getGreen()},${c.getBlue()});"
  private def makeSizeAtr(name:String, w:Int, h:Int):String =
    s"$name: ${w}px, ${h}px;"
  private def makeSizeAtr(name:String, s:Int):String =
    s"$name: ${s}px, ${s}px;"
  private def makeArrowSizeAtr(size:Int):String = size match{
    case s if s != 0 ⇒ {
      s"arrow-size: ${((size * 6) / math.sqrt(size)).toInt}px, ${((size * 3) / math.sqrt(size)).toInt}px;"}
    case _ ⇒
      "arrow-size: 2px, 2px;"}
  private def calcNodeSize(weight:Option[(Option[String],Double)]):Int = (weight, resizeByWeight) match{
    case (Some((_, w)),true) ⇒ (w * 30).toInt
    case _ ⇒ uiParams.defaultNodeSize}
  private def calcEdgeSize(weight:Option[(Option[String],Double)]):Int = (weight, resizeByWeight) match{
    case (Some((_, w)),true) ⇒ (w * 10).toInt
    case _ ⇒ uiParams.defaultEdgeSize}
  private def buildLabel(
    id:String,
    name:Option[String],
    weight:Option[(Option[String],Double)],
    vars:Option[List[(String,Double)]])
  :Option[String] = showLabels match{
    case true ⇒ {
      val stringWeight = weight.map{case(n,w) ⇒ n.getOrElse("w") + "=" + decimal.format(w)}
      val stringVars = vars.map(_.map{case(n,v) ⇒ n + "=" + decimal.format(v)}.mkString(", "))
      def selectNew(a:Option[String], b:Option[String]) = (a, b) match{
        case (Some(a),_) ⇒ Some(a)
        case (None,Some(b)) ⇒ Some(b)
        case _ ⇒ None}
      val labelData = labelsData.get(id) match{
        case Some(LabelData(n, sw, sv)) ⇒ {
          val ld = LabelData(selectNew(name, n), selectNew(stringWeight, sw), selectNew(stringVars, sv))
          labelsData += id → ld
          ld}
        case None ⇒ {
          val ld = LabelData(name, stringWeight, stringVars)
          labelsData += id → ld
          ld}}
      val varsBlock = (labelData.weight, labelData.vars) match{
        case (None,None) ⇒ None
        case (sw:Some[String], sv:Some[String]) ⇒ Some("{" + sw.getOrElse("") + ", " + sv.getOrElse("") + "}")
        case (sw, sv) ⇒ Some("{" + sw.getOrElse("") + sv.getOrElse("") + "}")}
      (labelData.name, varsBlock) match{
        case (None,None) ⇒ None
        case (n,vb) ⇒ Some("  " + n.getOrElse("") + " " + vb.getOrElse(""))}}
    case false ⇒ None}
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
  def addNode(
    id:String,
    name:Option[String],
    color:Option[Color],
    weight:Option[(Option[String],Double)],
    position:Option[Point],
    vars:Option[List[(String,Double)]])
  :Unit = {
    val n = graph.addNode[Node](id)
    buildLabel(id, name,weight,vars).foreach(nn ⇒ n.addAttribute("ui.label", nn))
    n.addAttribute("ui.style", makeColorAtr(color.getOrElse(uiParams.defaultNodeColor)))
    n.addAttribute("ui.style", makeSizeAtr("size", calcNodeSize(weight)))
    position.foreach(p ⇒ {
      n.addAttribute("layout.frozen")
      n.addAttribute("xy", new Integer(p.x), new Integer(-p.y))})}
  def delNode(id:String):Unit = {
    graph.removeNode(id.toString)}
  def addEdge(
    id:String,
    sourceId:String,
    targetId:String,
    idDirected:Boolean,
    name:Option[String],
    color:Option[Color],
    weight:Option[(Option[String],Double)],
    vars:Option[List[(String,Double)]])
  :Unit = {
    val e = graph.addEdge[Edge](id, sourceId, targetId, idDirected)
    buildLabel(id, name,weight,vars).foreach(en ⇒ e.addAttribute("ui.label", en))
    e.addAttribute("ui.style", makeColorAtr(color.getOrElse(uiParams.defaultNodeColor)))
    val es = calcEdgeSize(weight)
    e.addAttribute("ui.style", makeSizeAtr("size", es))
    e.addAttribute("ui.style", makeArrowSizeAtr(es))}
  def delEdge(id:String):Unit = {
    graph.removeEdge(id)}
  def clear():Unit = {graph.clear()}
  def updateNode(
    id:String,
    name:Option[String],
    color:Option[Color],
    weight:Option[(Option[String],Double)],
    position:Option[(Boolean,Point)],
    vars:Option[List[(String,Double)]])
  :Unit= {
    val n = graph.getNode[Node](id)
    buildLabel(id, name,weight,vars).foreach(nn ⇒ n.addAttribute("ui.label", nn))
    color.foreach(nc ⇒ n.addAttribute("ui.style", makeColorAtr(nc)))
    weight.foreach(_ ⇒ n.addAttribute("ui.style", makeSizeAtr("size", calcNodeSize(weight))))
    position match{
      case Some((true, p)) ⇒ {
        n.addAttribute("layout.frozen")
        n.addAttribute("xy", new Integer(p.x), new Integer(-p.y))}
      case Some((false, _)) ⇒ {
        n.removeAttribute("layout.frozen")}
      case _ ⇒ }}
  def updateEdge(
    id:String,
    name:Option[String],
    color:Option[Color],
    weight:Option[(Option[String],Double)],
    vars:Option[List[(String,Double)]])
  :Unit = {
    val e = graph.getEdge[Edge](id)
    buildLabel(id, name,weight,vars).foreach(en ⇒ e.addAttribute("ui.label", en))
    color.foreach(ec ⇒ e.addAttribute("ui.style", makeColorAtr(ec)))
    weight.foreach(ew ⇒ {
      val es = calcEdgeSize(weight)
      e.addAttribute("ui.style", makeSizeAtr("size", es, es))
      e.addAttribute("ui.style", makeArrowSizeAtr(es))})}}
