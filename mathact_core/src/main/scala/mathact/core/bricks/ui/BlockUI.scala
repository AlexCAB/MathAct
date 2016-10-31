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

import akka.actor.Actor
import mathact.core.gui.JFXInteraction
import mathact.core.gui.ui.{BlockFrameLike, BlockUILike}
import mathact.core.sketch.blocks.BlockLike

import scala.reflect.ClassTag
import scalafx.scene.Scene
import scalafx.stage.Stage


/** Adding UI to block.
  * Created by CAB on 31.08.2016.
  */

trait BlockUI extends BlockUILike { _: BlockLike ⇒
  //Variables
  @volatile private var isUIRegistered = false




  //Definitions
  protected trait Frame extends Stage with BlockFrameLike {
    //Check if registered
    if (! isUIRegistered) throw new IllegalStateException(
      "[BlockUI.Frame.<init>] Frame instance should be created by using of UI object.")
    //

    def sendEvent(event: UIEvent): Unit = ???


    def showOnStart: Boolean = ???
    def showOnStart_=(v: Boolean) {
      ???
    }



  }



  //UI creation
  protected object UI extends JFXInteraction{
    def apply[T <: BlockFrameLike : ClassTag]: Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val clazz = implicitly[ClassTag[T]].runtimeClass
      val stage = runNow(clazz.newInstance())

      ???

    }


    def apply(frame: ⇒BlockFrameLike): Unit = {
      //Set registered
      isUIRegistered = true
      //Creating stage
      val stage = runNow(frame)

      ???




    }


    def sendCommand(command: UICommand): Unit = ???

    def onEvent(proc: PartialFunction[UIEvent,Unit]): Unit = ???


  }







  //Internal API
  private[core] def setFrameVisible(boolean: Boolean): Unit = ???
  private[core] def closeFrame(): Unit = ???






}
