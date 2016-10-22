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


/** Block built info
  * Created by CAB on 15.05.2016.
  */

case class BlockBuiltInfo(
  blockId: Int,
  blockName: String,
  blockImagePath: Option[String],
  inlets: Map[Int, InletConnectionsInfo],
  outlets: Map[Int, OutletConnectionsInfo])
{
  override def toString =
    s"InletConnectionsInfo(blockId: $blockId, blockName: $blockName, blockImagePath: $blockImagePath, " +
    s"inlets: $inlets, outlets: $outlets)"}
