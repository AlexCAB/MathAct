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

package mathact.tools.generators

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkOut
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.UIEvent
import mathact.data.analog.Sample
import mathact.data.ui.C
import mathact.parts.ui.OnOffButton
import mathact.tools.Tool

import scala.concurrent.Future
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Spinner, SpinnerValueFactory}
import scalafx.scene.image.Image
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color


/** Tool to generate analog signal defined by function
  * Created by CAB on 19.11.2016.
  */

abstract class AnalogGenerator(implicit blockContext: BlockContext)
extends Tool(blockContext, "AG", "mathact/tools/generators/analog_generator.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkOut[Sample]{
  //Parameters
  val defaultSampleRate: Int = 10  //In hertz
  val minSampleRate: Int = 1
  val maxSampleRate: Int = 1000
  val defaultPeriod: Int = 1000  //In milliseconds
  val minPeriod: Int = 1
  val maxPeriod: Int = 1000000
  val defaultInitOn: Boolean = false
  val defaultF: Double⇒Double = _ ⇒ 0.0
  val sampleRateStep: Int = 10
  val periodStep: Int = 10
  val uiElemsHeight: Int = 25
  val uiBtnSize: Int = 25
  val uiSpinnerWidth: Int = 100
  val uiLabelStyle: String =  "-fx-font-weight: bold; -fx-font-size: 11pt;"
  //Resources
  private val onBtnImg = new Image("mathact/tools/generators/on_off_button_on.png", uiBtnSize, uiBtnSize, true, true)
  private val offBtnImg = new Image("mathact/tools/generators/on_off_button_off.png", uiBtnSize, uiBtnSize, true, true)
  private val disableBtnImg = new Image("mathact/tools/generators/on_off_button_d.png", uiBtnSize, uiBtnSize, true, true)
  //Properties
  @volatile private var _sampleRate = defaultSampleRate
  @volatile private var _period = defaultPeriod
  @volatile private var _initOn = defaultInitOn
  @volatile private var _f = defaultF
  //Definitions
  private case class SetGen(sampleRate: Int, period: Int) extends UIEvent
  private case object StopGen extends UIEvent
  private class Handler extends Outflow[Sample]{def riseEvent(s: Sample): Unit = pour(s) }
  private class Gen(sampleRate: Int, period: Int, function: Double⇒Double, outflow: Handler){
    //Params
    val samplePeriod = 1000 / sampleRate  //In mills
    val sampleTimeStep = period / sampleRate
    //Variables
    @volatile private var work = true
    @volatile private var totalTime = 0L
    @volatile private var prevTime = 0L
    @volatile private var virtualTime = 0L
    @volatile private object Mutex
    //Methods
    def stop(): Unit = {
      work = false
      Mutex.synchronized(Mutex.notifyAll())}
    //Worker
    Future{
      //Set times
      totalTime = 0
      prevTime = System.currentTimeMillis() - samplePeriod
      //Work loop
      while (work) {
        //Eval t and f
        val ct = System.currentTimeMillis()
        val t = (virtualTime % period).toDouble / period
        val f = _f
        //Increase total time
        val delta = ct - prevTime
        totalTime += delta
        prevTime = ct
        //Call user function and rise event
        try{
          val v = f(t)
          outflow.riseEvent(Sample(
            time = virtualTime,
            shift = t,
            value = v))}
        catch{ case e: Throwable ⇒
          logger.error(e, "[AnalogGenerator.Gen.Worker] Error on call of user generator function.")}
        //Increment virtual time
        virtualTime += sampleTimeStep
        //Sleep
        val timeout = samplePeriod + (samplePeriod - delta)
        Mutex.synchronized(Mutex.wait(if(timeout < 1) 1 else timeout))}}}
  //Variables
  @volatile private var currentGen: Option[Gen] = None
  //UI definition
  UI{ new SfxFrame{
    //Params
    val initTitle = "Analog generator" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Bounds
    val sampleRate =
      if(_sampleRate < minSampleRate) minSampleRate else if(_sampleRate > maxSampleRate ) maxSampleRate else _sampleRate
    val period =
      if(_period < minPeriod) minPeriod else if(_period > maxPeriod ) maxPeriod else _period
    //Components
    val onOffBtn: OnOffButton = new OnOffButton(onBtnImg, offBtnImg, disableBtnImg)(
      doOn = {
        onOffBtn.off()
        setGen()},
      doOff = {
        onOffBtn.on()
        setTitle(isOn = false)
        sendEvent(StopGen)})
    val spinnerSampleRate = new Spinner[Int]{
      prefHeight = uiElemsHeight
      prefWidth = uiSpinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{ if(! disable.value && ! onOffBtn.isOn) setGen() }
      valueFactory = new SpinnerValueFactory
        .IntegerSpinnerValueFactory(minSampleRate, maxSampleRate, sampleRate, sampleRateStep)
        .asInstanceOf[SpinnerValueFactory[Int]]}
    val spinnerPeriod = new Spinner[Int]{
      prefHeight = uiElemsHeight
      prefWidth = uiSpinnerWidth
      style = "-fx-font-size: 11pt;"
      disable = true
      editable = true
      value.onChange{ if(! disable.value && ! onOffBtn.isOn) setGen() }
      valueFactory = new SpinnerValueFactory
        .IntegerSpinnerValueFactory(minPeriod, maxPeriod, period, periodStep)
        .asInstanceOf[SpinnerValueFactory[Int]]}
    //Functions
    def setTitle(isOn: Boolean): Unit = { title = initTitle + " | " + (if (isOn) "ON" else "OFF") }
    def setGen(): Unit = {
      setTitle(isOn = true)
      sendEvent( SetGen(spinnerSampleRate.value.value, spinnerPeriod.value.value))}
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
          new Label{
            text = ", Period (ms) = "
            style = uiLabelStyle},
          spinnerPeriod,
          onOffBtn)}
      setTitle(_initOn)}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        spinnerSampleRate.disable = false
        spinnerPeriod.disable = false
        if (_initOn) onOffBtn.off() else onOffBtn.on()
        if (_initOn) sendEvent( SetGen(spinnerSampleRate.value.value, spinnerPeriod.value.value))
      case C.Stop ⇒
        spinnerSampleRate.disable = true
        spinnerPeriod.disable = true
        onOffBtn.passive()
        sendEvent(StopGen)}}}
  //Outflow
  private val outflow = new Handler
  //On start and on stop
  protected def onStart(): Unit = {
    UI.sendCommand(C.Start) }
  protected def onStop(): Unit = {
    currentGen.foreach(_.stop())
    UI.sendCommand(C.Stop)}
  //UI handling
  UI.onEvent{
    case SetGen(sampleRate, period) ⇒
      currentGen.foreach(_.stop())
      currentGen = Some(new Gen(sampleRate, period, _f, outflow))
    case StopGen ⇒
      currentGen.foreach(_.stop())}
  //DSL
  def sampleRate: Int = _sampleRate
  def sampleRate_=(v: Int){ _sampleRate = v }
  def period: Int = _period
  def period_=(v: Int){ _period = v }
  def initOn: Boolean = _initOn
  def initOn_=(v: Boolean){ _initOn = v }
  def f: Double⇒Double = _f
  def f_=(v: Double⇒Double){ _f = v }
  //Output
  val out = Outlet[Sample](outflow)}
