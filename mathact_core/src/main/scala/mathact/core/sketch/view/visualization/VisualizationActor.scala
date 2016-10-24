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

import akka.actor.ActorRef
import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.VisualizationConfigLike
import mathact.core.model.data.visualisation.{InletInfo, OutletInfo, BlockInfo}
import mathact.core.model.messages.M


/** SketchData visualization actor
  * Created by CAB on 31.08.2016.
  */


private[core] class VisualizationActor(config: VisualizationConfigLike, workbenchController: ActorRef)
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
  private var constructedInfo = List[(BlockInfo, Option[BlockImageData])]()
  private var connectedInfo = List[(OutletInfo, InletInfo)]()
  private var currentLayoutType = buildingLayoutType
  //Construction
  private val window = runNow{ new VisualizationViewAndController(config, self, log) }
  //Functions
  private def prepareBlockInfo(builtInfo: BlockInfo): (BlockInfo, Option[BlockImageData]) = {
    //Check image
    val imageData = builtInfo.blockImagePath.flatMap{ path ⇒
      try{
        val img = ImageIO.read(getClass.getClassLoader.getResource(path))
        Some(BlockImageData("/" + path, img.getWidth, img.getHeight))}
      catch{ case err: Throwable ⇒
        log.error(
          err,
          s"[VisualizationActor.prepareBlockInfo] Can't load custom block image for path: $path, default will used.")
        None}}
    //Return
    (builtInfo, imageData)}
  private def buildGraphView(
    constructedInfo: List[(BlockInfo, Option[BlockImageData])],
    connectedInfo: List[(OutletInfo, InletInfo)])
  :GraphData = {
    //Build blocks
    val blocks = constructedInfo.map{ case (builtInfo, imageData) ⇒ BlockData(
      builtInfo.blockId,
      builtInfo.blockName,
      imageData,
      builtInfo.inlets.map(d ⇒ (d.inletId, d.inletName)).toMap,
      builtInfo.outlets.map(d ⇒ (d.outletId, d.outletName)).toMap)}
    //Build connections
    val connections = connectedInfo.flatMap{ case (outlet, inlet) ⇒
      blocks.exists(_.blockId == outlet.blockId) && blocks.exists(_.blockId == outlet.blockId) match{
        case true ⇒
          Some(ConnectionData(inlet.blockId, inlet.inletId, outlet.blockId, outlet.outletId))
        case false ⇒
          None}}
    //Return
    GraphData(blocks, connections)}
  private def buildAndUpdateGraph(
    constructedInfo: List[(BlockInfo, Option[BlockImageData])],
    connectedInfo: List[(OutletInfo, InletInfo)],
    doUpdate: Boolean,
    morphingSteps: Int,
    morphingDelay: Int)
  :Unit = {
    doUpdate match {
      case true ⇒
        val graphData = buildGraphView(constructedInfo, connectedInfo)
        log.debug(
          s"[VisualizationActor.buildAndUpdateGraph] morphingSteps: $morphingSteps, morphingDelay: $morphingDelay, " +
          s"built graph data: $graphData")
        //Update view
        runAndWait{
          window.drawGraph(graphData)
          window.doLayout(buildingLayoutType, morphingSteps, morphingDelay)}
      case false ⇒
        log.debug("[VisualizationActor.buildAndUpdateGraph] UI not show or allBlockBuilt, skip building of graph data.")}}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowVisualizationUI ⇒
      runAndWait{window.show()}
      isShow = true
      buildAndUpdateGraph(constructedInfo, connectedInfo, ! isFinalGraphBuilt, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
      isFinalGraphBuilt = true
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Close button hit
    case DoClose ⇒
      runAndWait(window.hide())
      isShow = false
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Block constructed info, sends each time some block done building
    case M.BlockConstructedInfo(builtInfo) ⇒
      //Add toll info
      constructedInfo +:= prepareBlockInfo(builtInfo)
      //Build view if visible
      buildAndUpdateGraph(constructedInfo, connectedInfo, isShow, buildingLayoutMorphingSteps, buildingLayoutMorphingDelay)
    //Block connected info, sends each time some block done building
    case M.BlocksConnectedInfo(outletInfo, inletInfo) ⇒
      //Add toll info
      connectedInfo +:= (outletInfo → inletInfo)
      //Build view if visible
      buildAndUpdateGraph(constructedInfo, connectedInfo, isShow, buildingLayoutMorphingSteps, buildingLayoutMorphingDelay)
    //AllBlockBuilt, validate graph structure
    case M.AllBlockBuilt ⇒
      //Finale build and layout
      buildAndUpdateGraph(constructedInfo, connectedInfo, isShow, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
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
      workbenchController ! M.VisualizationUIChanged(isShow)}
  //Cleanup
  def cleanup(): Unit = {
    log.debug(s"[VisualizationActor.cleanup] Actor stopped, close UI.")
    runLater(window.close())}}
