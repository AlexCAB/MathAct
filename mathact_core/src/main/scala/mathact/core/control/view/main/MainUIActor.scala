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

package mathact.core.control.view.main

import akka.actor.{PoisonPill, ActorRef}
import mathact.core.ActorBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.MainUIConfigLike
import mathact.core.model.messages.M


/** Main UI actor
  * Created by CAB on 11.10.2016.
  */

private [mathact] class MainUIActor(
  config: MainUIConfigLike,
  mainController: ActorRef)
extends ActorBase with JFXInteraction {
  //Construction
  private val window = runNow{
    val stg = new MainUIViewAndController(config, mainController, log)
    stg.resizable = false
    stg.sizeToScene()
    stg}
  //Variables
  private var isShow = false
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Set sketch list
    case M.SetSketchList(sketches) ⇒
      if(isShow) runAndWait(window.hide())
      runAndWait{
        window.setTableData(sketches)
        window.show()}
      isShow = true
    //Run sketch
    case M.RunSketch(sketch) ⇒
      runAndWait(window.hide())
      mainController ! M.RunSketch(sketch)
      isShow = false
    //Main close btn hit
    case M.MainCloseBtnHit ⇒
      runAndWait(window.hide())
      mainController ! M.MainCloseBtnHit
      isShow = false
    //Terminate UI
    case M.TerminateMainUI ⇒
      runAndWait(window.close())
      mainController ! M.MainUITerminated
      self ! PoisonPill}}
