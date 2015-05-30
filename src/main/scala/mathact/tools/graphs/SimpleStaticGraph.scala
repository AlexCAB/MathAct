package mathact.tools.graphs
import java.awt.{Point, Color}
import mathact.utils.ui.components.{EdgeEnd, BorderFrame}
import mathact.utils.{ToolHelper, Tool, Environment}
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.Colors


/**
 * Tool for visualisation of static graph.
 * Created by CAB on 29.05.2015.
 */

abstract class SimpleStaticGraph(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool with Colors{
  //Variables
  private var idCounter = 0
  private var nodes = List[Node]()
  private var edges = List[Edge]()
  //Functions
  private def nextID:String = {idCounter += 1; "id_" + idCounter}
  private def addNode(node:Node):Node = {nodes = nodes.filter(_.id != node.id) :+ node; node}
  private def addEdge(edge:Edge):Edge = {edges = edges.filter(_.id != edge.id) :+ edge; edge}
  //Internal classes
  protected case class Node(
    id:String,
    name:Option[String],
    colorFun:()⇒Color,
    weightFun:()⇒Double,
    pointFun:Option[()⇒Point])
  {
    def color(color: ⇒Color):Node =
      addNode(Node(id, name, ()⇒color, weightFun, None))
    def weight(weight: ⇒Double):Node =
      addNode(Node(id, name, colorFun, ()⇒weight, None))
    def fixOn(x:Double, y:Double):Node =
      addNode(Node(id, name, colorFun, weightFun, Some(()⇒new Point(x.toInt,y.toInt))))}
  protected case class Edge(
    id:String,
    source:Node,
    sourceEnd:EdgeEnd,
    target:Node,
    targetEnd:EdgeEnd,
    name:Option[String],
    colorFun:()⇒Color,
    weightFun:()⇒Double)
  {
    def color(color: ⇒Color):Edge =
      addEdge(Edge(id, source, sourceEnd, target, targetEnd, name, ()⇒color, weightFun))
    def weight(weight: ⇒Double):Edge =
      addEdge(Edge(id, source, sourceEnd, target, targetEnd, name, colorFun, ()⇒weight))}
  //DSL
  def node(name:String = "", color:Color = gray, weight:Double = 1):Node =
    Node(
      nextID,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      ()⇒color,
      ()⇒weight,
      None)
  def edge(source:Node, target:Node, name:String = "", color:Color = black, weight:Double = 1):Edge =
    Edge(
      nextID,
      source,
      EdgeEnd.None,
      target, EdgeEnd.None,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      ()⇒color,
      ()⇒weight)
  def arrow(source:Node, target:Node, name:String = "", color:Color = black, weight:Double = 1):Edge =
    Edge(nextID,
      source,
      EdgeEnd.None,
      target,
      EdgeEnd.Arrow,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      ()⇒color,
      ()⇒weight)
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "SimpleStaticGraph")
  //UI



//  private val plot = new XYsPlot(environment.params.XYPlot, screenW, screenH, drawPoints)
//  private val minMaxAvg = new MinMaxAvgPane(environment.params.XYPlot)



  private val frame = new BorderFrame(
    environment.layout, environment.params.SimpleStaticGraph, helper.toolName, center = None){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {




      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
//      val bars = datas.flatMap{case Data(c, d) ⇒ d().map(v ⇒ (c, v))}
//      histogram.updateY(bars)
//      minMaxAvg.update(bars.map(_._2))
      updated()}
    def stop() = {
//      frame.hide()

    }}


}
