///* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
// * @                                                                             @ *
// *           #          # #                                 #    (c) 2016 CAB      *
// *          # #      # #                                  #  #                     *
// *         #  #    #  #           # #     # #           #     #              # #   *
// *        #   #  #   #             #       #          #        #              #    *
// *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
// *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
// *     #          #   # # # #   #       #      #  #           #  #         #       *
// *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
// *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
// * @                                                                             @ *
//\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */
//
//package mathact.core.gui
//
//import javafx.event.EventHandler
//import javafx.stage.WindowEvent
//
//import akka.event.LoggingAdapter
//import mathact.core.model.data.sketch.SketchData
//import mathact.core.model.enums.SketchStatus
//
//import scalafx.Includes._
//import scalafx.beans.property.{ObjectProperty, StringProperty}
//import scalafx.collections.ObservableBuffer
//import scalafx.geometry.Pos
//import scalafx.scene.Scene
//import scalafx.scene.control._
//import scalafx.scene.image.{Image, ImageView}
//import scalafx.scene.layout.VBox
//import scalafx.scene.paint.Color._
//import scalafx.stage.Stage
//
//
///** Select sketch window, show list of sketches
//  * Created by CAB on 19.06.2016.
//  */
//
////TODO Make window resizable (not work on Windows 7)
//abstract class SelectSketchWindow(log: LoggingAdapter) extends JFXInteraction {
//  //Parameters
//  val buttonsSize = 20
//  //Callbacks
//  def sketchSelected(sketchClassName: String): Unit
//  def windowClosed(): Unit
//  //Definitions
//  private class MainWindowStage(sketches: List[SketchData]) extends Stage {
//    //Components
//    val startBtnDImg = new Image("mathact/sketchList/sketch_start_d.png", buttonsSize, buttonsSize, true, true)
//    val startBtnEImg =  new Image("mathact/sketchList/sketch_start_e.png", buttonsSize, buttonsSize, true, true)
//    //Definitions
//    class SketchData(sketch: SketchData, onHit: String⇒Unit){
//      val className: String = ??? // = sketch.className
//      val name = ??? //sketch.sketchName.getOrElse(className)
//      val description = ??? //sketch.sketchDescription.getOrElse("---")
//      val status: SketchStatus = ??? //sketch.status match{
////        case SketchStatus.Autorun ⇒ "autorun"
////        case SketchStatus.Ready ⇒ "ready"
////        case SketchStatus.Ended ⇒ "ended"
////        case SketchStatus.Failed ⇒ "failed"
////        case _ ⇒ "unknown"}
//      val runBtn = new Button{
//        //Parameters
//        graphic = new ImageView{image = startBtnEImg}
//        prefHeight = buttonsSize
//        prefWidth = buttonsSize
//        onAction = handle{onHit(className)}
//        //Methods
//        def setEnabled(isEnabled: Boolean): Unit = isEnabled match{
//          case true ⇒
//            graphic = new ImageView{image = startBtnEImg}
//            disable = false
//          case false ⇒
//            graphic = new ImageView{image = startBtnDImg}
//            disable = true}}}
//    //Preparing
//    val sketchRows: List[SketchData] = ???
//
////      sketches.map(d ⇒
////
////      new SketchData(d, className ⇒ {
////      //Disable rest os sketches
//////      sketchRows.foreach{
//////        case s if s.className != className ⇒ s.runBtn.setEnabled(false)
//////        case _ ⇒}
////      //Call selected
////      sketchSelected(className)}))
//
//    //UI
//    title = "MathAct - Sketches"
//    scene = new Scene {
//      fill = White
//      content = sketches match{
//        case Nil ⇒ new Label{
//          text =
//            """
//              | No sketches found.
//              | Please define some sketch like:
//              |   object MySketch extends Application{
//              |     sketchOf[MySketchClass] name "Example" description "My first SketchData" autorun
//              |   }
//            """.stripMargin
//          style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"
//          prefWidth = 600}
//        case sks ⇒ new VBox{
//          alignment = Pos.Center
//          children = Seq(
//            new Label{
//              text = "Hello, select one of next sketches to run:"
//              style = "-fx-font-size: 16; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"},
//            new TableView[SketchData](ObservableBuffer(sketchRows)){
//              columnResizePolicy = TableView.UnconstrainedResizePolicy
//              val nameColumn = new TableColumn[SketchData, String] {
//                text = "Name"
//                prefWidth = 180
//                style = "-fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"
//                cellValueFactory = { d ⇒ new StringProperty(d.value, "name",  d.value.name)}}
//              val descriptionColumn = new TableColumn[SketchData, String] {
//                text = "Description"
//                prefWidth = 300
//                style = "-fx-font-size: 12; -fx-alignment: CENTER-LEFT;"
//                cellValueFactory = { d ⇒ new StringProperty(d.value, "description",  d.value.description)}}
//              val statusColumn = new TableColumn[SketchData, String] {
//                text = "Status"
//                prefWidth = 60
//                style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
////                cellValueFactory = { d ⇒ new StringProperty(d.value, "status",  d.value.status)}
//              }
//              val runBtnColumn = new TableColumn[SketchData, Button] {
//                text = "Run"
//                prefWidth = 42
//                style = "-fx-alignment: CENTER;"
//                cellValueFactory = { d ⇒ new ObjectProperty(d.value, "runBtn", d.value.runBtn)}
//                cellFactory = { d ⇒ new TableCell[SketchData, Button] {
//                  contentDisplay = ContentDisplay.GraphicOnly
//                  item.onChange{ (_,_,b) ⇒ graphic = b}}}}
//              columns ++= Seq(nameColumn, descriptionColumn, statusColumn, runBtnColumn)})}}}
//    //Close operation
//    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
//      def handle(event: WindowEvent): Unit = {
//        log.debug("[SelectSketchWindow.onCloseRequest] Close is hit, call windowClosed.")
//        windowClosed()
//        event.consume()}})}
//  //Variables
//  private var stage: Option[MainWindowStage] = None
//  //Methods
//  def show(sketches: List[SketchData]): Unit = {
//    //Close old is exist
//    stage.foreach(stg ⇒ runAndWait(stg.close()))
//    //Create new
//    stage = Some(runNow{
//      val stg = new MainWindowStage(sketches)
//      stg.resizable = false
//      stg.sizeToScene()
//      stg.show()
//      stg})}
//  def hide(): Unit = stage.foreach{ stg ⇒
//    runAndWait(stg.close())
//    stage = None}}
