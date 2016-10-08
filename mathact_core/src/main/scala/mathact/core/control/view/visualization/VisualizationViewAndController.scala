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

package mathact.core.control.view.visualization

import java.awt.{Color, BorderLayout, Dimension}
import java.util
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.{Observable, InvalidationListener}
import javax.swing.{JTextArea, JButton, JPanel, SwingUtilities}

import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import com.mxgraph.util.mxConstants._
import mathact.core.model.config.VisualizationConfigLike
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.ActorRef
import akka.event.LoggingAdapter
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.SketchUIElement._
import mathact.core.model.enums.SketchUiElemState._
import mathact.core.model.enums._
import mathact.core.model.messages.M

import scalafx.Includes._
import scalafx.embed.swing.SwingNode
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color._
import scalafx.scene.{Group, Scene, Node}
import scalafx.scene.control.{ToolBar, TextArea, Button}
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout._
import scalafx.scene.text.Text
import scalafx.stage.Stage

import collection.JavaConverters._


/** Visualization view and controller
  * Created by CAB on 28.09.2016.
  */

class VisualizationViewAndController(
  config: VisualizationConfigLike,
  visualizationActor: ActorRef,
  log: LoggingAdapter)
extends Stage { import Visualization._
  //Params
  val windowTitle = "MathAct - Visualization"
  val windowMinWidth = 400
  val windowMinHeight = 300
  val graphBackgroundColor = Color.WHITE
  val toolNodeSize = 20
  val toolNodeStyle = Map[String, Any](
    STYLE_SHAPE          → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR      → "#ffffff",
    STYLE_STROKECOLOR    → "#000000",
    STYLE_FONTCOLOR      → "#000000",
    STYLE_LABEL_POSITION → ALIGN_RIGHT,
    STYLE_ALIGN          → ALIGN_LEFT)
  val inletNodeSize = 10
  val inletNodeStyle = Map[String, Any](
    STYLE_SHAPE          → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR      → "#ffffff",
    STYLE_STROKECOLOR    → "#000000",
    STYLE_FONTCOLOR      → "#000000",
    STYLE_LABEL_POSITION → ALIGN_RIGHT,
    STYLE_ALIGN          → ALIGN_LEFT)
  val outletNodeSize = 8
  val outletNodeStyle = Map[String, Any](
    STYLE_SHAPE          → SHAPE_ELLIPSE,
    STYLE_FILLCOLOR      → "#000000",
    STYLE_STROKECOLOR    → "#000000",
    STYLE_FONTCOLOR      → "#000000",
    STYLE_LABEL_POSITION → ALIGN_RIGHT,
    STYLE_ALIGN          → ALIGN_LEFT)
  val pipeEdgeStyle = Map[String, Any](
    STYLE_STARTARROW     → NONE,
    STYLE_ENDARROW       → NONE,
    STYLE_ORTHOGONAL     → false,
    STYLE_STROKECOLOR    → "#000000",
    STYLE_STROKEWIDTH    → 2,
    STYLE_FONTCOLOR      → "#000000",
    STYLE_LABEL_POSITION → ALIGN_RIGHT,
    STYLE_ALIGN          → ALIGN_LEFT)
  val connectionEdgeStyle = Map[String, Any](
    STYLE_STARTARROW     → NONE,
    STYLE_ENDARROW       → ARROW_CLASSIC,
    STYLE_ORTHOGONAL     → false,
    STYLE_STROKECOLOR    → "#000000",
    STYLE_STROKEWIDTH    → 1,
    STYLE_FONTCOLOR      → "#000000",
    STYLE_LABEL_POSITION → ALIGN_RIGHT,
    STYLE_ALIGN          → ALIGN_LEFT)
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
  private val graph = new mxGraph()
  private val parent = graph.getDefaultParent
  graph.getStylesheet.putCellStyle("TOOL", buildStyle(toolNodeStyle))
  graph.getStylesheet.putCellStyle("INLET", buildStyle(inletNodeStyle))
  graph.getStylesheet.putCellStyle("OUTLET", buildStyle(outletNodeStyle))
  graph.getStylesheet.putCellStyle("PIPE", buildStyle(pipeEdgeStyle))
  graph.getStylesheet.putCellStyle("CONNECTION", buildStyle(connectionEdgeStyle))



  //TODO 1) Зделвть граф не редактируемым.
  //TODO 2) Добавить алгоритм размещения нод.
  //TODO 3) Собственно заполение даннымию
  //TODO
  //TODO
  //TODO
  //TODO



  graph.getModel.beginUpdate()
  try
  {
    val v1 = graph.insertVertex(parent, null, "Hello", 10, 10, toolNodeSize, toolNodeSize, "TOOL")
    val v2 = graph.insertVertex(parent, null, "Hello", 50, 50, inletNodeSize, inletNodeSize, "INLET")
    val v3 = graph.insertVertex(parent, null, "Hello", 100, 100, outletNodeSize, outletNodeSize, "OUTLET")
    val v4 = graph.insertVertex(parent, null, "Hello", 170, 170, toolNodeSize, toolNodeSize, "TOOL")




    graph.insertEdge(parent, null, "Edge", v1, v2, "PIPE")
    graph.insertEdge(parent, null, "Edge", v2, v3, "CONNECTION")
    graph.insertEdge(parent, null, "Edge", v3, v4, "PIPE")



  }
  finally
  {
    graph.getModel.endUpdate()
  }





  //UI Components


  //TODO


  //UI
  title = windowTitle
  scene = new Scene {
    fill = White
    resizable = true
    minWidth = windowMinWidth
    minHeight = windowMinHeight
    root = new BorderPane{
      top = new ToolBar{
        items = Seq(new Button("Hit me!"))}
      center = new SwingNode{
        SwingUtilities.invokeLater(new Runnable{ override def run(): Unit = {
          val graphComponent = new mxGraphComponent(graph)
          graphComponent.getViewport.setOpaque(true)
          graphComponent.getViewport.setBackground(graphBackgroundColor)
          content = graphComponent}})}}}
  //






}
