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

package mathact.core.app.view

import akka.actor.{ActorRef, PoisonPill}
import mathact.core.WorkerBase
import mathact.core.gui.JFXInteraction
import mathact.core.model.config.MainUIConfigLike
import mathact.core.model.messages.M


/** Main UI actor
  * Created by CAB on 11.10.2016.
  */

private [mathact] class MainUIActor(
  config: MainUIConfigLike,
  mainController: ActorRef)
extends WorkerBase with JFXInteraction {
  //Construction
  val window = runNow{
    val stg = new MainUIViewAndController(config, self, log)
    stg.resizable = false
    stg.sizeToScene()
    stg}
  //Variables
  var isShow = false
  var isSelected = false
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Set sketch list
    case M.SetSketchList(sketches) ⇒
      runAndWait(window.setTableData(sketches))
      if(! isShow) runAndWait(window.show())
      isShow = true
      isSelected = false
    //Run sketch
    case M.RunSketch(sketch) ⇒ isSelected match{
      case false ⇒
        log.debug("[MainUIActor @ RunSketch] Sketch: " + sketch)
        runAndWait(window.disableRunButtonsExceptGiven(sketch))
        mainController ! M.RunSketch(sketch)
        isSelected = true
      case true ⇒
        log.debug("[MainUIActor @ RunSketch] An sketch already selected, do nothing.")}
    //Hide main UI
    case M.HideMainUI ⇒
      if(isShow) runAndWait(window.hide())
      isShow = false
    //Main close btn hit
    case M.MainCloseBtnHit ⇒ isSelected match{
      case false ⇒
        log.debug("[MainUIActor @ MainCloseBtnHit] Hide UI and send MainCloseBtnHit to main controller.")
        runAndWait(window.hide())
        mainController ! M.MainCloseBtnHit
        isShow = false
      case true ⇒
        log.debug("[MainUIActor @ MainCloseBtnHit] An sketch selected, do nothing.")}
    //Terminate UI
//    case M.TerminateMainUI ⇒
//      runAndWait(window.close())
//      ???
////      mainController ! M.MainUITerminated
//      self ! PoisonPill
  }
  ???
}
