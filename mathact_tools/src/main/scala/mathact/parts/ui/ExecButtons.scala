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

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.Image
import scalafx.scene.layout.HBox


/** Set of start, stop and step buttons
  * Created by CAB on 03.12.2016.
  */

class ExecButtons(btnSize: Int, onStart: ⇒Unit, onStop: ⇒Unit, onStep: ⇒Unit)
extends HBox(2) {
  //Resources
  val startEImg = new Image("mathact/parts/ui/start_e.png", btnSize, btnSize, true, true)
  val startDImg = new Image("mathact/parts/ui/start_d.png", btnSize, btnSize, true, true)
  val stopEImg = new Image("mathact/parts/ui/stop_e.png", btnSize, btnSize, true, true)
  val stopDImg = new Image("mathact/parts/ui/stop_d.png", btnSize, btnSize, true, true)
  val stepEImg = new Image("mathact/parts/ui/step_e.png", btnSize, btnSize, true, true)
  val stepDImg = new Image("mathact/parts/ui/step_d.png", btnSize, btnSize, true, true)
  //Components
  val startBtn: IconButton  = new IconButton(btnSize, startEImg, startDImg)({
    stopBtn.active()
    onStart})
  val stopBtn: IconButton = new IconButton(btnSize, stopEImg, stopDImg)({
    startBtn.active()
    onStop})
  val stepBtn: IconButton = new IconButton(btnSize, stepEImg, stepDImg)({
    stepBtn.active()
    onStep})
  //Construction
  alignment = Pos.Center
  prefHeight = btnSize
  prefWidth = btnSize * 3
  padding = Insets(4.0)
  children = Seq(startBtn, stopBtn, stepBtn)
  //Methods
  def active(): Unit = {
    startBtn.active()
    stopBtn.passive()
    stepBtn.active()}
  def passive(): Unit = {
    startBtn.passive()
    stopBtn.passive()
    stepBtn.passive()}}
