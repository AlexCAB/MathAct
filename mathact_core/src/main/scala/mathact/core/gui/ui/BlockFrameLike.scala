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

import mathact.core.bricks.ui.{UICommand, UIEvent}

import scalafx.beans.property.ReadOnlyObjectProperty


/** Block frame interface
  * Created by CAB on 31.10.2016.
  */

private[core] trait BlockFrameLike {










  //Event exchange


  /** Commands processing */
  def onCommand: PartialFunction[UICommand, Unit]


  /** Send event to UI
    * @param event - UIEvent, event to be send */
  def sendEvent(event: UIEvent): Unit


  //Internal API
  private[core] def showFrame(): Unit
  private[core] def hideFrame(): Unit
  private[core] def closeFrame(): Unit

}
