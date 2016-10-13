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

package mathact.core.control.view.logging

import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.stage.WindowEvent

import akka.actor.{ActorRef, PoisonPill}
import mathact.core.ActorBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.UserLoggingConfigLike
import mathact.core.model.messages.M

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafxml.core.{NoDependencyResolver, FXMLLoader}


/** Logging to user UI console
  * Created by CAB on 26.08.2016.
  */

private [mathact] class UserLoggingActor(config: UserLoggingConfigLike, workbenchController: ActorRef)
extends ActorBase with JFXInteraction { import UserLogging._
  //Parameters
  val windowTitle = "MathAct - Logger"
  val uiFxmlPath = "mathact/userLog/ui.fxml"
  //Variables
  private var isShown = false
  private var searchText = ""
  private var logLevel = LogType.Info
  private var logAmount = 10
  private var logRows = List[LogRow]()
  //Construction
  private val (window, controller) = runNow{
    //Try to load resource
    Option(getClass.getClassLoader.getResource(uiFxmlPath)) match{
      case Some(conf) ⇒
        //Load FXML
        val loader = new FXMLLoader(
          getClass.getClassLoader.getResource(uiFxmlPath),
          NoDependencyResolver)
        loader.load()
        //Get view and controller
        val view = loader.getRoot[Parent]
        val controller = loader.getController[UserLogUIControllerLike]
        //Create stage
        val stg = new Stage {
          title = windowTitle
          scene = new Scene(view)}
        //Close operation
        stg.delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
          def handle(event: WindowEvent): Unit = {
            log.debug("[UserLogging.onCloseRequest] Close is hit.")
            //Set states
            isShown = false
            stg.hide()
            //Send message
            workbenchController ! M.UserLoggingUIChanged(isShown)}})
        //Set params and return
        stg.resizable = true
        stg.sizeToScene()
        controller.setActor(self)
        (stg, controller)
      case None ⇒
        throw new IllegalArgumentException(
          s"[UserLoggingActor.<init>] Cannot load FXML by '$uiFxmlPath path.'")}}
  //Functions
  private def refreshUi(): Unit = isShown match{
    case true ⇒
      //Filter log
      val clippedLog = logAmount match{
        case Int.MaxValue ⇒ logRows
        case amount ⇒ logRows.take(amount)}
      val filteredLog = logLevel match{
        case LogType.Info ⇒ clippedLog
        case LogType.Warn ⇒ clippedLog.filter(r ⇒ r.msgType == LogType.Warn || r.msgType == LogType.Error)
        case LogType.Error ⇒ clippedLog.filter(r ⇒ r.msgType == LogType.Error)}
      val searchedLog = searchText match{
        case "" ⇒ filteredLog
        case ss ⇒ filteredLog.filter(r ⇒ r.toolName.contains(ss) || r.message.contains(ss))}
      val resultLog = searchedLog.reverse
      //Show rows
      runAndWait(controller.setRows(resultLog))
    case false ⇒
      //UI hide
      log.debug("[UserLogging.refreshUi] UI not show, nothing to refresh.")}
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Do search
    case DoSearch(text) ⇒
      searchText = text
      refreshUi()
    //Set log level
    case SetLogLevel(level) ⇒
      logLevel = level
      refreshUi()
    //Set log amount
    case SetLogAmount(amount) ⇒
      logAmount = amount
      refreshUi()
    //Do clean
    case DoClean ⇒
      logRows = List()
      refreshUi()
    //Show UI
    case M.ShowUserLoggingUI ⇒
      isShown = true
      refreshUi()
      runAndWait(window.show())
      workbenchController ! M.UserLoggingUIChanged(isShown)
    //Hide UI
    case M.HideUserLoggingUI ⇒
      isShown = false
      refreshUi()
      runAndWait(window.hide())
      workbenchController ! M.UserLoggingUIChanged(isShown)
    //Log info
    case M.LogInfo(toolId, toolName, message) ⇒
      //Build row
      logRows +:= LogRow(LogType.Info, toolName, message)
      refreshUi()
    //Log warning
    case M.LogWarning(toolId, toolName, message) ⇒
      //Build row
      logRows +:= LogRow(LogType.Warn, toolName, message)
      refreshUi()
    //Log error
    case M.LogError(toolId, toolName, error, message) ⇒
      //Build row
      val row = LogRow(LogType.Error, toolName, message + (error match{
        case Some(e) ⇒
          "\n" +
          "Exception message: " + e.getMessage + "\n" +
          "Stack trace: \n      " + e.getStackTrace.mkString("\n      ")
        case None ⇒ ""}))
      //Add to Log
      logRows +:= row
      refreshUi()
    //Terminate user logging
    case M.TerminateUserLogging ⇒
      runAndWait(window.close())
      workbenchController ! M.UserLoggingTerminated
      self ! PoisonPill}}
