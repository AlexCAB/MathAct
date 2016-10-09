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
  private val window = runNow{ new VisualizationViewAndController(config, self, log) }


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




      runAndWait{
        window.drawGraph(GraphData(
          tools = List(
            ToolData(
              toolId = 1,
              toolName = "A",
              toolImage = None,
              inlets = Map(1 → Some("i_1")),
              outlets = Map(1 → Some("o_1"), 2 → Some("o_2"))),
            ToolData(
              toolId = 2,
              toolName = "B",
              toolImage = None,
              inlets = Map(1 → Some("i_1"), 2 → Some("i_2")),
              outlets = Map(1 → Some("o_1"), 2 → Some("o_2"))),
            ToolData(
              toolId = 3,
              toolName = "C",
              toolImage = Some(ToolImageData (20,20, "/mathact/userLog/info_img.png")),
              inlets = Map(1 → Some("i_1")),
              outlets = Map(1 → Some("o_1")))),
          connections = List(
            ConnectionData(
              inletToolId = 1,
              inletId = 1,
              outletToolId = 2,
              outletId = 1),
            ConnectionData(
              inletToolId = 2,
              inletId = 1,
              outletToolId = 3,
              outletId = 1),
            ConnectionData(
              inletToolId = 3,
              inletId = 1,
              outletToolId = 1,
              outletId = 1),
            ConnectionData(
              inletToolId = 2,
              inletId = 2,
              outletToolId = 2,
              outletId = 2))))
        window.doLayout(LayoutType.TreeLayout)}






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