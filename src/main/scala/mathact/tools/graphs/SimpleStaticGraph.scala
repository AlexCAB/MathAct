package mathact.tools.graphs
import java.awt.{Point, Color}
import mathact.utils.ui.components.{SimpleGraph, BorderFrame}
import mathact.utils.{ToolHelper, Tool, Environment}
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.{FunParameter, Colors}


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
    colorFun:FunParameter[Color],
    weightFun:FunParameter[Double],
    pointFun:Option[FunParameter[Point]])
  {
    //Methods
    def color(color: ⇒Color):Node =
      addNode(Node(id, name, FunParameter(()⇒color), weightFun, None))
    def weight(weight: ⇒Double):Node =
      addNode(Node(id, name, colorFun, FunParameter(()⇒weight), None))
    def fixOn(x:Double, y:Double):Node =
      addNode(Node(id, name, colorFun, weightFun, Some(FunParameter(()⇒new Point(x.toInt,y.toInt)))))}
  protected case class Edge(
    id:String,
    source:Node,
    target:Node,
    idDirected:Boolean,
    name:Option[String],
    colorFun:FunParameter[Color],
    weightFun:FunParameter[Double])
  {
    def color(color: ⇒Color):Edge =
      addEdge(Edge(id, source, target, idDirected, name, FunParameter(()⇒color), weightFun))
    def weight(weight: ⇒Double):Edge =
      addEdge(Edge(id, source, target, idDirected, name, colorFun, FunParameter(()⇒weight)))}
  //DSL
  def node(name:String = "", color:Color = gray, weight:Double = 1):Node =
    Node(
      nextID,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(()⇒color),
        FunParameter(()⇒weight),
      None)
  def edge(source:Node, target:Node, name:String = "", color:Color = black, weight:Double = 1):Edge =
    Edge(
      nextID,
      source,
      target,
      idDirected = false,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(()⇒color),
        FunParameter(()⇒weight))
  def arrow(source:Node, target:Node, name:String = "", color:Color = black, weight:Double = 1):Edge =
    Edge(nextID,
      source,
      target,
      idDirected = true,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(()⇒color),
        FunParameter(()⇒weight))
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "SimpleStaticGraph")
  //UI
  private val graph = new SimpleGraph(environment.params.SimpleStaticGraph, screenW, screenH)
  private val frame = new BorderFrame(
    environment.layout, environment.params.SimpleStaticGraph, helper.toolName, center = Some(graph)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      nodes.foreach(n ⇒ {
        graph.addNode(
          n.id,
          n.name,
          n.colorFun.getValue,
          n.weightFun.getValue.map(v ⇒ (v * 30).toInt),
          n.pointFun.flatMap(_.getValue))})
      edges.foreach(e ⇒ {
        graph.addEdge(
          e.id,
          e.source.id,
          e.target.id,
          e.idDirected,
          e.name,
          e.colorFun.getValue,
          e.weightFun.getValue.map(v ⇒ (v * 10).toInt))})
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      nodes.foreach(n ⇒ {
        graph.updateNode(
          n.id,
          None,
          n.colorFun.getValue,
          n.weightFun.getValue.map(v ⇒ (v * 30).toInt),
          None)})
      edges.foreach(e ⇒ {
        graph.updateEdge(
          e.id,
          None,
          e.colorFun.getValue,
          e.weightFun.getValue.map(v ⇒ (v * 10).toInt))})
      updated()}
    def stop() = {
      frame.hide()}}}
