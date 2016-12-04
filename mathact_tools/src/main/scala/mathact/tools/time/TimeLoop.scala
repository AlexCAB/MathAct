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

package mathact.tools.time

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStart, ObjOnStop, ObjWiring}
import mathact.core.bricks.ui.BlockUI
import mathact.core.bricks.ui.interaction.{UICommand, UIEvent}
import mathact.data.Timed
import mathact.data.ui.{C, E}
import mathact.parts.ui.{ExecButtons, IconButton}
import mathact.tools.Tool

import scala.concurrent.Future
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Slider}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.paint.Color


/** Timed messages loop controller
  * Created by CAB on 03.12.2016.
  */

abstract class TimeLoop[V <: Timed[V]](implicit blockContext: BlockContext)
extends Tool(blockContext, "TL", "mathact/tools/time/time_loop.png")
with ObjWiring with ObjOnStart with ObjOnStop with BlockUI with LinkThrough[V, V]{
  //Parameters
  val defaultStartTime: Long = 0
  val defaultPeriod: Long = 10000000 //In iterations
  val defaultInitTimeout: Long = 10
  val defaultMinTimeout: Long = 0
  val defaultMaxTimeout: Long = 1000
  val sliderStep: Long = 10
  val btnSize: Int = 30
  val sliderWidth: Int = 300
  val stateStringBoxStyle: String =
    "-fx-border-color: #808080; -fx-border-width: 1px; -fx-border-radius: 3.0; " +
    "-fx-border-insets: 2.0 2.0 2.0 2.0;"
  val stateStringTextStyle: String = "-fx-font-size: 11pt;"
  //Resources
  val resetEImg = new Image("mathact/tools/time/reset_e.png", btnSize, btnSize, true, true)
  val resetDImg = new Image("mathact/tools/time/reset_d.png", btnSize, btnSize, true, true)
  //Properties
  @volatile private var _startTime = defaultStartTime
  @volatile private var _period = defaultPeriod
  @volatile private var _initMessage: Option[V] = None
  @volatile private var _initTimeout = defaultInitTimeout
  @volatile private var _minTimeout = defaultMinTimeout
  @volatile private var _maxTimeout = defaultMaxTimeout
  //Definitions
  case class LoopTimeout(timeout: Long) extends UIEvent
  case class Reset(startTime: Long, period: Long, timeout: Long) extends UIEvent
  case class UpdateTime(time: Long) extends UICommand
  //UI definition
  private class LoopUI extends SfxFrame{
    //Params
    title = "Time loop" + (name match{case Some(n) ⇒ " - " + n case _ ⇒ ""})
    showOnStart = true
    resizable = false
    //Bounds
    val minTimeout =
      if(_minTimeout < 0) 0 else _minTimeout
    val maxTimeout =
      if(_maxTimeout < minTimeout) minTimeout else _maxTimeout
    val initTimeout =
      if(_initTimeout < minTimeout) minTimeout else if(_initTimeout > maxTimeout) maxTimeout else _initTimeout
    val startTime =
      if(_startTime < 0) 0 else _startTime
    val period =
      if(_period < 1) 1 else _period
    //Components
    val startStopStepBtn: ExecButtons = new ExecButtons(
      btnSize,
      { startStopStepBtn.stepBtn.passive()
        sendEvent(E.Start)},
      { startStopStepBtn.stepBtn.active()
        sendEvent(E.Stop)},
      sendEvent(E.Step))
    val speedSlider = new Slider{
      //Variables
      private var oldPos = initTimeout
      //Config
      min = minTimeout
      max = maxTimeout
      value = initTimeout
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = (maxTimeout - minTimeout) / 10
      minorTickCount = 4
      blockIncrement = sliderStep
      prefHeight = btnSize
      prefWidth = sliderWidth
      disable = true
      //On action
      value.onChange{
        val rVal = (value.value / sliderStep).toLong * sliderStep
        if(rVal != oldPos) {
          oldPos = rVal
          sendEvent(LoopTimeout(rVal))}}
      //Methods
      def lastPos: Long = oldPos}
    val stateString = new Label{
      style = stateStringTextStyle}
    val resetBtn: IconButton  = new IconButton(btnSize, resetEImg, resetDImg)({
      resetBtn.active()
      startStopStepBtn.active()
      speedSlider.disable = false
      sendEvent(Reset(startTime, period, speedSlider.lastPos))})
    //Scene
    scene = new Scene{
      fill = Color.White
      root = new BorderPane{
        top = new HBox(2){
          children = Seq(
            startStopStepBtn,
            new HBox(2){
              alignment = Pos.Center
              padding = Insets(6.0, 10.0, 4.0, 4.0)
              children = speedSlider},
            new HBox(2){
              alignment = Pos.Center
              padding = Insets(4.0)
              children = resetBtn})}
        bottom = new HBox {
          style = stateStringBoxStyle
          prefHeight
          padding = Insets(1.0)
          children = stateString}}}
    //Commands reactions
    def onCommand = {
      case C.Start ⇒
        sendEvent(Reset(startTime, period, initTimeout)) //Sends in milli seconds
        startStopStepBtn.active()
        speedSlider.disable = false
        resetBtn.active()
      case C.Stop ⇒
        startStopStepBtn.passive()
        speedSlider.disable = true
        resetBtn.passive()
      case C.Ended ⇒
        startStopStepBtn.passive()
        speedSlider.disable = true
      case UpdateTime(time) ⇒
        stateString.text = "Current time = " + time}}
  //UI registration
  UI(new LoopUI)
  //On start and on stop
  protected def onStart(): Unit = { UI.sendCommand(C.Start) }
  protected def onStop(): Unit = { UI.sendCommand(C.Stop) }
  //Handler
  private val handler = new Inflow[V] with Outflow[V]{
    //Variables
    @volatile private var stepTimeout: Long = 0L
    @volatile private var autoLoop: Boolean = false
    @volatile private var lastReceived: Boolean = false
    @volatile private var currentTime: Long = 0L
    @volatile private var endTime: Long = 0L
    @volatile private var lastMessage: Option[V] = None
    //Functions
    private def sendNext(): Unit = (lastMessage, _initMessage) match{
      case (lm, im) if lm.nonEmpty || im.nonEmpty ⇒ currentTime match{
        case ct if ct <= endTime ⇒
          lastReceived = false
          pour(lm.getOrElse(im.get).time(currentTime))
        case _ ⇒
          UI.sendCommand(C.Ended)}
      case (None, None) ⇒
        logger.warn(s"No message to start form. Set initMessage parameter or send one to inlet before starting.")
        UI.sendCommand(C.Ended)}
    private def receivedLast(value: V): Unit = {
      //Next time step
      lastMessage = Some(value)
      currentTime += 1
      lastReceived = true
      //Up UI
      UI.sendCommand(UpdateTime(currentTime))
      //Pass message next with time out
      (autoLoop, stepTimeout) match {
        case (true, 0) ⇒
          sendNext()
        case (true, t) ⇒ Future{
          Thread.sleep(t)
          if(autoLoop) sendNext()}
        case _ ⇒}}
    //Methods
    def reset(startTime: Long, period: Long, timeout: Long): Unit = {
      //Set Params
      currentTime = startTime
      endTime = startTime + period
      stepTimeout = timeout
      autoLoop = false
      //Update UI
      UI.sendCommand(UpdateTime(currentTime))}
    def startLoop(): Unit = if(! autoLoop){
      autoLoop = true
      sendNext()}
    def stopLoop(): Unit = {
      autoLoop = false}
    def step(): Unit = if(! autoLoop) sendNext()
    def setTimeout(timeout: Long): Unit = {
      stepTimeout = timeout}
    //Processing
    protected def drain(value: V): Unit = (value.time, lastReceived) match{
      case (time, false) if time == currentTime ⇒
        receivedLast(value)
      case (t, false) if t > currentTime ⇒ logger.warn(
        s"Message $value from future. Current time $currentTime < message time $t. Look like message time was edited.")
      case (t, false) if t < currentTime ⇒ logger.warn(
        s"Message $value from past. Current time $currentTime > message time $t. Look like cloned message.")
      case (_, true) ⇒ logger.warn(
        s"Double message $value. Message with time ${value.time} was already received.")}}
  //UI event handling
  UI.onEvent{
    case Reset(startTime, period, timeout) ⇒ handler.reset(startTime, period, timeout)
    case LoopTimeout(timeout) ⇒ handler.setTimeout(timeout)
    case E.Start ⇒ handler.startLoop()
    case E.Stop ⇒ handler.stopLoop()
    case E.Step ⇒ handler.step()}
  //DSL
  def startTime: Long = _startTime
  def startTime_=(v: Long){ _startTime = v }
  def period: Long = _period
  def period_=(v: Long){ _period = v }
  def initMessage: Option[V] = _initMessage
  def initMessage_=(v: V){ _initMessage = Some(v)}
  def initTimeout: Long = _initTimeout
  def initTimeout_=(v: Long){ _initTimeout = v }
  def minTimeout: Long = _minTimeout
  def minTimeout_=(v: Long){ _minTimeout = v }
  def maxTimeout: Long = _maxTimeout
  def maxTimeout_=(v: Long){ _maxTimeout = v }
  //Connection points
  val in = Inlet[V](handler)
  val out = Outlet[V](handler)}
