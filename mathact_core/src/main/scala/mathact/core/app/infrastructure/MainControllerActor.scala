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

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import mathact.core.bricks.data.SketchData
import mathact.core.model.config.MainConfigLike
import mathact.core.model.enums.SketchStatus
import mathact.core.model.holders.MainUIRef
import mathact.core.model.messages.M


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

private[core] abstract class MainControllerActor(config: MainConfigLike, doStop: Int⇒Unit)
extends Actor{
  //Strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  //Sub actors (abstract fields defined here to capture this actor context)
  val mainUi: MainUIRef
  //Variables
  var sketchList = List[(SketchData, SketchStatus)]()
  var currentSketch: Option[(ActorRef,SketchData)] = None
  var lastError: Option[Throwable] = None
  //Abstract methods
  def createSketchController(config: MainConfigLike, sketchData: SketchData): ActorRef
  //Functions
  def stopAppByNormal(): Unit = {
    log.debug(s"[MainControllerActor.stopApp] Normal stop, application will terminated.")
    self ! PoisonPill}
  def stopAppByError(error: Throwable): Unit = {
    log.error(error, s"[MainControllerActor.fatalError] Stop by fatal error, application will terminated.")
    lastError = Some(error)
    self ! PoisonPill}
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
      val msg = s"[MainControllerActor.forSketch] Not found sketch for className: $className , sketchList: $sketchList"
      log.error(msg)
      stopAppByError(new IllegalArgumentException(msg))}
  def forCurrentSketch(className: String)(proc: (ActorRef, SketchData) ⇒ Unit): Unit = currentSketch match{
    case Some((actor, sketch)) if sketch.className == className ⇒
      proc(actor, sketch)
    case cs ⇒
      log.error(s"[MainControllerActor.forCurrentSketch] No or wrong current sketch, currentSketch: $cs")}
  def setAndShowUISketchTable(): Unit =
    mainUi ! M.SetSketchList(sketchList.map{ case (d,s) ⇒ d.toSketchInfo(s)})
  //Actor reaction on messages
  def receive: PartialFunction[Any, Unit] = {
    //Main controller start
    case M.MainControllerStart(sketches) ⇒
      //Set sketch list
      sketchList = sketches.map(s ⇒ (s, SketchStatus.Ready))
      //If exist auto-run sketch then run, otherwise show UI
      sketches.find(_.autorun) match{
        case Some(sketch) ⇒
          log.debug("[MainControllerActor @ MainControllerStart] Auto-run sketch: " + sketch)
          runSketch(sketch)
        case None ⇒
          log.debug("[MainControllerActor @ MainControllerStart] No sketch to auto-run found, show UI.")
          mainUi ! M.SetSketchList(sketchList.map{ case (d,s) ⇒ d.toSketchInfo(s) })}
    //Run selected sketch
    case M.RunSketch(sketchInfo) if currentSketch.isEmpty ⇒ forSketch(sketchInfo.className){ sketch ⇒
      runSketch(sketch)}
    //New sketch context, redirect to sketch controller if exist
    case M.NewSketchContext(workbench, sketchClassName) ⇒ forCurrentSketch(sketchClassName){ case (actor, sketch) ⇒
      actor ! M.GetSketchContext(sender)}
    //Sketch built, hide UI
    case M.SketchBuilt(className) ⇒ forCurrentSketch(className){ case (_, sketch) ⇒
      mainUi ! M.HideMainUI}
    //Sketch fail, hide UI
    case M.SketchFail(className) ⇒ forCurrentSketch(className){ case (_, sketch) ⇒
      mainUi ! M.HideMainUI}
    //Sketch done successfully
    case M.SketchDone(className) ⇒ forCurrentSketch(className){ case (actor, sketch) ⇒
      setSketchSate(className, SketchStatus.Ended)
      context.unwatch(sender)
      setAndShowUISketchTable()
      currentSketch = None}
    //Sketch done with error
    case M.SketchError(className, errors) ⇒ forCurrentSketch(className){ case (actor, sketch) ⇒
      log.error("[MainControllerActor @ SketchError] Sketch failed.")
      errors.foreach(e ⇒ log.error(e, "[MainControllerActor @ SketchError] Error."))
      setSketchSate(className, SketchStatus.Failed)
      context.unwatch(sender)
      setAndShowUISketchTable()
      currentSketch = None}
    //Main close hit, terminate if to sketch ran
    case M.MainCloseBtnHit if currentSketch.isEmpty ⇒
      stopAppByNormal()
    //Termination of actor
    case Terminated(actor) ⇒ actor match{
      case a if a == mainUi.ref ⇒
        val msg = s"[MainControllerActor @ Terminated] Main UI terminated suddenly, currentSketch: $currentSketch"
        log.error(msg)
        stopAppByError(new Exception(msg))
      case a if currentSketch.map(_._1).contains(a) ⇒
        log.error(s"[MainControllerActor @ Terminated] Current sketch terminated suddenly, currentSketch: $currentSketch")
        setSketchSate(currentSketch.get._2.className, SketchStatus.Failed)
        setAndShowUISketchTable()
        currentSketch = None
      case a ⇒
        log.error("[MainControllerActor @ Terminated] Unknown actor: " + a)}
    //Unknown message
    case m ⇒
      log.error(s"[MainControllerActor @ ?] Unknown message: $m")}
  //Do stop on termination
  override def postStop(): Unit =  {
    log.debug("[MainControllerActor.postStop] Call doStop.")
    doStop(if(lastError.nonEmpty) -1 else 0)}}