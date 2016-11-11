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

package mathact.core.gui.ui

import mathact.core.bricks.ui.interaction.UIEvent


/** BlockUI interface
  * Created by CAB on 31.10.2016.
  */

private[core] trait BlockUILike {
  private[core] def uiInit(): Unit
  private[core] def uiCreate(): Unit
  private[core] def uiLayout(windowId: Int, x: Double, y: Double): Unit
  private[core] def uiEvent(event: UIEvent): Unit
  private[core] def uiShow(): Unit
  private[core] def uiHide(): Unit
  private[core] def uiClose(): Unit}
