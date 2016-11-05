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

package mathact.core.bricks.ui

import javafx.beans.value.{ObservableValue, ChangeListener}

import mathact.core.gui.JFXInteraction
import mathact.core.gui.ui.BlockUILike
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.sketch.blocks.BlockLike

import scala.reflect.ClassTag
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.Includes._
import scalafxml.core.{NoDependencyResolver, FXMLLoader}
import javafx.scene.Parent


/** Adding UI to block.
  * Created by CAB on 31.08.2016.
  */

trait BlockUI extends BlockUILike with JFXInteraction{ _: BlockLike ⇒
  //Variables
  @volatile private var isUIRegistered = false
  @volatile private var isWindowShown = false
  @volatile private var _showOnStart = false
  @volatile private var _prefX: Option[Double] = None
  @volatile private var _prefY: Option[Double] = None
  @volatile private var currentFrame: Option[SfxFrame] = None
  @volatile private var eventProcs = List[PartialFunction[UIEvent,Unit]]()
  //Definitions
  /** Base class for ScalaFS frame */
  protected trait SfxFrame extends Stage {
    //Check if registered
    if (! isUIRegistered) throw new IllegalStateException(
      "[BlockUI.SfxFrame.<init>] SfxFrame instance should be created by using of UI object.")
    //Functions
    private def stateChanged(): Unit = {
      pump.windowUpdated(id = 1, WindowState(isWindowShown, x.value, y.value, height.value, width.value, title.value))}
    //DSL
    def showOnStart: Boolean = _showOnStart
    def showOnStart_=(v: Boolean) { _showOnStart = v }
    def prefX: Double = _prefX.getOrElse(-1)
    def prefX_=(v: Double) { _prefX = Some(v) }
    def prefY: Double = _prefY.getOrElse(-1)
    def prefY_=(v: Double) { _prefY =  Some(v) }
    //Minimisation and show/hide listeners
    onHidden = handle{
      isWindowShown = false
      stateChanged()}
    onShown = handle{
      isWindowShown = true
      stateChanged()}
    delegate.iconifiedProperty.onChange{ (_, _, isMaximized) ⇒ {
      isWindowShown = ! isMaximized
      stateChanged()}}
    //Resize and position listeners
    delegate.heightProperty.onChange{ (_, _, _) ⇒ stateChanged()}
    delegate.widthProperty.onChange{ (_, _, _) ⇒ stateChanged()}
    delegate.xProperty.onChange{ (_, _, _) ⇒ stateChanged()}
    delegate.yProperty.onChange{ (_, _, _) ⇒ stateChanged()}
    //Control flow
    def sendEvent(event: UIEvent): Unit = pump.sendUiEvent(event)
    def onCommand: PartialFunction[UICommand, Unit]}
  /** Base class for frame with FXML loading */
  protected abstract class FxmlFrame[C](uiFxmlPath: String) extends SfxFrame{
    //Try to load resource
    val (view, controller) = Option(getClass.getClassLoader.getResource(uiFxmlPath)) match{
      case Some(conf) ⇒
        //Load FXML
        val loader = new FXMLLoader(
          conf,
          NoDependencyResolver)
        loader.load()
        //Get view and controller
        val view = loader.getRoot[Parent]
        val controller = loader.getController[C]
        //Set scene
        scene = new Scene(view)
        (view, controller)
      case None ⇒
        throw new IllegalArgumentException(
          s"[FxmlFrame.<init>] Cannot load FXML by '$uiFxmlPath path.'")}}
  //UI creation
  protected object UI{
    //Constructors
    def apply[T <: SfxFrame : ClassTag]: Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val clazz = implicitly[ClassTag[T]].runtimeClass
      val frame = runNow(clazz.newInstance().asInstanceOf[SfxFrame])
      currentFrame = Some(frame)}
    def apply(frameCreator: ⇒SfxFrame): Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val frame = runNow(frameCreator)
      currentFrame = Some(frame)}
    //Control flow
    def sendCommand(command: UICommand): Unit = currentFrame.foreach{ f ⇒ runAndWait(f.onCommand.apply(command))}
    def onEvent(proc: PartialFunction[UIEvent,Unit]): Unit = {eventProcs +:= proc}}
  //Internal API

  //TODO Так как нужно чтобы это функции отабатывали синхронно заменить runLater на runNow,
  //TODO и вынести вызов функций в контекс импелера (для синхроного выполния и ловли ошибок)
  //Так же uiInit должна быть синхронизирована до Constructed

  private[core] def uiInit(): Unit = currentFrame.foreach(f ⇒ runLater{
    f.sizeToScene()
    pump.registerWindow(
      id = 1,
      WindowState(_showOnStart, f.x.value, f.y.value, f.height.value, f.width.value, f.title.value),
      WindowPreference(_prefX, _prefY))})



  private[core] def uiCreate(): Unit = if(_showOnStart) currentFrame.foreach(f ⇒ runLater(f.show()))
  private[core] def uiShow(): Unit = currentFrame.foreach(f ⇒ runLater(f.show()))
  private[core] def uiHide(): Unit = currentFrame.foreach(f ⇒ runLater(f.hide()))
  private[core] def uiClose(): Unit = currentFrame.foreach(f ⇒ runLater(f.close()))
  private[core] def uiLayout(windowId: Int, x: Double, y: Double): Unit = currentFrame.foreach(f ⇒ runLater{
    f.x = x
    f.y = y
    f.toFront()})


  private[core] def uiEvent(event: UIEvent): Unit = eventProcs.foreach(_.apply(event))}
