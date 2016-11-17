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
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStop, ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.{E, C}
import mathact.data.{TimedEvent, TimedValue}
import mathact.tools.Tool
import scalafx.scene.Scene
import scalafx.scene.control.{Spinner, SpinnerValueFactory, Slider}
import scalafx.scene.layout.HBox
import scalafx.geometry.{Insets, Pos}
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
    val spinner: Spinner[Double]  = new Spinner[Double]{
      prefHeight = elemsHeight
      prefWidth = spinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{
        Option(slider).foreach(_.value = this.value.value)
        sendEvent(E.DoubleValueChanged(this.value.value))}
      valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minVal, maxVal, initVal, valStep)
        .asInstanceOf[SpinnerValueFactory[Double]]}
    val slider: Slider = new Slider{
      min = minVal
      max = maxVal
      value = initVal
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = (maxVal - minVal).abs / 10
      minorTickCount = 4
      blockIncrement = valStep
      prefHeight = elemsHeight
      prefWidth = sliderWidth
      disable = true
      value.onChange{ spinner.valueFactory.value.setValue(this.value.value) }}
    //Scene
    scene = new Scene{
      fill = White
      root = new HBox {
          alignment = Pos.Center
          children = Seq(
            new HBox(2) {
              alignment = Pos.Center
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              children = spinner},
            new HBox {
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              alignment = Pos.Center
              children = slider})}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        sendEvent(E.DoubleValueChanged(initVal))
        spinner.disable = false
        slider.disable = false
      case C.Stop ⇒
        spinner.disable = true
        slider.disable = true}}
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
