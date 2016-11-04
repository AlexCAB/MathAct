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

package mathact.core.layout.infrastructure

import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.holders.DriveRef


/** Layout
  * Created by CAB on 04.11.2016.
  */

object Layout {
  //Definitions
  case class WindowData(
    drive: DriveRef,
    windowId: Int,
    preference: WindowPreference,
    var state: WindowState)
  case class WindowPosition(
    drive: DriveRef,
    windowId: Int,
    x: Double,
    y: Double)
  //Algorithms
  def fillAllScreenLayout(windows: Seq[WindowData]): Seq[WindowPosition]  = ???
  def windowsStairsLayout(windows: Seq[WindowData]): Seq[WindowPosition]  = ???



}
