package mathact.tools.calculators
import java.awt.Point
import java.io.File
import edu.ucla.belief.io.hugin.HuginNode
import edu.ucla.belief.{FiniteVariable, InferenceEngine, BeliefNetwork}
import edu.ucla.belief.io.{PropertySuperintendent, NetworkIO}
import mathact.utils.clockwork.{ExecutionException, VisualisationGear}
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
  showInference:Boolean = true,             //Shove|Hide node inference values and prop
  showCPT:Boolean = false,                  //Shove|Hide node CPT
  autoUpdate:Boolean = true,
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
  private case class CPTData(node:HuginNode, linesNames:List[String], columnsIndexes:Map[List[String],Int])
  private trait Node
  private trait NoneVar extends Node{
    val nodeID:String
    var node:Option[CPTData] = None}
  private trait Prop extends Node
  private case class BinTableProb(
    nodeID:String, v:()⇒Double) extends Prop with NoneVar
  private case class TableProb(
    nodeID:String, vs:Seq[()⇒Double]) extends Prop with NoneVar
  private case class BinColumnProb(
    nodeID:String, colName:List[String], v:()⇒Double) extends Prop with NoneVar
  private case class ColumnProb(
    nodeID:String, colName:List[String], vs:Seq[()⇒Double]) extends Prop with NoneVar
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
  private var beliefNet:Option[BeliefNetwork] = None
  private var ui:Option[(TVFrame, SimpleGraph)] = None
  //Functions
  private def getAndCheckNode(net:BeliefNetwork, node:Node with NoneVar):CPTData = {
    //Get date
    val id = node.nodeID
    val vertices = net.vertices().toArray.flatMap{case n:HuginNode ⇒ Some((n.getID, n)); case _ ⇒ None}.toMap
    if(! vertices.contains(id)){
      throw new SyntaxException(s"Error: In '$netPath' not found node with ID '$id', exist: ${vertices.keySet}")}
    val vertex = vertices(id)
    val cptTable = vertex.getCPTShell.getCPT
    val linesNames = vertex.instances.asInstanceOf[java.util.List[String]].asScala.toList
    val instNames = cptTable.variables.asInstanceOf[java.util.List[FiniteVariable]].asScala.toList.dropRight(1)
      .map(_.instances.asInstanceOf[java.util.List[String]].asScala.toList)
    def buildIndexes(il:List[List[String]]):List[List[String]] = il match{
      case h :: t if t.nonEmpty ⇒  h.flatMap(e ⇒ buildIndexes(t).map(l ⇒ e +: l))
      case h :: Nil ⇒ h.map(e ⇒ List(e))
      case Nil ⇒ List()}
    val columnsIndexes = buildIndexes(instNames).zipWithIndex.toMap
    //Check definitions
    def incorrectDef(msg:String):SyntaxException =
      new SyntaxException(s"Error, for node with id ='$id': $msg.")
    node match {
      case BinTableProb(_,v) ⇒ {
        if(cptTable.getCPLength != 2){
          throw incorrectDef(s"CPT is not a binary (length(${cptTable.getCPLength}) != 2)")}}
      case TableProb( _,vs) ⇒ {
        if(cptTable.getCPLength != vs.size){
          throw incorrectDef(s"CPT size not equal to table size (${cptTable.getCPLength} != ${vs.size}})")}}
      case BinColumnProb( _, colName, v) ⇒ {
        if(! columnsIndexes.contains(colName)){
          throw incorrectDef(s"Unknown column name '$colName', available: ${columnsIndexes.keySet}")}
        if(linesNames.size != 2){
          throw incorrectDef(s"Column $colName not binary(${linesNames.size} != 2)")}}
      case ColumnProb(_, colName, vs) ⇒ {
        if(! columnsIndexes.contains(colName)){
          throw incorrectDef(s"Unknown column name '$colName', available: ${columnsIndexes.keySet}")}
        if(linesNames.size != vs.size){
          throw incorrectDef(s"Column '$colName' not match number of cell (${linesNames.size} != ${vs.size}})")}}
      case BinaryInference(_, p) ⇒ {
        if(linesNames.size != 2){
          throw incorrectDef(s"Node not binary(${linesNames.size} != 2)")}}
      case ValueProbInference(_, valueName, p) ⇒ {
        if(! linesNames.contains(valueName)){
          throw incorrectDef(s"Node  have no value '$valueName', available: $linesNames")}}
      case AllProbInference(_, p) ⇒
      case ValueInference(_, p) ⇒
      case NodeEvidence(_, v) ⇒
      case _:AllInference ⇒
      case _:AllValuesInference ⇒}
    //Return data
    CPTData(vertex, linesNames, columnsIndexes)}
  private def loadNet():Unit = {
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
    beliefNet = Some(bayesNet)
    //Map nodes to Prop probabilities, evidences, inferences
    probabilities = probabilities.map{case n:NoneVar  ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}
    evidences = evidences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}
    inferences = inferences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}}
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
      case (Some((frame,graph)),Some(net)) ⇒ {
        buildGraph(graph,net)}
      case _ ⇒ }}
  private def updateUI(
    net:BeliefNetwork,
    evds:List[(HuginNode, String)],
    vars:Map[String, List[(String, Double)]])
  :Unit = ui.foreach{ case (_, garaph) ⇒{


    println("updateUI")

    //    showInference:Boolean = true,             //Shove|Hide node inference values and prop
    //    showCPT:Boolean = false,                  //Shove|Hide node CPT

  }



  }
  private def doCalc(evidence:Map[String,String]):Map[String, Map[String, Double]] = beliefNet match{
    case Some(net) ⇒ {
      //Functions
      def checkOutOfBonds(p:Double, nodeID:String):Unit = if(p > 1 || p < 0){
        throw new ExecutionException(s"Error: Prob value '$p' for node '$nodeID' is out of bounds 1 >= v >= 0.")}
      def checkOutOfSun(ps:List[List[Double]], nodeID:String):Unit = ps.zipWithIndex.foreach{
        case (c,i) if c.sum != 1.0 ⇒
          throw new ExecutionException(s"Error: Sum of column '$i' in node '$nodeID' not equals 1.")
        case _ ⇒}
      def normColumn(ps:List[Double]):List[Double] = ps.sum match {
        case 0.0 ⇒ ps.map(_ ⇒ 0.0)
        case s ⇒ ps.map(e ⇒ e / s)}
      def normTable(n:Int, ps:List[Double]):List[List[Double]] = { //Return columns
        val sl = ps.size / n
        def split(ps:List[Double]):List[List[Double]] = ps match{
          case Nil ⇒ List()
          case l ⇒ ps.take(sl) +: split(ps.drop(sl))}
        split(ps).transpose.map(c ⇒ normColumn(c))}
      def updateColumn(v:HuginNode, n:Int, ci:Int, data:Array[Double]):Unit = {
        val cpd = v.getCPTShell.getCPT
        (n * ci until n * ci + data.length).zip(data).foreach{case (i, d) ⇒ cpd.setCP(i,d)}}
      //Get and set probabilities
      probabilities.foreach{
        case n:BinTableProb if n.node.nonEmpty ⇒ {
          val CPTData(v, _, _) = n.node.get
          val p = n.v()
          checkOutOfBonds(p, n.nodeID)
          v.getCPTShell.getCPT.setValues(Array(p, 1.0 - p))}
        case n:TableProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, _) = n.node.get
          val ps = n.vs.map(_()).toList
          ps.foreach(p ⇒ checkOutOfBonds(p, n.nodeID))
          val nps = normTable(ls.size, ps)
          checkOutOfSun(nps, n.nodeID)
          v.getCPTShell.getCPT.setValues(nps.flatMap(e ⇒ e).toArray)}
        case n:BinColumnProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
          val p = n.v()
          checkOutOfBonds(p, n.nodeID)
          val ci = cs(n.colName)
          updateColumn(v, ls.size, ci, Array(p, 1.0 - p))}
        case n:ColumnProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
          val ps = n.vs.map(_()).toList
          ps.foreach(p ⇒ checkOutOfBonds(p, n.nodeID))
          val nps = normColumn(ps)
          checkOutOfSun(List(nps), n.nodeID)
          val ci = cs(n.colName)
          updateColumn(v, ls.size, ci, nps.toArray)}
        case _ ⇒}
      //Get evidence
      val evds = evidences.flatMap{
        case  n:NodeEvidence if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
            n.v() match{
             case "none" ⇒ None
             case s if ls.contains(s) ⇒ Some((v,s))
             case s ⇒ throw new ExecutionException(s"Error: Incorrect evidence value '$s' for node '${n.nodeID}'.")}}
        case _ ⇒ None}
      //Calc
      val engine = dynamator.manufactureInferenceEngine(net)
      net.getEvidenceController.setObservations(evds.toMap.asJava)
      val vars = net.vertices().toArray.flatMap{
        case v:HuginNode ⇒ {
          val ls = v.instances.asInstanceOf[java.util.List[String]].asScala.toList
          val ps = engine.conditional(v).dataclone().toList
          Some((v.getID, ls.zip(ps)))}
        case _ ⇒ None}.toMap
      val varsMap = vars.map{case(k, m) ⇒ (k, m.toMap)}
      //Call inferences
      inferences.foreach{
        case BinaryInference(id, p) ⇒ p(vars(id).head._2)
        case ValueProbInference(id, valueName, p) ⇒ p(varsMap(id)(valueName))
        case AllProbInference(id, p) ⇒ p(varsMap(id))
        case ValueInference(id, p) ⇒ p(varsMap(id).maxBy(_._2)._1)
        case AllInference(p) ⇒ p(varsMap)
        case AllValuesInference(p) ⇒ p(varsMap.map{case (k, m) ⇒ (k, m.maxBy(_._2)._1)})}
      //Update UI
      updateUI(net, evds, vars)
      //Return
      varsMap}
    case _ ⇒ Map()}
  //DSL
  protected implicit def byNameToNoArg[T](v: ⇒ T):()⇒T = {() ⇒ v}
  protected def cpt:ProbNode = new ProbNode
  protected class ProbNode{
    def node(id:String):ProbCPD = new ProbCPD(id)}
  protected class ProbCPD(nodeID:String){
    def binary(cell0: ⇒Double):Unit = {
      probabilities :+= BinTableProb(nodeID, ()⇒{cell0})}
    def column(name0:String, nameN:String*):ProbColumn = new ProbColumn(nodeID, name0 +: nameN)
    def table(cell0:()⇒Double, cellN:(()⇒Double)*):Unit = {
      probabilities :+= TableProb(nodeID, cell0 +: cellN)}}
  protected class ProbColumn(nodeID:String, colName:Seq[String]){
    def binary(cell0: ⇒ Double): Unit = {
      probabilities :+= BinColumnProb(nodeID, colName.toList, ()⇒{cell0})}
    def of(cell0: () ⇒ Double, cellN: (() ⇒ Double)*): Unit = {
      probabilities :+= ColumnProb(nodeID, colName.toList, cell0 +: cellN)}}
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
  //Methods
  /**
   * Do update
   */
  def update():Unit = doCalc(Map())
  /**
   * Calc values
   * @param evidence - Map(< node name >,< value name >)
   * @return - Map(< node name >,< value name >)
   */
  def values(evidence:Map[String,String]):Map[String,String] =
    doCalc(evidence).map{case (k, m) ⇒ (k, m.maxBy(_._2)._1)}
  /**
   * Calc probabilities
   * @param evidence - Map(< node name >,< value name >)
   * @return - Map(< node name >,Map(< value name >,< probability >))
   */
  def probabilities(evidence:Map[String,String]):Map[String, Map[String, Double]] = doCalc(evidence)
  //Construction
  if(netPath == ""){
    throw new SyntaxException("Error: 'netPath' should not be empty.")}
  //UI
  ui = showUI match{
    case true ⇒ {
      val uiGraph = new SimpleGraph(uiParams, screenW, screenH, false, true)
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
        case (Some((frame,graph)),Some(net)) ⇒ {
          buildGraph(graph,net)
          frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
        case _ ⇒ }}
    def update() = if(autoUpdate){
      doCalc(Map())}
    def stop() = {
      ui.foreach{case (frame,_) ⇒ frame.hide()}}}}