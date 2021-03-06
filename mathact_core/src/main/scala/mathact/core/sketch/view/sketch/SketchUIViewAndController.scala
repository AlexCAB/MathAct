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

package mathact.core.sketch.view.sketch

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.ActorRef
import akka.event.LoggingAdapter
import mathact.core.model.config.SketchUIConfigLike
import mathact.core.model.enums.SketchUIElement._
import mathact.core.model.enums.SketchUiElemState._
import mathact.core.model.enums._
import mathact.core.model.messages.M

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color._
import scalafx.scene.{Scene, Node}
import scalafx.scene.control.Button
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.text.Text
import scalafx.stage.Stage


/** Sketch UI controller
  * Created by CAB on 26.09.2016.
  */

private[core] class SketchUIViewAndController(
  config: SketchUIConfigLike,
  sketchController: ActorRef,
  log: LoggingAdapter)
extends Stage {
  //Params
  val windowTitle = "MathAct - Workbench"
  val buttonsImageSize = 30
  val logBtnDPath             = "mathact/sketchIU/log_btn_d.png"
  val logBtnSPath             = "mathact/sketchIU/log_btn_s.png"
  val logBtnHPath             = "mathact/sketchIU/log_btn_h.png"
  val visualisationBtnDPath   = "mathact/sketchIU/visualisation_btn_d.png"
  val visualisationBtnSPath   = "mathact/sketchIU/visualisation_btn_s.png"
  val visualisationBtnHPath   = "mathact/sketchIU/visualisation_btn_h.png"
  val runBtnDPath             = "mathact/sketchIU/run_btn_d.png"
  val runBtnEPath             = "mathact/sketchIU/run_btn_e.png"
  val showAllBlocksUiDPath    = "mathact/sketchIU/show_all_blocks_ui_d.png"
  val showAllBlocksUiEPath    = "mathact/sketchIU/show_all_blocks_ui_e.png"
  val hideAllBlocksUiBtnDPath = "mathact/sketchIU/hide_all_blocks_ui_btn_d.png"
  val hideAllBlocksUiBtnEPath = "mathact/sketchIU/hide_all_blocks_ui_btn_e.png"
  val skipAllTimeoutTaskDPath = "mathact/sketchIU/skip_all_timeout_task_d.png"
  val skipAllTimeoutTaskEPath = "mathact/sketchIU/skip_all_timeout_task_e.png"
  val stopSketchBtnDPath      = "mathact/sketchIU/stop_sketch_btn_d.png"
  val stopSketchBtnEPath      = "mathact/sketchIU/stop_sketch_btn_e.png"
  val layoutFillEPath         = "mathact/sketchIU/layout_fill_e.png"
  val layoutFillDPath         = "mathact/sketchIU/layout_fill_d.png"
  val layoutStairsEPath       = "mathact/sketchIU/layout_stairs_e.png"
  val layoutStairsDPath       = "mathact/sketchIU/layout_stairs_d.png"
  //Definitions
  class MWButton[I](elem: SketchUIElement, states: List[(I, String)], action: (SketchUIElement,I)⇒Unit) extends Button{
    //Variables
    private var currentStates = states.head._1  //First state should be is always 'disable'
    //Build images
    val images = states
        .map{case (id, imgPath) ⇒
          (id, new ImageView{image = new Image(imgPath, buttonsImageSize, buttonsImageSize, true, true)})}
        .toMap
    //Config
    graphic = images(states.head._1)
    disable = true
    prefHeight = buttonsImageSize
    prefWidth = buttonsImageSize
    onAction = handle{
      //Disable button
      graphic = images(states.head._1)
      disable = true
      //Run action
      action(elem,currentStates)}
    //Methods
    def setState(newState: I): Unit = {
      graphic = images(newState)
      currentStates = newState
      disable = newState == states.head._1}}
  private class ButtonBox(spacing: Double, buttons: Seq[Node]) extends HBox(spacing){
    alignment = Pos.Center
    prefHeight = buttonsImageSize
    prefWidth = buttonsImageSize * 3
    padding = Insets(4.0, 4.0, 4.0, 4.0)
    children = buttons}
  //Functions
  private def actionTriggered(elem: SketchUIElement, state: SketchUiElemState): Unit = {
    log.debug(s"[SketchUI.actionTriggered] Hit elem $elem in state: $state")
    sketchController ! M.SketchUIActionTriggered(elem, state)}
  //Close operation
  delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
    def handle(event: WindowEvent): Unit = {
      log.debug("[SketchUI.onCloseRequest] Close is hit, send SketchUIActionTriggered(CloseBtn, Unit).")
      sketchController ! M.SketchUIActionTriggered(CloseBtn, Unit)
      event.consume()}})
  //UI Components
  val logBtn = new MWButton[SketchUiElemState](
    LogBtn,
    List(ElemDisabled → logBtnDPath, ElemShow → logBtnSPath, ElemHide → logBtnHPath),
    actionTriggered)
  val visualisationBtn = new MWButton[SketchUiElemState](
    VisualisationBtn,
    List(ElemDisabled → visualisationBtnDPath, ElemShow → visualisationBtnSPath, ElemHide → visualisationBtnHPath),
    actionTriggered)
  val runBtn = new MWButton[SketchUiElemState](
    RunBtn,
    List(ElemDisabled → runBtnDPath, ElemEnabled → runBtnEPath),
    actionTriggered)
  val layoutFillBtn = new MWButton[SketchUiElemState](
    LayoutFillBtn,
    List(ElemDisabled → layoutFillDPath, ElemEnabled → layoutFillEPath),
    actionTriggered)
  val layoutStairsBtn = new MWButton[SketchUiElemState](
    LayoutStairsBtn,
    List(ElemDisabled → layoutStairsDPath, ElemEnabled → layoutStairsEPath),
    actionTriggered)
  val showAllBlocksUiBtn = new MWButton[SketchUiElemState](
    ShowAllBlocksUiBtn,
    List(ElemDisabled → showAllBlocksUiDPath, ElemEnabled → showAllBlocksUiEPath),
    actionTriggered)
  val hideAllBlocksUiBtn = new MWButton[SketchUiElemState](
    HideAllBlocksUiBtn,
    List(ElemDisabled → hideAllBlocksUiBtnDPath, ElemEnabled → hideAllBlocksUiBtnEPath),
    actionTriggered)
  val skipAllTimeoutTaskBtn = new MWButton[SketchUiElemState](
    SkipAllTimeoutTaskBtn,
    List(ElemDisabled → skipAllTimeoutTaskDPath, ElemEnabled → skipAllTimeoutTaskEPath),
    actionTriggered)
  val stopSketchBtn = new MWButton[SketchUiElemState](
    StopSketchBtn,
    List(ElemDisabled → stopSketchBtnDPath, ElemEnabled → stopSketchBtnEPath),
    actionTriggered)
  val stateString = new Text {
    text = "???"
    style = "-fx-font-size: 11pt;"}
  //UI
  title = windowTitle
  scene = new Scene {
    fill = White
    content = new BorderPane{
      top = new HBox {
        alignment = Pos.Center
        children = Seq(
          new ButtonBox(spacing = 2, Seq(logBtn,visualisationBtn)),
          new ButtonBox(spacing = 0, Seq(runBtn)),
          new ButtonBox(spacing = 2, Seq(layoutFillBtn,layoutStairsBtn)),
          new ButtonBox(spacing = 2, Seq(showAllBlocksUiBtn,hideAllBlocksUiBtn)),
          new ButtonBox(spacing = 2, Seq(skipAllTimeoutTaskBtn,stopSketchBtn)))}
      bottom = new HBox {
        style = "-fx-border-color: #808080; -fx-border-width: 1px; -fx-border-radius: 3.0; " +
          "-fx-border-insets: 2.0 2.0 2.0 2.0;"
        prefHeight
        padding = Insets(1.0)
        children = stateString}}}}
