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

package mathact.tools.pots

import scalafx.scene.Scene
import scalafx.scene.paint.Color
import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkOut
import mathact.core.bricks.plumbing.wiring.obj._
import mathact.core.bricks.ui.BlockUI
import mathact.data.basic.SingleValue
import mathact.data.ui.{C, E}
import mathact.parts.ui.SetPointDouble
import mathact.tools.Tool


/** Setting dial, sent SingleValue on each value update
  * Created by CAB on 03.12.2016.
  */

abstract class SettingDial(implicit blockContext: BlockContext)
extends Tool(blockContext, "SD", "mathact/tools/pots/setting_dial.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkOut[SingleValue]{
  //Parameters
  val elemsHeight: Int = 25
  val spinnerWidth: Int = 100
  val sliderWidth: Int = 300
  val defaultInit: Double = 0
  val defaultMin: Double = -1
  val defaultMax: Double = 1
  //Variables
  @volatile private var _init = defaultInit
  @volatile private var _min = defaultMin
  @volatile private var _max = defaultMax
  //UI definition
  private class PotUI extends SfxFrame{
    //Params
    title = "Setting dial" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Bounds
    val minVal = _min
    val maxVal = if(_max > minVal) _max else minVal + 1
    val initVal = if(_init < minVal) minVal else if(_init > maxVal) maxVal else _init
    val valStep = (maxVal - minVal).abs / 100
    //Components
    val setPoint = new SetPointDouble(
      elemsHeight,
      spinnerWidth,
      sliderWidth,
      minVal,
      maxVal,
      initVal,
      valStep,
      v ⇒ sendEvent(E.DoubleValueChanged(v)))
    //Scene
    scene = new Scene{
      fill = Color.White
      root = setPoint}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        sendEvent(E.DoubleValueChanged(initVal))
        setPoint.active()
      case C.Stop ⇒
        setPoint.passive()}}
  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }
  //Flows
  private val outflow = new Outflow[SingleValue]{ def riseEvent(value: Double): Unit = pour(SingleValue(value)) }
  //UI registration and events handling
  UI(new PotUI)
  UI.onEvent{ case E.DoubleValueChanged(newVal) ⇒ outflow.riseEvent(newVal) }
  //DSL
  def init: Double = _init
  def init_=(v: Double){ _init = v}
  def min: Double = _min
  def min_=(v: Double){ _min = v}
  def max: Double = _max
  def max_=(v: Double){ _max = v}
  //Connection points
  val out = Outlet[SingleValue](outflow)}