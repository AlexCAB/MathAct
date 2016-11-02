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
  @volatile private var _showOnStart = false
  @volatile private var currentFrame: Option[BlockFrameLike] = None
  @volatile private var eventProcs = List[PartialFunction[UIEvent,Unit]]()
  //Definitions
  /** Block frame interface */
  private[core] trait BlockFrameLike {
    //Control flow
    def onCommand: PartialFunction[UICommand, Unit]
    def sendEvent(event: UIEvent): Unit
    //Internal API
    private[core] def showFrame(): Unit
    private[core] def hideFrame(): Unit
    private[core] def closeFrame(): Unit}
  /** Base class for ScalaFS frame */
  protected trait SfxFrame extends Stage with BlockFrameLike {
    //Check if registered
    if (! isUIRegistered) throw new IllegalStateException(
      "[BlockUI.SfxFrame.<init>] SfxFrame instance should be created by using of UI object.")
    //DSL
    def showOnStart: Boolean = _showOnStart
    def showOnStart_=(v: Boolean) { _showOnStart = v }
    //Control flow
    def sendEvent(event: UIEvent): Unit = pump.sendUiEvent(event)
    //Internal API
    private[core] def showFrame(): Unit = show()
    private[core] def hideFrame(): Unit = hide()
    private[core] def closeFrame(): Unit = close()}
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
    def apply[T <: BlockFrameLike : ClassTag]: Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val clazz = implicitly[ClassTag[T]].runtimeClass
      val frame = runNow(clazz.newInstance().asInstanceOf[BlockFrameLike])
      currentFrame = Some(frame)}
    def apply(frameCreator: ⇒BlockFrameLike): Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val frame = runNow(frameCreator)
      currentFrame = Some(frame)}
    //Control flow
    def sendCommand(command: UICommand): Unit = currentFrame.foreach{ f ⇒ runAndWait(f.onCommand.apply(command))}
    def onEvent(proc: PartialFunction[UIEvent,Unit]): Unit = {eventProcs +:= proc}}
  //Internal API
  private[core] def createFrame(): Unit = if(_showOnStart) currentFrame.foreach(f ⇒ runLater(f.showFrame()))
  private[core] def showFrame(): Unit = currentFrame.foreach(f ⇒ runLater(f.showFrame()))
  private[core] def hideFrame(): Unit = currentFrame.foreach(f ⇒ runLater(f.hideFrame()))
  private[core] def closeFrame(): Unit = currentFrame.foreach(f ⇒ runLater(f.closeFrame()))
  private[core] def uiEvent(event: UIEvent): Unit = eventProcs.foreach(_.apply(event))}
