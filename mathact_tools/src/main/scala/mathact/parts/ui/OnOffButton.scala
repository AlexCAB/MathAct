/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

package mathact.parts.ui

import scalafx.Includes._
import scalafx.scene.control.Button
import scalafx.scene.image.{ImageView, Image}

/** On and Off button
  * Created by CAB on 20.11.2016.
  */

class OnOffButton(onImg: Image, offImg: Image, disabledImg: Image)(doOn: ⇒Unit, doOff: ⇒Unit) extends Button {
  //Images
  private val onView = new ImageView{ image = onImg }
  private val offView = new ImageView{ image = offImg }
  private val dView = new ImageView{ image = disabledImg }
  //Variables
  private var isOnS = false
  //Config
  graphic = dView
  disable = true
  onAction = handle{
    passive()
    if (isOnS) doOn else doOff}
  //Methods
  def on(): Unit = {
    graphic = onView
    disable = false
    isOnS = true}
  def off(): Unit = {
    graphic = offView
    disable = false
    isOnS = false}
  def passive(): Unit = {
    graphic = dView
    disable = true}
  def isOn: Boolean = isOnS}