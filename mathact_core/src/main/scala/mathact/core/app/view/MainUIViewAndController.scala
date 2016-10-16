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

package mathact.core.app.view

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.ActorRef
import akka.event.LoggingAdapter
import mathact.core.model.config.MainUIConfigLike
import mathact.core.model.data.sketch.SketchInfo
import mathact.core.model.enums._
import mathact.core.model.messages.M

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.Stage


/** Main UI view and controller
  * Created by CAB on 11.10.2016.
  */

private [mathact] class MainUIViewAndController(
  config: MainUIConfigLike,
  uiController: ActorRef,
  log: LoggingAdapter)
extends Stage {
  //Parameters
  val windowTitle = "MathAct - Sketches"
  val windowMinHeight = 150
  val windowMinWidth = 600
  val buttonsSize = 20
  val startBtnDImgPath = "mathact/sketchList/sketch_start_d.png"
  val startBtnEImgPath = "mathact/sketchList/sketch_start_e.png"
  val noSketchesMessage =
    """
    | No sketches found.
    | Please define some sketches like:
    |   object MySketches extends Application{
    |     sketchOf[MySketchClass] name "Example" description "My first SketchData" autorun
    |   }
    """.stripMargin
  //Resources
  private val startBtnDImg = new Image(startBtnDImgPath, buttonsSize, buttonsSize, true, true)
  private val startBtnEImg =  new Image(startBtnEImgPath, buttonsSize, buttonsSize, true, true)
  //Definitions
  private class SketchData(sketch: SketchInfo){
    val className: String = sketch.className
    val name = sketch.sketchName.getOrElse(className)
    val description = sketch.sketchDescription.getOrElse("---")
    val status = sketch.lastRunStatus match{
      case SketchStatus.Ready ⇒ "ready"
      case SketchStatus.Ended ⇒ "ended"
      case SketchStatus.Failed ⇒ "failed"
      case _ ⇒ "unknown"}
    val runBtn = new Button{
      //Parameters
      graphic = new ImageView{image = startBtnEImg}
      prefHeight = buttonsSize
      prefWidth = buttonsSize
      onAction = handle{ uiController ! M.RunSketch(sketch) }
      //Methods
      def setEnabled(isEnabled: Boolean): Unit = isEnabled match{
        case true ⇒
          graphic = new ImageView{image = startBtnEImg}
          disable = false
        case false ⇒
          graphic = new ImageView{image = startBtnDImg}
          disable = true}}}
  //Variables
  private var sketchList = List[SketchData]()
  //Close operation
  delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
    def handle(event: WindowEvent): Unit = {
      log.debug("[SketchUI.onCloseRequest] Close is hit, send SketchUIActionTriggered(CloseBtn, Unit).")
      uiController ! M.MainCloseBtnHit
      event.consume()}})
  //UI Components
  private val noSketchesLabel = new Label{
    text = noSketchesMessage
    style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"}
  private val sketchesTable = new TableView[SketchData](){
    columnResizePolicy = TableView.UnconstrainedResizePolicy
    val nameColumn = new TableColumn[SketchData, String] {
      text = "Name"
      prefWidth = 180
      style = "-fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"
      cellValueFactory = { d ⇒ new StringProperty(d.value, "name",  d.value.name)}}
    val descriptionColumn = new TableColumn[SketchData, String] {
      text = "Description"
      prefWidth = 300
      style = "-fx-font-size: 12; -fx-alignment: CENTER-LEFT;"
      cellValueFactory = { d ⇒ new StringProperty(d.value, "description",  d.value.description)}}
    val statusColumn = new TableColumn[SketchData, String] {
      text = "Status"
      prefWidth = 60
      style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
      cellValueFactory = { d ⇒ new StringProperty(d.value, "status",  d.value.status)}}
    val runBtnColumn = new TableColumn[SketchData, Button] {
      text = "Run"
      prefWidth = 42
      style = "-fx-alignment: CENTER;"
      cellValueFactory = { d ⇒ new ObjectProperty(d.value, "runBtn", d.value.runBtn)}
      cellFactory = { d ⇒ new TableCell[SketchData, Button] {
        contentDisplay = ContentDisplay.GraphicOnly
        item.onChange{ (_,_,b) ⇒ graphic = b}}}}
    columns ++= Seq(nameColumn, descriptionColumn, statusColumn, runBtnColumn)}
  private val selectionUi = new VBox{
    alignment = Pos.Center
    children = Seq(
      new Label{
        text = "Hello, select one of next sketches to run:"
        style = "-fx-font-size: 16; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"},
      sketchesTable)}
  private val contentHolder = new StackPane
  //UI
  title = windowTitle
  scene = new Scene {
    fill = Color.White
    minHeight = windowMinHeight
    minWidth = windowMinWidth
    root = contentHolder}
  //Methods
  /** Fill table with sketches data or show "no sketches" message if list is empty
    * @param sketches - List[SketchInfo] */
  def setTableData(sketches: List[SketchInfo]): Unit = sketches match{
    case Nil ⇒
      log.debug("[MainUIViewAndController.setTableData] Sketches list is empty, show 'no sketches' message.")
      contentHolder.children = noSketchesLabel
    case _ ⇒
      log.debug("[MainUIViewAndController.setTableData] Show sketches: " + sketches)
      sketchList = sketches.map( data ⇒ new SketchData(data))
      sketchesTable.items = ObservableBuffer(sketchList)
      contentHolder.children = selectionUi}
  /** Disable run buttons except given one
    * @param info - SketchInfo, sketch to skip */
  def disableRunButtonsExceptGiven(info: SketchInfo): Unit = sketchList.foreach{
    case sketch if sketch.className != info.className ⇒ sketch.runBtn.setEnabled(false)
    case _ ⇒}}
