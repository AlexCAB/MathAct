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

import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.util.Locale

import mathact.core.bricks.linking.LinkOut
import akka.actor.{Actor, Props}
import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.data.discrete.TimedEvent
import mathact.data.ui.{C, E}
import mathact.parts.ui.ExecButtons
import mathact.tools.Tool

import scala.concurrent.Future
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Slider}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.paint.Color._


/** Tool to generate discrete timed events
  * Created by CAB on 10.11.2016.
  */

abstract class DiscreteGenerator(implicit blockContext: BlockContext)
extends Tool(blockContext, "DG", "mathact/tools/generators/discrete_generator.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkOut[TimedEvent]{
  //Parameters
  val defaultInitFreq: Double = 2
  val defaultMinFreq: Double = 1
  val defaultMaxFreq: Double = 10
  val defaultSliderStep: Double = 0.1
  val btnSize: Int = 25
  val sliderWidth: Int = 300
  val precision = 1000L
  //Resources
  private val btnIcons = ExecButtons.BtnIcons(
    startEImg = new Image("mathact/tools/generators/start_e.png", btnSize, btnSize, true, true),
    startDImg = new Image("mathact/tools/generators/start_d.png", btnSize, btnSize, true, true),
    stopEImg = new Image("mathact/tools/generators/stop_e.png", btnSize, btnSize, true, true),
    stopDImg = new Image("mathact/tools/generators/stop_d.png", btnSize, btnSize, true, true),
    stepEImg = new Image("mathact/tools/generators/step_e.png", btnSize, btnSize, true, true),
    stepDImg = new Image("mathact/tools/generators/step_d.png", btnSize, btnSize, true, true))
  //Variables
  @volatile private var _initFreq = (defaultInitFreq * precision).toLong
  @volatile private var _minFreq = (defaultMinFreq * precision).toLong
  @volatile private var _maxFreq = (defaultMaxFreq * precision).toLong
  @volatile private var _sliderStep = (defaultSliderStep * precision).toLong
  //UI definition
  private class GenUI extends SfxFrame{
    //Params
    title = "Discrete generator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Helpers
    val decimalFormat = new DecimalFormat("0.###",  new DecimalFormatSymbols(Locale.US))
    //Bounds
    val maxVal = 1000 * precision
    val minFreq = if(_minFreq < 1) 1 else if(_minFreq > maxVal ) maxVal else _minFreq
    val maxFreq = if(_maxFreq < minFreq) minFreq else if(_maxFreq > maxVal) maxVal else _maxFreq
    val initFreq = if(_initFreq < minFreq) minFreq else if(_initFreq > maxFreq) maxFreq else _initFreq
    val freqStep = if (_sliderStep <= 0) 1 else if(_sliderStep > maxVal) maxVal else _sliderStep
    //Functions
    def buildStatus(currentFreq: Double): String =
      s"Frequency(Hz): min = ${decimalFormat.format(minFreq / precision)}, " +
      s"max = ${decimalFormat.format(maxFreq / precision)}, " +
      s"current = ${decimalFormat.format(currentFreq / precision)}"
    //Variables
    private var oldSliderPos = 0L
    //Components
    val startStopStepBtn = new ExecButtons(btnSize, btnIcons, {
      case ExecButtons.Action.Start ⇒ sendEvent(E.Start)
      case ExecButtons.Action.Stop ⇒ sendEvent(E.Stop)
      case ExecButtons.Action.Step ⇒ sendEvent(E.Step)})
    val stateString = new Label{
      text = buildStatus(initFreq)
      style = "-fx-font-size: 11pt;"}
    val speedSlider = new Slider{
      min = minFreq / precision
      max = maxFreq / precision
      value = initFreq / precision
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = freqStep * 10 / precision
      minorTickCount = 4
      blockIncrement = freqStep / precision
      prefHeight = btnSize
      prefWidth = sliderWidth
      disable = true
      value.onChange{
        val rVal = (value.value * precision / freqStep).toLong * freqStep
        if(rVal != oldSliderPos) {
          oldSliderPos = rVal
          stateString.text = buildStatus(rVal)
          sendEvent(E.LongValueChanged((1000 * precision) / rVal))}}}  //Sends in milli seconds
    //Scene
    scene = new Scene{
      fill = White
      root = new BorderPane{
        top =  new HBox {
          alignment = Pos.Center
          children = Seq(
            startStopStepBtn,
            new HBox {
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              alignment = Pos.Center
              children = speedSlider})}
        bottom = new HBox {
          style = "-fx-border-color: #808080; -fx-border-width: 1px; -fx-border-radius: 3.0; " +
            "-fx-border-insets: 2.0 2.0 2.0 2.0;"
          prefHeight
          padding = Insets(1.0)
          children = stateString}}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        sendEvent(E.LongValueChanged((1000 * precision) / initFreq)) //Sends in milli seconds
        startStopStepBtn.active()
        speedSlider.disable = false
      case C.Stop ⇒
        sendEvent(E.Stop)
        startStopStepBtn.passive()
        speedSlider.disable = true}}
  //UI registration
  UI(new GenUI)
  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }
  //Outflow
  private val outflow = new Outflow[TimedEvent]{
    def riseEvent(virtualTime: Long): Unit = pour(
      TimedEvent(virtualTime))}
  //Processor
  private val processor = actorOf(
    Props(new Actor {
      //Variable
      var isStarted = false
      var version = 0L
      var period = 0L
      var lastTime = 0L
      var virtualTime = 0L
      //Functions
      def sysTime: Long = System.currentTimeMillis()
      def sleep(delay: Long, version: Long): Unit =
        try{ if (delay > 0) Thread.sleep(delay) }
        catch{ case _: InterruptedException ⇒ Thread.currentThread().interrupt() }
        finally{ self ! version }
      //Logic
      def newTick(): Unit = {
        val delay = (lastTime + period) - sysTime //Difference between next time and current time
        lastTime += period  //Next last time
        virtualTime += period
        if(delay > 10) Future{ sleep(delay, version) } else sleep(delay, version)}
      def restart(): Unit = {
        version += 1
        lastTime = sysTime
        isStarted = true}
      def stop(): Unit = {
        isStarted = false
        version += 1}
      def update(newPeriod: Long): Unit = {
        period = newPeriod}
      //Receive
      def receive = {
        case E.Start ⇒
          restart()
          outflow.riseEvent(virtualTime)
          newTick()
        case E.Stop ⇒
          stop()
        case E.Step ⇒
          virtualTime += period
          outflow.riseEvent(virtualTime)
        case E.LongValueChanged(newVal) if isStarted ⇒
          update(newVal)
          restart()
          outflow.riseEvent(virtualTime)
          newTick()
        case E.LongValueChanged(newVal) ⇒
          update(newVal)
        case pv: Long if pv == version && isStarted ⇒
          outflow.riseEvent(virtualTime)
          newTick()}}),
    "DiscreteGeneratorProcessor")
  UI.onEvent{ case e ⇒ processor ! e }
  //DSL
  def initFrequency: Double = _initFreq / precision
  def initFrequency_=(v: Double){ _initFreq = (v * precision).toLong}
  def minFrequency: Double = _minFreq / precision
  def minFrequency_=(v: Double){ _minFreq = (v * precision).toLong}
  def maxFrequency: Double = _maxFreq / precision
  def maxFrequency_=(v: Double){ _maxFreq = (v * precision).toLong}
  def sliderStep: Double = _sliderStep / precision
  def sliderStep_=(v: Double){ _sliderStep = (v * precision).toLong}
  //Output
  val out = Outlet[TimedEvent](outflow)}
