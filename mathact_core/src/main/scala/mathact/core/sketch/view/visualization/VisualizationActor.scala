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

import javax.imageio.ImageIO

import akka.actor.{PoisonPill, ActorRef}
import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.VisualizationConfigLike
import mathact.core.model.data.visualisation.ToolBuiltInfo
import mathact.core.model.messages.M


/** SketchData visualization actor
  * Created by CAB on 31.08.2016.
  */


private [mathact] class VisualizationActor(config: VisualizationConfigLike, workbenchController: ActorRef)
extends WorkerBase with JFXInteraction { import Visualization._
  //Parameters
  val buildingLayoutType = LayoutType.OrganicLayout
  val buildingLayoutMorphingSteps = 10
  val buildingLayoutMorphingDelay = 5 //Im milli seconds
  val finaleLayoutMorphingSteps = 40
  val finaleLayoutMorphingDelay = 50 //Im milli seconds
  val doLayoutMorphingSteps = 30
  val doLayoutMorphingDelay = 40 //Im milli seconds
  //Variables
  private var isShow = false
  private var isFinalGraphBuilt = false
  private var builtTools = List[(ToolBuiltInfo, Option[ToolImageData])]()
  private var currentLayoutType = buildingLayoutType
  //Construction
  private val window = runNow{ new VisualizationViewAndController(config, self, log) }
  //Functions
  private def prepareToolInfo(builtInfo: ToolBuiltInfo): (ToolBuiltInfo, Option[ToolImageData]) = {
    //Check image
    val imageData = builtInfo.toolImagePath.flatMap{ path ⇒
      try{
        val img = ImageIO.read(getClass.getClassLoader.getResource(path))
        Some(ToolImageData("/" + path, img.getWidth, img.getHeight))}
      catch{ case err: Throwable ⇒
        log.error(
          err,
          s"[VisualizationActor.prepareToolInfo] Can't load custom tool image for path: $path, default will used.")
        None}}
    //Return
    (builtInfo, imageData)}
  private def buildGraphView(builtTools: List[(ToolBuiltInfo, Option[ToolImageData])]): GraphData = {
    //Build tools
    val tools = builtTools.map{ case (builtInfo, imageData) ⇒ ToolData(
      builtInfo.toolId,
      builtInfo.toolName,
      imageData,
      builtInfo.inlets.map{ case (id, d) ⇒ (id, d.inletName) },
      builtInfo.outlets.map{ case (id, d) ⇒ (id, d.outletName) })}
    //Build connections
    val connections = builtTools.flatMap{ case (builtInfo, _) ⇒
      builtInfo.outlets.flatMap{ case (outletId, data) ⇒
        data.subscribers.flatMap{
          case subscriber if tools.exists(_.toolId == subscriber.toolId) ⇒
            Some(ConnectionData(subscriber.toolId, subscriber.inletId, builtInfo.toolId, outletId))
          case _ ⇒
            None}}}
    //Return
    GraphData(tools, connections)}
  private def buildAndUpdateGraph(
    builtTools: List[(ToolBuiltInfo, Option[ToolImageData])],
    doUpdate: Boolean,
    morphingSteps: Int,
    morphingDelay: Int)
  :Unit = {
    doUpdate match {
      case true ⇒
        val graphData = buildGraphView(builtTools)
        log.debug(
          s"[VisualizationActor.buildAndUpdateGraph] morphingSteps: $morphingSteps, morphingDelay: $morphingDelay, " +
          s"built graph data: $graphData")
        //Update view
        runAndWait{
          window.drawGraph(graphData)
          window.doLayout(buildingLayoutType, morphingSteps, morphingDelay)}
      case false ⇒
        log.debug("[VisualizationActor.buildAndUpdateGraph] UI not show or allToolBuilt, skip building of graph data.")}}
  private def verifyGraphStructure(builtTools: List[ToolBuiltInfo]): Unit = {
    //Preparing
    val tools: Map[Int, (Set[Int], Set[Int])] = builtTools
      .map(t ⇒ t.toolId → Tuple2(t.inlets.keys.toSet,  t.outlets.keys.toSet))
      .toMap
    //Test
    val testRes = builtTools.flatMap{ tool ⇒
      tool.inlets.flatMap{ case (_, inlet) ⇒
        inlet.publishers.flatMap{ publisher ⇒
          tools.exists{ case (toolId, (_, outletIds)) ⇒
            publisher.toolId == toolId && outletIds.contains(publisher.outletId)}
          match{
            case true ⇒
              None
            case false ⇒
              val msg = s"Not found outlet with toolId = ${publisher.toolId} and outletId = ${publisher.outletId}, " +
                s"which should be a publishers for inlet with toolId = ${tool.toolId} and inletId = ${inlet.inletId}"
              Some(msg)}}} ++
      tool.outlets.flatMap{ case (_, outlet) ⇒
        outlet.subscribers.flatMap{ subscriber ⇒
          tools.exists{ case (toolId, (inletIds, _)) ⇒
            subscriber.toolId == toolId && inletIds.contains(subscriber.inletId)}
          match{
            case true ⇒
              None
            case false ⇒
              val msg = s"Not found inlet with toolId = ${subscriber.toolId} and inletId = ${subscriber.inletId}, " +
                s"which should be a subscriber for outlet with toolId = ${tool.toolId} and outletId = ${outlet.outletId}"
                Some(msg)}}}}
    //Log result
    testRes match{
      case Nil ⇒
        log.debug("[VisualizationActor.verifyGraphStructure] Structure is valid.")
      case errors ⇒
        log.error(
          "[VisualizationActor.verifyGraphStructure] Structure invalid, next errors found: \n    " +
          errors.mkString("\n    "))}}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowVisualizationUI ⇒
      runAndWait{window.show()}
      isShow = true
      buildAndUpdateGraph(builtTools, ! isFinalGraphBuilt, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
      isFinalGraphBuilt = true
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Close button hit
    case DoClose ⇒
      runAndWait(window.hide())
      isShow = false
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Tool built info, sends each time some tool done building
    case M.ToolBuilt(builtInfo) ⇒
      //Add toll info
      builtTools +:= prepareToolInfo(builtInfo)
      //Build view if visible
      buildAndUpdateGraph(builtTools, isShow, buildingLayoutMorphingSteps, buildingLayoutMorphingDelay)
    //AllToolBuilt, validate graph structure
    case M.AllToolBuilt ⇒
      //Validate graph structure
      verifyGraphStructure(builtTools.map(_._1))
      //Finale build and layout
      buildAndUpdateGraph(builtTools, isShow, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
      isFinalGraphBuilt = isShow
    //Change layout
    case LayoutTypeChanced(layoutType) ⇒
      currentLayoutType = layoutType
    //Do layout
    case DoLayoutBtnHit ⇒
      runAndWait{window.doLayout(currentLayoutType, doLayoutMorphingSteps, doLayoutMorphingDelay)}
    //Hide UI
    case M.HideVisualizationUI ⇒
      runAndWait(window.hide())
      isShow = false
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Terminate UI
    case M.TerminateVisualization ⇒
      runAndWait(window.close())
      ???
//      workbenchController ! M.VisualizationTerminated
      self ! PoisonPill}}