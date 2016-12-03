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

package mathact.tools.pots

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.discrete.{TimedEvent, TimedValue}
import mathact.data.ui.{C, E}
import mathact.parts.ui.SetPointDouble
import mathact.tools.Tool

import scalafx.scene.Scene
import scalafx.scene.paint.Color._


/** Tool for adding of double value to the TimedEvent produced by DiscreteGenerator
  * Created by CAB on 12.11.2016.
  */

class TimedValuesPot(implicit context: BlockContext)
extends Tool(context, "TVP", "mathact/tools/pots/timed_values_pot.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkThrough[TimedEvent, TimedValue]{
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
  @volatile private var currentValue = 0.0
  //UI definition
  private class PotUI extends SfxFrame{
    //Params
    title = "Timed vals pot" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
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
      fill = White
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
  private val outflow = new Outflow[TimedValue]{
    def riseEvent(time: Long): Unit = pour(TimedValue(time, currentValue))}
  private val inflow = new Inflow[TimedEvent]{
    protected def drain(value: TimedEvent): Unit = { outflow.riseEvent(value.time) }}
  //UI registration and events handling
  UI(new PotUI)
  UI.onEvent{ case E.DoubleValueChanged(newVal) ⇒ currentValue = newVal }
  //DSL
  def init: Double = _init
  def init_=(v: Double){ _init = v}
  def min: Double = _min
  def min_=(v: Double){ _min = v}
  def max: Double = _max
  def max_=(v: Double){ _max = v}
  //Connection points
  val in = Inlet[TimedEvent](inflow)
  val out = Outlet[TimedValue](outflow)}
