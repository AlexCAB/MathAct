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
import mathact.data.analog.Sample
import mathact.data.ui.{C, E}
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Slider, Spinner, SpinnerValueFactory}
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color.White

/** Simple analog potentiometer
  * Created by CAB on 24.11.2016.
  */

class AnalogPot (implicit context: BlockContext)
extends Tool(context, "AP", "mathact/tools/pots/analog_pot.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkThrough[Sample, Sample]{
  //Parameters
  val elemsHeight: Int = 25
  val spinnerWidth: Int = 100
  val sliderWidth: Int = 300
  val defaultInit: Double = 0.5
  //Variables
  @volatile private var _init = defaultInit
  @volatile private var level = 0.0
  //UI definition
  private class PotUI extends SfxFrame{
    //Params
    title = "Analog pot" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Bounds
    val initLevel = if(_init < 0) 0 else if(_init > 1) 1 else _init
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
      valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, initLevel, 0.1)
        .asInstanceOf[SpinnerValueFactory[Double]]}
    val slider: Slider = new Slider{
      min = 0
      max = 1
      value = initLevel
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 0.1
      minorTickCount = 4
      blockIncrement = 0.1
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
        sendEvent(E.DoubleValueChanged(initLevel))
        spinner.disable = false
        slider.disable = false
      case C.Stop ⇒
        spinner.disable = true
        slider.disable = true}}
  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }
  //Handler
  private val handler = new Outflow[Sample] with Inflow[Sample]{
    protected def drain(s: Sample): Unit = { pour(s.copy(value = s.value * level)) }}
  //UI registration and events handling
  UI(new PotUI)
  UI.onEvent{ case E.DoubleValueChanged(newVal) ⇒ level = newVal }
  //DSL
  def init: Double = _init
  def init_=(v: Double){ _init = v}
  //Connection points
  val in = Inlet[Sample](handler)
  val out = Outlet[Sample](handler)}