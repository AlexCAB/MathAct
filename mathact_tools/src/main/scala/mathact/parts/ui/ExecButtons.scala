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

package mathact.parts.ui

import mathact.parts.ui.ExecButtons.BtnIcons
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.Image
import scalafx.scene.layout.HBox


/** Set of start, stop and step buttons
  * Created by CAB on 03.12.2016.
  */

object ExecButtons{
  //Definitions
  case class BtnIcons(
    startEImg: Image,
    startDImg: Image,
    stopEImg: Image,
    stopDImg: Image,
    stepEImg: Image,
    stepDImg: Image)
  object Action extends Enumeration {
    val Start, Stop, Step = Value}
  type Action = Action.Value}


class ExecButtons(btnSize: Int, icons: BtnIcons, action: ExecButtons.Action⇒Unit)
extends HBox(2) { import ExecButtons._
  //Components
  val startBtn: IconButton  = new IconButton(icons.startEImg, icons.startDImg)({
    stopBtn.active()
    action(Action.Start)})
  val stopBtn: IconButton = new IconButton(icons.stopEImg, icons.stopDImg)({
    startBtn.active()
    action(Action.Stop)})
  val stepBtn: IconButton = new IconButton(icons.stepEImg, icons.stepDImg)({
    stepBtn.active()
    action(Action.Step)})
  //Construction
  alignment = Pos.Center
  prefHeight = btnSize
  prefWidth = btnSize * 3
  padding = Insets(4.0)
  children = Seq(startBtn, stopBtn, stepBtn)
  //Methods
  def active(): Unit = {
    startBtn.active()
    stepBtn.active()}
  def passive(): Unit = {
    startBtn.passive()
    stopBtn.passive()
    stepBtn.passive()}}
