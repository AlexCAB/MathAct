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

package mathact.core.sketch.view.logging

import javafx.event.EventHandler
import javafx.scene.input.{KeyCodeCombination, KeyEvent}

import akka.actor.ActorRef

import scalafx.beans.property.{StringProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{Clipboard, ClipboardContent, KeyCombination, KeyCode}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import UserLogging._


/** User log UI presenter
  * Created by CAB on 24.09.2016.
  */

@sfxml
class UserLogUIController(
  private val autoScrollBtn: Button,
  private val searchText: TextField,
  private val logLevelChoice: ChoiceBox[String],
  private val logAmountChoice: ChoiceBox[String],
  private val cleanBtn: Button,
  private val tableView: TableView[LogRow])
extends UserLogUIControllerLike{
   //Params
  val buttonsImageSize = 30
  val logImageSize = 20
  val toolNameColumnPrefWidth = 150
  val autoScrollBtnOnPath = "mathact/userLog/auto_scroll_btn_on.png"
  val autoScrollBtnOffPath = "mathact/userLog/auto_scroll_btn_off.png"
  val cleanBtnDPath = "mathact/userLog/clean_btn_d.png"
  val cleanBtnEPath = "mathact/userLog/clean_btn_e.png"
  val infoImgPath = "mathact/userLog/info_img.png"
  val warnImgPath = "mathact/userLog/warn_img.png"
  val errorImgPath = "mathact/userLog/error_img.png"
  val autoScrollDefault = true
  val logLevelChoiceDefault = "Show all"
  val logAmountChoiceDefault = "Last 100"
  //Variables
  private var actor: Option[ActorRef] = None
  private var autoScroll = autoScrollDefault
  //Load resources
  val autoScrollBtnOnImg =
    new ImageView{image = new Image(autoScrollBtnOnPath, buttonsImageSize, buttonsImageSize, true, true)}
  val autoScrollBtnOffImg =
    new ImageView{image = new Image(autoScrollBtnOffPath, buttonsImageSize, buttonsImageSize, true, true)}
  val cleanBtnDImg =
    new ImageView{image = new Image(cleanBtnDPath, buttonsImageSize, buttonsImageSize, true, true)}
  val cleanBtnEImg =
    new ImageView{image = new Image(cleanBtnEPath, buttonsImageSize, buttonsImageSize, true, true)}
  val logImages = Map(LogType.Info → infoImgPath, LogType.Warn → warnImgPath, LogType.Error → errorImgPath)
    .map{case (id, path) ⇒ (id, new Image(path, logImageSize, logImageSize, true, true))}
  //Listeners
  def autoScrollAction(): Unit = {
    //Update value
    autoScroll = ! autoScroll
    //Set button view
    autoScrollBtn.graphic = if (autoScroll) autoScrollBtnOnImg else autoScrollBtnOffImg}
  def searchTextAction(): Unit = actor.foreach(_ ! DoSearch(searchText.text.value))
  def logLevelChoiceAction(): Unit = actor.foreach{ a ⇒
    logLevelChoice.selectionModel.value.selectedIndexProperty.toInt match{
      case 0 ⇒ a ! SetLogLevel(LogType.Info)
      case 1 ⇒ a ! SetLogLevel(LogType.Warn)
      case 2 ⇒ a ! SetLogLevel(LogType.Error)
      case _ ⇒}}
  def logAmountChoiceAction(): Unit = actor.foreach{ a ⇒
    logAmountChoice.selectionModel.value.selectedIndexProperty.toInt match{
      case 0 ⇒ a ! SetLogAmount(Int.MaxValue)
      case 1 ⇒ a ! SetLogAmount(10000)
      case 2 ⇒ a ! SetLogAmount(1000)
      case 3 ⇒ a ! SetLogAmount(100)
      case 4 ⇒ a ! SetLogAmount(10)
      case 5 ⇒ a ! SetLogAmount(1)
      case _ ⇒}}
  def cleanBtnAction(): Unit = actor.foreach{ a ⇒
    //Disable btn
    cleanBtn.graphic = cleanBtnDImg
    cleanBtn.disable = true
    //Send message
    a ! DoClean}
  //Preparing tools
  autoScrollBtn.graphic = autoScrollBtnOnImg
  autoScrollBtn.disable = false
  cleanBtn.graphic = cleanBtnEImg
  cleanBtn.disable = false
  searchText.textProperty.onChange(searchTextAction())
  logLevelChoice.delegate.getSelectionModel.selectedItemProperty.onChange(logLevelChoiceAction())
  logAmountChoice.delegate.getSelectionModel.selectedItemProperty.onChange(logAmountChoiceAction())
  //Preparing table
  val msgTypeColumn = new TableColumn[LogRow, ImageView] {
    text = "T"
    prefWidth = logImageSize + 4
    style = "-fx-alignment: CENTER;"
    cellValueFactory = { d ⇒ new ObjectProperty(d.value, "type",  new ImageView{image = logImages(d.value.msgType)})}
    cellFactory = { _ ⇒
      new TableCell[LogRow, ImageView] {
        item.onChange { (_, _, img) ⇒ graphic = img}}}}
  val toolNameColumn = new TableColumn[LogRow, String] {
    text = "Tool Name"
    prefWidth = toolNameColumnPrefWidth
    style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
    cellValueFactory = { d ⇒ new StringProperty(d.value, "toolName",  d.value.toolName)}}
  val messageColumn = new TableColumn[LogRow, String] {
    text = "Message"
    style = "-fx-font-size: 12;"
    cellValueFactory = { d ⇒ new StringProperty(d.value, "message",  d.value.message)}}
  messageColumn.prefWidthProperty.bind(tableView.width - (msgTypeColumn.width + toolNameColumn.width) - 20)
  tableView.columns ++= Seq(msgTypeColumn, toolNameColumn, messageColumn)
  tableView.onKeyPressed = new EventHandler[KeyEvent]{
    val copyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.ControlAny)
    def handle(e: KeyEvent): Unit = if(copyCombination.`match`(e)){
      val item = tableView.selectionModel.value.getSelectedItem
      val clipboard = new ClipboardContent
      val text = item.toolName + "\t|\t" + item.message
      clipboard.putString(text)
      Clipboard.systemClipboard.setContent(clipboard)}}
  //Methods
  def setActor(actor: ActorRef): Unit = {
    //Set actor
    this.actor = Some(actor)
    //Set default values, which trigger send of SetLogLevel and SetLogAmount
    logLevelChoice.delegate.getSelectionModel.select(logLevelChoiceDefault)
    logAmountChoice.delegate.getSelectionModel.select(logAmountChoiceDefault)}
  def setRows(rows: List[LogRow]): Unit = {
    //Set rows
    tableView.items = ObservableBuffer(rows)
    //Scroll
    if(autoScroll) tableView.scrollTo(rows.size)
    //Update clean btn
    rows.size match{
      case 0 ⇒
        cleanBtn.graphic = cleanBtnDImg
        cleanBtn.disable = true
      case _ ⇒
        cleanBtn.graphic = cleanBtnEImg
        cleanBtn.disable = false}}}
