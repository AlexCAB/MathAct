/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.core.sketch.view.visualization

import java.awt.Color
import java.util
import javax.swing.SwingUtilities

import com.mxgraph.layout._
import com.mxgraph.model.mxCell
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.swing.util.mxMorphing
import com.mxgraph.util.{mxEventObject, mxEvent}
import com.mxgraph.util.mxEventSource.mxIEventListener
import com.mxgraph.view.mxGraph
import com.mxgraph.util.mxConstants._
import mathact.core.model.config.VisualizationConfigLike
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.ActorRef
import akka.event.LoggingAdapter

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingNode
import scalafx.scene.paint.Color._
import scalafx.scene.Scene
import scalafx.scene.control.{ChoiceBox, ToolBar, Button}
import scalafx.scene.layout._
import scalafx.stage.Stage


/** Visualization view and controller
  * Created by CAB on 28.09.2016.
  */

//TODO Write some custom layouts.
private [mathact] class VisualizationViewAndController(
  config: VisualizationConfigLike,
  visualizationActor: ActorRef,
  log: LoggingAdapter)
extends Stage { import Visualization._
  //Params
  val windowTitle = "MathAct - Visualization"
  val windowPrefWidth = 800
  val windowPrefHeight = 600
  val graphBackgroundColor = Color.WHITE
  val layoutMorphingEase = 1.2
  val layoutChoiceItems = ObservableBuffer(
    "Organic Layout",
    "Circle Layout",
    "Tree Layout",
    "Parallel Edge Layout",
    "Stack Layout")
  val defaultLayoutChoice = 0 //0 to 4
  val toolNodeSize = 26
  val toolNodeSimpleStyle = Map[String, Any](
    STYLE_SHAPE                   → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR               → "#ffffff",
    STYLE_STROKECOLOR             → "#000000",
    STYLE_FONTCOLOR               → "#000000",
    STYLE_VERTICAL_LABEL_POSITION → ALIGN_BOTTOM,
    STYLE_ALIGN                   → ALIGN_CENTER,
    STYLE_EDITABLE                → 0)
  val toolNodeImageStyle = Map[String, Any](
    STYLE_SHAPE                   → SHAPE_IMAGE,
    STYLE_VERTICAL_LABEL_POSITION → ALIGN_BOTTOM,
    STYLE_ALIGN                   → ALIGN_CENTER,
    STYLE_EDITABLE                → 0)
  val inletNodeSize = 16
  val inletNodeStyle = Map[String, Any](
    STYLE_SHAPE                   → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR               → "#ffffff",
    STYLE_STROKECOLOR             → "#000000",
    STYLE_FONTCOLOR               → "#000000",
    STYLE_VERTICAL_LABEL_POSITION → ALIGN_TOP,
    STYLE_ALIGN                   → ALIGN_CENTER,
    STYLE_EDITABLE                → 0)
  val outletNodeSize = 16
  val outletNodeStyle = Map[String, Any](
    STYLE_SHAPE                   → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR               → "#000000",
    STYLE_STROKECOLOR             → "#000000",
    STYLE_FONTCOLOR               → "#000000",
    STYLE_VERTICAL_LABEL_POSITION → ALIGN_TOP,
    STYLE_ALIGN                   → ALIGN_CENTER,
    STYLE_EDITABLE                → 0)
  val pipeEdgeStyle = Map[String, Any](
    STYLE_STARTARROW     → NONE,
    STYLE_ENDARROW       → NONE,
    STYLE_ORTHOGONAL     → false,
    STYLE_STROKECOLOR    → "#000000",
    STYLE_STROKEWIDTH    → 2,
    STYLE_FONTCOLOR      → "#000000",
    STYLE_NOLABEL        → 1,
    STYLE_EDITABLE       → 0)
  val connectionEdgeStyle = Map[String, Any](
    STYLE_STARTARROW     → NONE,
    STYLE_ENDARROW       → ARROW_CLASSIC,
    STYLE_ORTHOGONAL     → false,
    STYLE_STROKECOLOR    → "#000000",
    STYLE_STROKEWIDTH    → 1,
    STYLE_FONTCOLOR      → "#000000",
    STYLE_NOLABEL        → 1,
    STYLE_EDITABLE       → 0)
  //Close operation
  delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
    def handle(event: WindowEvent): Unit = {
      log.debug("[VisualizationViewAndController.onCloseRequest] Close is hit, send DoClose.")
      visualizationActor ! DoClose
      event.consume()}})
  //Functions
  private def buildStyle(rawStyle: Map[String, Any]): util.Map[String, Object] = {
    val styleMap = new util.HashMap[String,Object]
    rawStyle.foreach{case (k,v) ⇒ styleMap.put(k,v.asInstanceOf[Object])}
    styleMap}
  //Graph
  private val graph = new mxGraph{
    override def isCellSelectable(cell: Any): Boolean = Option(cell) match{
      case Some(c: mxCell) if c.isEdge ⇒ false
      case _ ⇒ super.isCellSelectable(cell)}}
  private val parent = graph.getDefaultParent
  graph.getStylesheet.putCellStyle("TOOL", buildStyle(toolNodeSimpleStyle))
  graph.getStylesheet.putCellStyle("INLET", buildStyle(inletNodeStyle))
  graph.getStylesheet.putCellStyle("OUTLET", buildStyle(outletNodeStyle))
  graph.getStylesheet.putCellStyle("PIPE", buildStyle(pipeEdgeStyle))
  graph.getStylesheet.putCellStyle("CONNECTION", buildStyle(connectionEdgeStyle))
  //Graph component
  val graphComponent = new mxGraphComponent(graph)
  graphComponent.getViewport.setOpaque(true)
  graphComponent.getViewport.setBackground(graphBackgroundColor)
  graphComponent.setConnectable(false)
  graphComponent.setDragEnabled(false)
  //Layouts
  private val organicLayout = new mxFastOrganicLayout(graph)
  private val circleLayout = new mxCircleLayout(graph)
  private val treeLayout = new mxCompactTreeLayout(graph)
  private val parallelEdgeLayout = new mxParallelEdgeLayout(graph)
  private val stackLayout = new mxStackLayout(graph)
  //UI Components
  private val layoutChoice = new ChoiceBox[String](ObservableBuffer(layoutChoiceItems)){
    delegate.getSelectionModel.select(defaultLayoutChoice)
    delegate.getSelectionModel.selectedItemProperty.onChange{
      val newVal = selectionModel.value.selectedIndexProperty.toInt match{
        case 0 ⇒ Some(LayoutType.OrganicLayout)
        case 1 ⇒ Some(LayoutType.CircleLayout)
        case 2 ⇒ Some(LayoutType.TreeLayout)
        case 3 ⇒ Some(LayoutType.ParallelEdgeLayout)
        case 4 ⇒ Some(LayoutType.StackLayout)
        case _ ⇒ None}
      newVal.foreach(v ⇒  visualizationActor ! LayoutTypeChanced(v))}}
  private val doLayoutBtn = new Button("Do Layout!"){
    onAction = handle{ visualizationActor ! DoLayoutBtnHit }}
  //UI
  title = windowTitle
  scene = new Scene {
    fill = White
    resizable = true
    minWidth = 400
    minHeight = 300
    root = new BorderPane{
       prefWidth = windowPrefWidth
       prefHeight = windowPrefHeight
      top = new ToolBar{
        items = Seq(layoutChoice, doLayoutBtn)}
      center = new SwingNode{
        SwingUtilities.invokeLater(new Runnable{ override def run(): Unit = {content = graphComponent}})}}}
  //Methods
  /** Building and displaying of graph
    * @param data - GraphData */
  def drawGraph(data: GraphData): Unit = {
    log.debug("[VisualizationViewAndController.drawGraph] Try to draw graph for data: " + data)
    graph.getModel.beginUpdate()
    try {
      //Clean up
      graph.removeCells(graph.getChildVertices(graph.getDefaultParent))
      //Build tools
      val toolVertices = data.tools.zipWithIndex
        .map{ case (tool, toolIndex) ⇒
          //Preparing
          val toolX = (toolIndex + 1) * toolNodeSize * 3.5
          val toolY = (toolIndex + 1) * toolNodeSize * 1.5
          val (toolStyle, verWidth, verHeight) = tool.toolImage
            .map{ image ⇒
              val styleName = "TOOL_" + tool.toolId
              val style = toolNodeImageStyle + (STYLE_IMAGE → image.path)
              graph.getStylesheet.putCellStyle(styleName, buildStyle(style))
              (styleName, image.width.toDouble, image.height.toDouble)}
            .getOrElse(("TOOL", toolNodeSize.toDouble, toolNodeSize.toDouble))
          //Add tool vertex
          val toolVertex = graph.insertVertex(parent, null, tool.toolName, toolX, toolY, verWidth, verHeight, toolStyle)
          //Add pipes
          def addPipes(pipes: Map[Int, Option[String]], style: String, size: Int, shift: Double): Map[Int, AnyRef] = pipes
            .zipWithIndex
            .map{ case ((pipeId, pipeName), pipeIndex) ⇒
              //Preparing
              val pipeX = toolX + toolNodeSize * shift
              val pipeY = (toolY - (pipes.size - 1) * size) + size * 0.22 + pipeIndex * size * 2
              //Adding vertex
              val pipeVertex = graph.insertVertex(parent, null, pipeName.orNull, pipeX, pipeY, size, size, style)
              //Adding edge
              graph.insertEdge(parent, null, null, toolVertex, pipeVertex, "PIPE")
              //Return
              (pipeId, pipeVertex)}
          val inletVertices = addPipes(tool.inlets, "INLET", inletNodeSize, -0.8)
          val outletVertices = addPipes(tool.outlets, "OUTLET", outletNodeSize, 1.15)
          //Return
          (tool.toolId, (inletVertices, outletVertices))}
        .toMap
      //Build connections
      data.connections.foreach{ con ⇒ graph.insertEdge(
        parent,
        null,
        null,
        toolVertices(con.outletToolId)._2(con.outletId),
        toolVertices(con.inletToolId)._1(con.inletId),
        "CONNECTION")}}
    finally {
      graph.getModel.endUpdate()}}
  /** Applying given layout to the graph
    * @param layoutType - LayoutType */
  def doLayout(layoutType: LayoutType, numberOfIterations: Int, iterationDelay: Int): Unit = {
    log.debug("[VisualizationViewAndController.drawGraph] Try to do layout.")
    SwingUtilities.invokeLater(new Runnable{ override def run(): Unit = {
      graph.getModel.beginUpdate()
      try{
        layoutType match{
          case LayoutType.OrganicLayout ⇒ organicLayout.execute(graph.getDefaultParent)
          case LayoutType.CircleLayout ⇒ circleLayout.execute(graph.getDefaultParent)
          case LayoutType.TreeLayout ⇒ treeLayout.execute(graph.getDefaultParent)
          case LayoutType.ParallelEdgeLayout ⇒ parallelEdgeLayout.execute(graph.getDefaultParent)
          case LayoutType.StackLayout ⇒ stackLayout.execute(graph.getDefaultParent)}}
      finally {
        val morph = new mxMorphing(graphComponent, numberOfIterations, layoutMorphingEase, iterationDelay)
        morph.addListener(mxEvent.DONE, new mxIEventListener() {
          override def invoke(arg0: Object,  arg1: mxEventObject): Unit = graph.getModel.endUpdate()})
        morph.startAnimation()}}})}}
