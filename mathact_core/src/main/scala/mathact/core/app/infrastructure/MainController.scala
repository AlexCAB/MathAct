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

package mathact.core.app.infrastructure

import akka.actor._
import mathact.core.WorkerBase
import mathact.core.model.config.MainConfigLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums.SketchStatus
import mathact.core.model.messages.M


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

private [mathact] abstract class MainController(config: MainConfigLike, doStop: Int⇒Unit)
extends WorkerBase{
  //Sub actors (abstract fields defined here to capture this actor context)
  val mainUi: ActorRef
  //Variables
  var sketchList = List[(SketchData, SketchStatus)]()
  var currentSketch: Option[(ActorRef,SketchData)] = None
  var isFatalError = false
  //Abstract methods
  def createSketchController(config: MainConfigLike, sketchData: SketchData): ActorRef
  //Functions
  def runSketch(sketch: SketchData): Unit = {
    //Create actor
    val sketchController = createSketchController(config, sketch)
    context.watch(sketchController)
    //Run actor
    sketchController ! M.LaunchSketch
    //Set current
    currentSketch = Some((sketchController, sketch))}
  def setSketchSate(className: String, newState: SketchStatus): Unit = {
    sketchList = sketchList.map{
      case (s,_) if s.className == className ⇒ (s, newState)
      case s ⇒ s}}
  def forSketch(className: String)(proc: SketchData ⇒ Unit): Unit = sketchList.find(_._1.className == className) match{
    case Some((sketch, _)) if sketch.className == className ⇒
      proc(sketch)
    case _ ⇒
      log.error(s"[MainController.forSketch] Not found sketch for className: $className , sketchList: $sketchList")}
  def forCurrentSketch(className: String)(proc: (ActorRef, SketchData) ⇒ Unit): Unit = currentSketch match{
    case Some((actor, sketch)) if sketch.className == className ⇒ proc(actor, sketch)
    case cs ⇒ log.error("[MainController.forCurrentSketch] No or wrong current sketch, currentSketch: " + cs)}
  def setAndShowUISketchTable(): Unit =
    mainUi ! M.SetSketchList(sketchList.map{ case (d,s) ⇒ d.toSketchInfo(s)})
  //Actor reaction on messages
  def reaction: PartialFunction[Any, Unit] = {
    //Main controller start
    case M.MainControllerStart(sketches) ⇒
      //Set sketch list
      sketchList = sketches.map(s ⇒ (s, SketchStatus.Ready))
      //If exist auto-run sketch then run, otherwise show UI
      sketches.find(_.autorun) match{
        case Some(sketch) ⇒
          log.debug("[MainController @ MainControllerStart] Auto-run sketch: " + sketch)
          runSketch(sketch)
        case None ⇒
          log.debug("[MainController @ MainControllerStart] No sketch to auto-run found, show UI.")
          mainUi ! M.SetSketchList(sketchList.map{ case (d,s) ⇒ d.toSketchInfo(s)})}
    //Run selected sketch
    case M.RunSketch(sketchInfo) if currentSketch.isEmpty ⇒ forSketch(sketchInfo.className){ sketch ⇒
      runSketch(sketch)}
    //New sketch context, redirect to sketch controller if exist
    case M.NewSketchContext(workbench, sketchClassName) ⇒ forCurrentSketch(sketchClassName){ case (actor, sketch) ⇒
      actor ! M.GetSketchContext(sender)}
    //Sketch built, hide UI
//    case M.SketchBuilt(className, workbench) ⇒ forCurrentSketch(className){ case (_, sketch) ⇒
//      if(! sketch.autorun) mainUi ! M.HideMainUI}
    //Sketch done
    case M.SketchDone(className) ⇒ forCurrentSketch(className){ case (actor, sketch) ⇒
      setSketchSate(className, SketchStatus.Ended)}
    //Sketch error
    case M.SketchError(className, errors) ⇒ forCurrentSketch(className){ case (actor, sketch) ⇒
      log.error("[MainController @ SketchError] Sketch failed.")
      errors.foreach(e ⇒ log.error(e, "[MainController @ SketchError] Error."))
      setSketchSate(className, SketchStatus.Failed)}
    //Sketch controller terminated, show UI
//    case M.SketchControllerTerminated(className) ⇒ forCurrentSketch(className){ case (actor, sketch) ⇒
//      context.unwatch(sender)
//      setAndShowUISketchTable()
//      currentSketch = None
//      if(isFatalError) self ! PoisonPill}
//    //Main close hit, terminate UI
//    case M.MainCloseBtnHit if currentSketch.isEmpty ⇒
//      mainUi ! M.TerminateMainUI
//    //Main close hit, call do stop and terminate self
//    case M.MainUITerminated ⇒
//      context.unwatch(mainUi)
//      self ! PoisonPill
    //Termination of actor
    case Terminated(actor) ⇒ actor match{
      case a if a == mainUi ⇒
        log.error(s"[MainController @ Terminated] Main UI terminated suddenly, currentSketch: $currentSketch" )
        //Stop application
        isFatalError = true
        currentSketch match {
          case Some(sketch) ⇒

            ??? //sketch._1 ! M.ShutdownSketch
          case None ⇒ self ! PoisonPill}
      case a if currentSketch.map(_._1).contains(a) ⇒
        log.error(s"[MainController @ Terminated] Current sketch terminated suddenly, currentSketch: $currentSketch")
        setSketchSate(currentSketch.get._2.className, SketchStatus.Failed)
        setAndShowUISketchTable()
        currentSketch = None
      case a ⇒
        log.error("[MainController @ Terminated] Unknown actor: " + a)}}
  //Do stop on termination
  override def postStop(): Unit =  {
    super.postStop()
    log.debug("[MainController.postStop] Call doStop.")
    doStop(if(isFatalError) -1 else 0)}}