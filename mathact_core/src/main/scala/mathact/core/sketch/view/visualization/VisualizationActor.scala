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
import mathact.core.model.data.visualisation.BlockBuiltInfo
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
  private var builtBlocks = List[(BlockBuiltInfo, Option[BlockImageData])]()
  private var currentLayoutType = buildingLayoutType
  //Construction
  private val window = runNow{ new VisualizationViewAndController(config, self, log) }
  //Functions
  private def prepareBlockInfo(builtInfo: BlockBuiltInfo): (BlockBuiltInfo, Option[BlockImageData]) = {
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
  private def buildGraphView(builtBlocks: List[(BlockBuiltInfo, Option[BlockImageData])]): GraphData = {
    //Build blocks
    val blocks = builtBlocks.map{ case (builtInfo, imageData) ⇒ BlockData(
      builtInfo.blockId,
      builtInfo.blockName,
      imageData,
      builtInfo.inlets.map{ case (id, d) ⇒ (id, d.inletName) },
      builtInfo.outlets.map{ case (id, d) ⇒ (id, d.outletName) })}
    //Build connections
    val connections = builtBlocks.flatMap{ case (builtInfo, _) ⇒
      builtInfo.outlets.flatMap{ case (outletId, data) ⇒
        data.subscribers.flatMap{
          case subscriber if blocks.exists(_.blockId == subscriber.blockId) ⇒
            Some(ConnectionData(subscriber.blockId, subscriber.inletId, builtInfo.blockId, outletId))
          case _ ⇒
            None}}}
    //Return
    GraphData(blocks, connections)}
  private def buildAndUpdateGraph(
    builtBlocks: List[(BlockBuiltInfo, Option[BlockImageData])],
    doUpdate: Boolean,
    morphingSteps: Int,
    morphingDelay: Int)
  :Unit = {
    doUpdate match {
      case true ⇒
        val graphData = buildGraphView(builtBlocks)
        log.debug(
          s"[VisualizationActor.buildAndUpdateGraph] morphingSteps: $morphingSteps, morphingDelay: $morphingDelay, " +
          s"built graph data: $graphData")
        //Update view
        runAndWait{
          window.drawGraph(graphData)
          window.doLayout(buildingLayoutType, morphingSteps, morphingDelay)}
      case false ⇒
        log.debug("[VisualizationActor.buildAndUpdateGraph] UI not show or allBlockBuilt, skip building of graph data.")}}
  private def verifyGraphStructure(builtBlocks: List[BlockBuiltInfo]): Unit = {
    //Preparing
    val blocks: Map[Int, (Set[Int], Set[Int])] = builtBlocks
      .map(t ⇒ t.blockId → Tuple2(t.inlets.keys.toSet,  t.outlets.keys.toSet))
      .toMap
    //Test
    val testRes = builtBlocks.flatMap{ block ⇒
      block.inlets.flatMap{ case (_, inlet) ⇒
        inlet.publishers.flatMap{ publisher ⇒
          blocks.exists{ case (blockId, (_, outletIds)) ⇒
            publisher.blockId == blockId && outletIds.contains(publisher.outletId)}
          match{
            case true ⇒
              None
            case false ⇒
              val msg = s"Not found outlet with blockId = ${publisher.blockId} and outletId = ${publisher.outletId}, " +
                s"which should be a publishers for inlet with blockId = ${block.blockId} and inletId = ${inlet.inletId}"
              Some(msg)}}} ++
      block.outlets.flatMap{ case (_, outlet) ⇒
        outlet.subscribers.flatMap{ subscriber ⇒
          blocks.exists{ case (blockId, (inletIds, _)) ⇒
            subscriber.blockId == blockId && inletIds.contains(subscriber.inletId)}
          match{
            case true ⇒
              None
            case false ⇒
              val msg = s"Not found inlet with blockId = ${subscriber.blockId} and inletId = ${subscriber.inletId}, " +
                s"which should be a subscriber for outlet with blockId = ${block.blockId} and outletId = ${outlet.outletId}"
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
      buildAndUpdateGraph(builtBlocks, ! isFinalGraphBuilt, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
      isFinalGraphBuilt = true
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Close button hit
    case DoClose ⇒
      runAndWait(window.hide())
      isShow = false
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Block built info, sends each time some block done building
    case M.BlockBuilt(builtInfo) ⇒
      //Add toll info
      builtBlocks +:= prepareBlockInfo(builtInfo)
      //Build view if visible
      buildAndUpdateGraph(builtBlocks, isShow, buildingLayoutMorphingSteps, buildingLayoutMorphingDelay)
    //AllBlockBuilt, validate graph structure
    case M.AllBlockBuilt ⇒
      //Validate graph structure
      verifyGraphStructure(builtBlocks.map(_._1))
      //Finale build and layout
      buildAndUpdateGraph(builtBlocks, isShow, finaleLayoutMorphingSteps, finaleLayoutMorphingDelay)
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
//    //Terminate UI
//    case M.TerminateVisualization ⇒
//      runAndWait(window.close())  //TODO Не ждать или с таймаутом
//      ???
////      workbenchController ! M.VisualizationTerminated
//      self ! PoisonPill
  }
  ???
}