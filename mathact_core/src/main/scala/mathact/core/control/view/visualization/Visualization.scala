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

import scalafx.scene.image.Image


/** Visualization object
  * Created by CAB on 28.09.2016.
  */

object Visualization {
  //Enums
  object LayoutType extends Enumeration {
    val OrganicLayout = Value
    val CircleLayout = Value
    val TreeLayout = Value
    val ParallelEdgeLayout = Value
    val StackLayout = Value}
  type LayoutType = LayoutType.Value
  //Data
  case class ToolImageData(
    width: Int,
    height: Int,
    patch: String)
  case class ToolData(
    toolId: Int,
    toolName: String,
    toolImage: Option[ToolImageData],
    inlets: Map[Int, Option[String]],
    outlets: Map[Int, Option[String]])
  case class ConnectionData(
    inletToolId: Int,
    inletId: Int,
    outletToolId: Int,
    outletId: Int)
  case class GraphData(tools: List[ToolData], connections: List[ConnectionData])
  //Messages
  case object DoClose
  case class LayoutTypeChanced(layoutType: LayoutType)
  case object DoLayoutBtnHit}
