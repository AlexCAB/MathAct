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

package mathact.core.control.view.visualization

import akka.actor.{PoisonPill, ActorRef}
import mathact.core.ActorBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.VisualizationConfigLike
import mathact.core.model.messages.M


/** SketchData visualization actor
  * Created by CAB on 31.08.2016.
  */


class VisualizationActor(config: VisualizationConfigLike, workbenchController: ActorRef)
extends ActorBase with JFXInteraction { import Visualization._
  //Variables
  private var isShow = false
  //Construction
  private val window = runNow{
    val stg = new VisualizationViewAndController(config, self, log)
    stg.resizable = false
    stg.sizeToScene()
    stg}


  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Close button hit
    case DoClose ⇒
      isShow = false
      runAndWait(window.hide())
      workbenchController ! M.VisualizationUIChanged(isShow)






    //


      //TODO Попробовать https://jgraph.github.io/mxgraph/docs/manual_javavis.html
      //TODO
      //TODO





    //Show UI
    case M.ShowVisualizationUI ⇒
      isShow = true
      runAndWait(window.show())
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Hide UI
    case M.HideVisualizationUI ⇒
      isShow = false
      runAndWait(window.hide())
      workbenchController ! M.VisualizationUIChanged(isShow)
    //Terminate UI
    case M.TerminateVisualization ⇒
      runAndWait(window.close())
      workbenchController ! M.VisualizationTerminated
      self ! PoisonPill


    //TODO Сообщения обновление вида


  }





}