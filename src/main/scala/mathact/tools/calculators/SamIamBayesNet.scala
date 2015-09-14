package mathact.tools.calculators
import java.awt.Point
import java.io.File
import edu.ucla.belief.io.hugin.HuginNode
import edu.ucla.belief.{InferenceEngine, BeliefNetwork}
import edu.ucla.belief.io.{PropertySuperintendent, NetworkIO}
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{SimpleGraph, ResetButton, TVFrame}
import scala.language.implicitConversions
import edu.ucla.belief.approx.{MessagePassingScheduler, PropagationEngineGenerator}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.collection.JavaConverters._


/**
 * Wrapper for SamIam (http://reasoning.cs.ucla.edu/samiam/index.php) bayes net inference engine.
 * Created by CAB on 12.09.2015.
 */

abstract class SamIamBayesNet(
  netPath:String,
  name:String = "",
  showUI:Boolean = true,                    //ON/OFF graphical UI
  showLabels:Boolean = true,
  engineTimeout:Long = 10000,
  engineMaxIterations:Int = 100,
  engineScheduler:MessagePassingScheduler = edu.ucla.belief.approx.MessagePassingScheduler.TOPDOWNBOTTUMUP,
  engineConvergenceThreshold:Double = 1.0E-7,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 600,
  screenH:Int = 300)
 (implicit environment:Environment)
extends Tool{
  //Private definition
  private trait Node
  private trait NoneVar extends Node{
    val nodeID:String
    var node:Option[HuginNode] = None}
  private trait Prop extends Node
  private case class BinTableProb(
    nodeID:String, v:()⇒Double) extends Prop with NoneVar
  private case class TableProb(
    nodeID:String, vs:Seq[()⇒Double]) extends Prop with NoneVar
  private case class BinColumnProb(
    nodeID:String, colName:Seq[String], v:()⇒Double) extends Prop with NoneVar
  private case class ColumnProb(
    nodeID:String, colName:Seq[String], vs:Seq[()⇒Double]) extends Prop with NoneVar
  private trait Evidence extends Node
  private case class NodeEvidence(
    nodeID:String, v:()⇒String) extends Evidence with NoneVar
  private trait Inference extends Node
  private case class AllInference(
    p:(Map[String, Map[String, Double]])⇒Unit) extends Inference
  private case class AllValuesInference(
    p:(Map[String, String])⇒Unit) extends Inference
  private case class BinaryInference(
    nodeID:String, p:Double⇒Unit) extends Inference with NoneVar
  private case class AllProbInference(
    nodeID:String, p:Map[String,Double]⇒Unit) extends Inference with NoneVar
  private case class ValueInference(
    nodeID:String, p:String⇒Unit) extends Inference with NoneVar
  private case class ValueProbInference(
    nodeID:String, valueName:String, p:Double⇒Unit) extends Inference with NoneVar
  //Helpers
  private val helper = new ToolHelper(this, name, netPath.replace("\\","/").split("/").last)
  private val dynamator = new PropagationEngineGenerator
  private val uiParams = environment.params.SamIamBayesNet
  //Variables
  private var probabilities = List[Prop]()
  private var evidences = List[Evidence]()
  private var inferences = List[Inference]()
  private var beliefNet:Option[(BeliefNetwork, InferenceEngine)] = None
  private var ui:Option[(TVFrame, SimpleGraph)] = None

  //Functions
  private def getAndCheckNode(vertices:Map[String, HuginNode], node:Node with NoneVar):HuginNode = {
    //Get id and vertice
    val id = node.nodeID
    if(! vertices.contains(id)){
      throw new SyntaxException(s"Error: In '$netPath' not found node with ID '$id', exist: ${vertices.keySet}")}
    val vertice = vertices(id)
    //Check definitions
    node match {
      case BinTableProb(_,v) ⇒ {

      }
      case TableProb( _,vs) ⇒ {

      }
      case BinColumnProb( _,colName, v) ⇒ {

      }
      case ColumnProb(_,colName, vs) ⇒ {

      }
      case NodeEvidence(_,v) ⇒ {

      }
      case BinaryInference(_,p) ⇒ {

      }
      case AllProbInference(_,p) ⇒ {

      }
      case ValueInference(_,p) ⇒ {

      }
      case ValueProbInference(_,valueName,p) ⇒ {

      }
      case _:AllInference ⇒
      case _:AllValuesInference ⇒}
    vertice}
  private def loadNet():Unit = {
    //Stop prev engine
    beliefNet.foreach{case (_,e) ⇒ e.die()}
    //Load net
    val file = new File(netPath)
    if((! file.exists()) || file.isDirectory){
      throw new SyntaxException(s"Error: File '$file' not exist or is directory.")}
    val bayesNet = NetworkIO.read(file)
    //Setting
    val settings = PropagationEngineGenerator.getSettings(bayesNet.asInstanceOf[PropertySuperintendent])
    settings.setTimeoutMillis(engineTimeout)
    settings.setMaxIterations(engineMaxIterations)
    settings.setScheduler(engineScheduler)
    settings.setConvergenceThreshold(engineConvergenceThreshold)
    //Engine
    val engine = dynamator.manufactureInferenceEngine(bayesNet)
    beliefNet = Some(bayesNet,engine)
    //Map nodes to Prop probabilities, evidences, inferences
    val vertices = bayesNet.vertices().toArray.flatMap{case n:HuginNode ⇒ Some((n.getID, n)); case _ ⇒ None}.toMap
    probabilities = probabilities.map{case n:NoneVar  ⇒ {n.node = Some(getAndCheckNode(vertices, n)); n}; case e ⇒ e}
    evidences = evidences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(vertices, n)); n}; case e ⇒ e}
    inferences = inferences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(vertices, n)); n}; case e ⇒ e}}
  private def buildGraph(graph:SimpleGraph, net:BeliefNetwork):Unit = {
    //Clear old
    graph.clear()
    //Build
    val nodes = net.vertices().toArray.flatMap{
      case v:HuginNode ⇒ {
        val id = v.getID
        val props = v.getProperties
        val List(x, y) = props.get("position").asInstanceOf[java.util.ArrayList[Int]].asScala.toList
        val l = props.get("label").toString
        graph.addNode(id, Some(l), None, None, Some(new Point(x, y * -1)), None)
        Some((id, v))}
      case _ ⇒ None}
    nodes.foreach{case (id, node) ⇒ {
      net.inComing(node).asInstanceOf[java.util.Set[HuginNode]].asScala.foreach(vo ⇒ {
        graph.addEdge(id + vo.getID, vo.getID, id, true, None, None, None, None)})}}}
  private def reload():Unit = {
    //Load
    loadNet()
    //Build UI
    (ui,beliefNet) match{
      case (Some((frame,graph)),Some((net,_))) ⇒ {
        buildGraph(graph,net)}
      case _ ⇒ }}
  //DSL
  protected implicit def byNameToNoArg[T](v: ⇒ T):()⇒T = {() ⇒ v}
  protected def cpt:ProbNode = new ProbNode
  protected class ProbNode{
    def node(id:String):ProbCPD = new ProbCPD(id)}
  protected class ProbCPD(nodeID:String){
    def binary(cell0: ⇒Double):Unit = {probabilities :+= BinTableProb(nodeID, ()⇒{cell0})}
    def column(name0:String, nameN:String*):ProbColumn = new ProbColumn(nodeID, name0 +: nameN)
    def table(cell0:()⇒Double, cellN:(()⇒Double)*):Unit = {probabilities :+= TableProb(nodeID, cell0 +: cellN)}}
  protected class ProbColumn(nodeID:String, colName:Seq[String]){
    def binary(cell0: ⇒ Double): Unit = {probabilities :+= BinColumnProb(nodeID, colName, ()⇒{cell0})}
    def of(cell1: () ⇒ Double, cellN: (() ⇒ Double)*): Unit = {probabilities :+= ColumnProb(nodeID, colName, cellN)}}
  protected def evidence:EvdNode = new EvdNode
  protected class EvdNode{
      def node(id:String):EvdValue = new EvdValue(id)}
  protected class EvdValue(nodeID:String){
      def of(valueName: ⇒String):Unit = {evidences :+= NodeEvidence(nodeID, ()⇒{valueName})}}
  protected def inference:InfNode = new InfNode
  protected def inference(p:(Map[String, Map[String, Double]])⇒Unit):Unit = {inferences :+= AllInference(p)}
  protected class InfNode{
    def node(id:String):InfResult = new InfResult(id)
    def allValues(p:(Map[String, String])⇒Unit):Unit = {inferences :+= AllValuesInference(p)}}
  protected class InfResult(nodeID:String) {
    def binaryProb(p:Double⇒Unit):Unit = {inferences :+= BinaryInference(nodeID, p)}
    def allProb(p:Map[String,Double]⇒Unit):Unit = {inferences :+= AllProbInference(nodeID, p)}
    def value(p:String⇒Unit):Unit = {inferences :+= ValueInference(nodeID, p)}
    def valueProb(name:String):InfValue = new InfValue(nodeID, name)}
  protected class InfValue(nodeID:String, name:String){
    def prob(p:Double⇒Unit):Unit = {inferences :+= ValueProbInference(nodeID, name, p)}}
  //Construction
  if(netPath == ""){
    throw new SyntaxException("Error: 'netPath' should not be empty.")}
  //UI
  ui = showUI match{
    case true ⇒ {
      val uiGraph = new SimpleGraph(uiParams, screenW, screenH, false, showLabels)
      val uiReset = new ResetButton(uiParams){
        def reset() = {reload()}}
      val uiFrame:TVFrame = new TVFrame(
        environment.layout, uiParams, helper.toolName, center = Some(uiGraph), bottom = List(uiReset)){
        def closing() = {gear.endWork()}}
      Some((uiFrame, uiGraph))}
    case _ ⇒ None  }
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Load net
      loadNet()





      //Build UI
      (ui,beliefNet) match{
        case (Some((frame,graph)),Some((net,_))) ⇒ {
          buildGraph(graph,net)
          frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
        case _ ⇒ }}
    def update() = {



    }
    def stop() = {
      ui.foreach{case (frame,_) ⇒ frame.hide()}}}}