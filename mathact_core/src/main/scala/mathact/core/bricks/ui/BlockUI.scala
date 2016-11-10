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
  @volatile private var frameCreator: Option[()⇒SfxFrame] = None
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
    if (frameCreator.isEmpty) throw new IllegalStateException(
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
          s"[FxmlFrame.<init>] Cannot load FXML by '$uiFxmlPath' path.")}}
  //UI creation
  protected object UI{
    //Constructors
    def apply[T <: SfxFrame : ClassTag]: Unit = {
      val clazz = implicitly[ClassTag[T]].runtimeClass
      frameCreator = Some(() ⇒ { clazz.newInstance().asInstanceOf[SfxFrame]})}
    def apply(creator: ⇒SfxFrame): Unit = {
      frameCreator = Some(() ⇒ creator)}
    //Control flow
    def sendCommand(command: UICommand): Unit = currentFrame.foreach{ f ⇒ runAndWait(f.onCommand.apply(command))}
    def onEvent(proc: PartialFunction[UIEvent,Unit]): Unit = {eventProcs +:= proc}}
  //Internal API
  private[core] def uiInit(): Unit = frameCreator match {
    case Some(creator) ⇒
      //Creating frame
      val frame = runNow(creator())
      currentFrame = Some(frame)
      //Registering
      pump.registerWindow(
        id = 1,
        WindowState(isShown = false, 0, 0, 0, 0, runNow(frame.title.value)),
        WindowPreference(_prefX, _prefY))
    case None ⇒
      throw new IllegalStateException(
        s"[BlockUI.uiInit] UI frame not registered, use 'UI(new MyUI)' or 'UI[MyUI]' to register.")}
  private[core] def uiCreate(): Unit = if(_showOnStart) currentFrame.foreach{ f ⇒ runAndWait{
    f.show()
    f.sizeToScene()}}
  private[core] def uiShow(): Unit = currentFrame.foreach(f ⇒ runAndWait(f.show()))
  private[core] def uiHide(): Unit = currentFrame.foreach(f ⇒ runAndWait(f.hide()))
  private[core] def uiClose(): Unit = currentFrame.foreach(f ⇒ runAndWait(f.close()))
  private[core] def uiLayout(windowId: Int, x: Double, y: Double): Unit = currentFrame.foreach(f ⇒ runAndWait{
    f.x = x
    f.y = y
    f.toFront()})
  private[core] def uiEvent(event: UIEvent): Unit = eventProcs.foreach(_.apply(event))}
