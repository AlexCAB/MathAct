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

package mathact.core.bricks.ui.parts

import scalafx.Includes._
import scalafx.scene.control.Button
import scalafx.scene.image.{ImageView, Image}


/** Button with icon
  * Created by CAB on 11.11.2016.
  */

class IconButton(enabledImg: Image, disabledImg: Image)(action: ⇒Unit) extends Button {
  //Images
  private val eView = new ImageView{ image =  enabledImg }
  private val dView = new ImageView{ image =  disabledImg }
  //Config
  graphic = dView
  disable = true
  onAction = handle{
    passive()
    action }
  //Methods
  def active(): Unit = {
    graphic = eView
    disable = false}
  def passive(): Unit = {
    graphic = dView
    disable = true}}
