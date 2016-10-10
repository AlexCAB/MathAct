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

package mathact.core.model.data.visualisation

import scalafx.scene.image.Image


/** Tool built info
  * Created by CAB on 15.05.2016.
  */

case class ToolBuiltInfo(
  toolId: Int,
  toolName: String,
  toolImagePath: Option[String],
  inlets: Map[Int, InletConnectionsInfo],
  outlets: Map[Int, OutletConnectionsInfo])
{
  override def toString =
    s"InletConnectionsInfo(toolId: $toolId, toolName: $toolName, toolImagePath: $toolImagePath, " +
    s"inlets: $inlets, outlets: $outlets)"}
