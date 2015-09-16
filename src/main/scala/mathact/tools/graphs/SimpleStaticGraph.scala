package mathact.tools.graphs
import java.awt.{Point, Color}
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
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
  resizeByWeight:Boolean = false,
  showLabels:Boolean = true,
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
  private val params = environment.params.SimpleStaticGraph
  private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
  decimal.applyPattern(params.numberFormat)
  //Functions
  private def nextID:String = {idCounter += 1; "id_" + idCounter}
  private def addNode(node:Node):Node = {nodes = nodes.filter(_.id != node.id) :+ node; node}
  private def addEdge(edge:Edge):Edge = {edges = edges.filter(_.id != edge.id) :+ edge; edge}
  //Internal classes
  protected case class Node(
    id:String,
    name:Option[String],
    colorFun:FunParameter[Color],
    weightFun:Option[FunParameter[Double]],
    pointFun:Option[FunParameter[Point]],
    vars:List[FunParameter[Double]])
  {
    //Methods
    def color(color: ⇒Color):Node =
      addNode(Node(id, name, FunParameter(None, ()⇒color), weightFun, pointFun, vars))
    def weight(weight: ⇒Double):Node =
      addNode(Node(id, name, colorFun, Some(FunParameter(None, ()⇒weight)), pointFun, vars))
    def weight(weightName:String, weight: ⇒Double):Node =
      addNode(Node(id, name, colorFun, Some(FunParameter(Some(weightName), ()⇒weight)), pointFun, vars))
//    def fixOn(x:Double, y:Double):Node =
//      addNode(Node(id, name, colorFun, weightFun, Some(FunParameter(None, ()⇒new Point(x.toInt,y.toInt))), vars))
    def variable(varName:String, varProc: ⇒Double):Node =
      addNode(Node(id, name, colorFun, weightFun, pointFun,
        vars :+ FunParameter(Some(varName), () ⇒ varProc)))}
  protected case class Edge(
    id:String,
    source:Node,
    target:Node,
    idDirected:Boolean,
    name:Option[String],
    colorFun:FunParameter[Color],
    weightFun:Option[FunParameter[Double]],
    vars:List[FunParameter[Double]])
  {
    def color(color: ⇒Color):Edge =
      addEdge(Edge(id, source, target, idDirected, name, FunParameter(None,()⇒color), weightFun, vars))
    def weight(weight: ⇒Double):Edge =
      addEdge(Edge(id, source, target, idDirected, name, colorFun, Some(FunParameter(None,()⇒weight)), vars))
    def weight(weightName:String, weight: ⇒Double):Edge =
      addEdge(
        Edge(id, source, target, idDirected, name, colorFun, Some(FunParameter(Some(weightName),()⇒weight)), vars))
    def variable(varName:String, varProc: ⇒Double):Edge =
      addEdge(
        Edge(id, source, target, idDirected, name, colorFun, weightFun,
          vars :+ FunParameter(Some(varName), () ⇒ varProc)))}
  //DSL
  def node(
    name:String = "",
    color:Color = params.defaultNodeColor,
    weight:Double = Double.NaN,
    fixOn:(Int,Int) = (Int.MinValue,Int.MinValue))
  :Node =
    addNode(Node(
      nextID,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(None, () ⇒ color),
      weight match{case w if w.isNaN ⇒ None; case w ⇒ Some(FunParameter(None, () ⇒ w))},
      fixOn match{
        case (x,y) if x == Int.MinValue || y == Int.MinValue ⇒ None
        case (x,y) ⇒ Some(FunParameter(None, ()⇒new Point(x.toInt,y.toInt)))},
      List()))
  def edge(
    source:Node,
    target:Node,
    name:String = "",
    color:Color = params.defaultEdgeColor,
    weight:Double = Double.NaN)
  :Edge =
    addEdge(Edge(
      nextID,
      source,
      target,
      idDirected = false,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(None, () ⇒ color),
      weight match{case w if w.isNaN ⇒ None; case w ⇒ Some(FunParameter(None, () ⇒ w))},
      List()))
  def arc(
    source:Node,
    target:Node,
    name:String = "",
    color:Color = params.defaultEdgeColor,
    weight:Double = Double.NaN)
  :Edge =
    addEdge(Edge(nextID,
      source,
      target,
      idDirected = true,
      name match{case s if s == "" ⇒ None; case s ⇒ Some(s)},
      FunParameter(None, ()⇒color),
      weight match{case w if w.isNaN ⇒ None; case w ⇒ Some(FunParameter(None, () ⇒ w))},
      List()))
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "SimpleStaticGraph")
  //UI
  private val graph = new SimpleGraph(
    environment.params.SimpleStaticGraph, screenW, screenH, resizeByWeight, showLabels)
  private val frame = new BorderFrame(
    environment.layout, environment.params.SimpleStaticGraph, helper.toolName, center = Some(graph)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    //Function
    def convertVars(vars:List[FunParameter[Double]]):Option[List[(String,String)]] = {
      vars.map(fp ⇒ (fp.getLastValueWithName, fp.getValueWithName)) match{
        case lv if lv.exists{case (_,v) ⇒ v.nonEmpty} ⇒ Some(
          lv.map{
            case (_, Some((n,v))) ⇒ (n.getOrElse(""), decimal.format(v))
            case (Some((n,v)), _) ⇒ (n.getOrElse(""), decimal.format(v))
            case _ ⇒ ("", "0.0")})
        case _ ⇒ None}}
    //Methods
    def start() = {
      nodes.foreach(n ⇒ {
        graph.addNode(
          n.id,
          n.name,
          n.colorFun.getValue,
          n.weightFun.flatMap(_.getValueWithName),
          n.pointFun.flatMap(_.getValue),
          convertVars(n.vars))})
      edges.foreach(e ⇒ {
        graph.addEdge(
          e.id,
          e.source.id,
          e.target.id,
          e.idDirected,
          e.name,
          e.colorFun.getValue,
          e.weightFun.flatMap(_.getValueWithName),
          convertVars(e.vars))})
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      nodes.foreach(n ⇒ {
        graph.updateNode(
          n.id,
          None,
          n.colorFun.getValue,
          n.weightFun.flatMap(_.getValueWithName),
          None,
          convertVars(n.vars))})
      edges.foreach(e ⇒ {
        graph.updateEdge(
          e.id,
          None,
          e.colorFun.getValue,
          e.weightFun.flatMap(_.getValueWithName),
          convertVars(e.vars))})
      updated()}
    def stop() = {
      frame.hide()}}}
