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

package mathact.tools.generators

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkOut
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStop, ObjOnStart, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UIEvent
import mathact.data.analog.Sample
import mathact.data.ui.C
import mathact.tools.Tool

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, SpinnerValueFactory, Spinner}
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color


/** Tool to generate analog signal defined by function
  * Created by CAB on 19.11.2016.
  */

abstract class AnalogGenerator(implicit blockContext: BlockContext)
extends Tool(blockContext, "AG", "mathact/tools/generators/analog_generator.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkOut[Sample]{
  //Parameters
  val defaultSampleRate: Long = 10  //In hertz
  val minSampleRate: Long = 1
  val maxSampleRate: Long = 1000
  val defaultPeriod: Long = 1000  //In milliseconds
  val minPeriod: Long = 1
  val maxPeriod: Long = 1000000
  val defaultF: Double⇒Double = _ ⇒ 0.0
  val sampleRateStep: Int = 10
  val periodStep: Int = 10
  val uiElemsHeight: Int = 25
  val uiSpinnerWidth: Int = 100
  val uiLabelStyle: String =  "-fx-font-weight: bold; -fx-font-size: 11pt;"
  //Properties
  @volatile private var _sampleRate = defaultSampleRate
  @volatile private var _period = defaultPeriod
  @volatile private var _f = defaultF
  //Definitions
  private case class SampleRateChanged(sampleRate: Long) extends UIEvent
  private case class PeriodChanged(period: Long) extends UIEvent
  //Variables




  //UI definition
  UI{ new SfxFrame{
    //Params
    title = "Analog generator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Bounds
    val sampleRate =
      if(_sampleRate < minSampleRate) minSampleRate else if(_sampleRate > maxSampleRate ) maxSampleRate else _sampleRate
    val period =
      if(_period < minPeriod) minPeriod else if(_period > maxPeriod ) maxPeriod else _period
    //Components
    val spinnerSampleRate = new Spinner[Long]{
      prefHeight = uiElemsHeight
      prefWidth = uiSpinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{sendEvent(SampleRateChanged(this.value.value))}
      valueFactory = new SpinnerValueFactory
        .DoubleSpinnerValueFactory(minSampleRate, maxSampleRate, sampleRate, sampleRateStep)
        .asInstanceOf[SpinnerValueFactory[Long]]}
    val spinnerPeriod = new Spinner[Long]{
      prefHeight = uiElemsHeight
      prefWidth = uiSpinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{sendEvent(PeriodChanged(this.value.value))}
      valueFactory = new SpinnerValueFactory
        .DoubleSpinnerValueFactory(minPeriod, maxPeriod, period, periodStep)
        .asInstanceOf[SpinnerValueFactory[Long]]}





    //Scene
    scene = new Scene{
      fill = Color.White
      root = new HBox(2) {
        padding = Insets(4.0)
        alignment = Pos.Center
        children = Seq(
          new Label{
            text = "Sample rate (Hz) = "
            style = uiLabelStyle},
          spinnerSampleRate,
          new Label{9
            text = ", Period (ms) = "
            style = uiLabelStyle},
          spinnerPeriod)}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
//        sendEvent(E.LongValueChanged((1000 * precision) / initFreq)) //Sends in milli seconds
//        startBtn.active()
//        stepBtn.active()
//        speedSlider.disable = false
      case C.Stop ⇒
//        sendEvent(E.Stop)
//        startBtn.passive()
//        stopBtn.passive()
//        stepBtn.passive()
//        speedSlider.disable = true
    }}}





  //Outflow
  private val outflow = new Outflow[Sample]{
    def riseEvent(): Unit = pour(???)}


  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }

  //UI handling
  UI.onEvent{ case e ⇒ ??? }




  //DSL
  def sampleRate: Long = _sampleRate
  def sampleRate_=(v: Long){ _sampleRate = v }
  def period: Long = _period
  def period_=(v: Long){ _period = v}
  def f: Double⇒Double = _f
  def f_=(v: Double⇒Double){ _f = v}
  //Output
  val out = Outlet[Sample](outflow)}
