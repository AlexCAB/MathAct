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
import mathact.core.gui.ui.{BlockFrameLike, BlockUILike}
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

trait BlockUI extends BlockUILike { _: BlockLike ⇒
  //Variables
  @volatile private var isUIRegistered = false
  @volatile private var currentFrame: Option[BlockFrameLike] = None




  //Definitions
  protected trait SfxFrame extends Stage with BlockFrameLike {
    //Check if registered
    if (! isUIRegistered) throw new IllegalStateException(
      "[BlockUI.SfxFrame.<init>] SfxFrame instance should be created by using of UI object.")
    //

    def sendEvent(event: UIEvent): Unit = pump.sendUiEvent(event)


    def showOnStart: Boolean = ???
    def showOnStart_=(v: Boolean) {
      //???
    }


    //Internal API
    private[core] def showFrame(): Unit = show()
    private[core] def hideFrame(): Unit = hide()
    private[core] def closeFrame(): Unit = close()


  }


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
          s"[FxmlFrame.<init>] Cannot load FXML by '$uiFxmlPath path.'")}



  }







  //UI creation
  protected object UI extends JFXInteraction{
    //Variables
    @volatile private var eventProcs = List[PartialFunction[UIEvent,Unit]]()


    //Constructors
    def apply[T <: BlockFrameLike : ClassTag]: Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val clazz = implicitly[ClassTag[T]].runtimeClass
      val frame = runNow(clazz.newInstance().asInstanceOf[BlockFrameLike])

      runAndWait(frame.showFrame())

      currentFrame = Some(frame)

      //???

    }


    def apply(frame: ⇒BlockFrameLike): Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val f = runNow(frame)

      runAndWait(f.showFrame())

      currentFrame = Some(f)

      //???




    }


    def sendCommand(command: UICommand): Unit = currentFrame.foreach{ f ⇒ runAndWait(f.onCommand.apply(command))}



    def onEvent(proc: PartialFunction[UIEvent,Unit]): Unit = {eventProcs +:= proc}


    private[core] def procEvent(event: UIEvent): Unit = eventProcs.foreach(_.apply(event))


  }



  //TODO Если включен авто показ фрейма, он должен выполнятся сразу перед функцией старта (не на постройке)



  //Internal API
  private[core] def showFrame(): Unit = ???
  private[core] def hideFrame(): Unit = ???
  private[core] def closeFrame(): Unit = ???
  private[core] def uiEvent(event: UIEvent): Unit = UI.procEvent(event)






}
