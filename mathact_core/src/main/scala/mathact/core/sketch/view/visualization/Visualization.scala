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


/** Visualization object
  * Created by CAB on 28.09.2016.
  */

private [mathact] object Visualization {
  //Enums
  object LayoutType extends Enumeration {
    val OrganicLayout = Value
    val CircleLayout = Value
    val TreeLayout = Value
    val ParallelEdgeLayout = Value
    val StackLayout = Value}
  type LayoutType = LayoutType.Value
  //Data
  case class BlockImageData(
    path: String,
    width: Int,
    height: Int)
  case class BlockData(
    blockId: Int,
    blockName: String,
    blockImage: Option[BlockImageData],
    inlets: Map[Int, Option[String]],
    outlets: Map[Int, Option[String]])
  case class ConnectionData(
    inletBlockId: Int,
    inletId: Int,
    outletBlockId: Int,
    outletId: Int)
  case class GraphData(blocks: List[BlockData], connections: List[ConnectionData])
  //Messages
  case object DoClose
  case class LayoutTypeChanced(layoutType: LayoutType)
  case object DoLayoutBtnHit}
