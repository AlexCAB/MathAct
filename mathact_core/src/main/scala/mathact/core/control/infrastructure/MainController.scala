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

package mathact.core.control.infrastructure

import java.util.concurrent.ExecutionException

import akka.actor._
import com.typesafe.config.Config
import mathact.AppConfig
import mathact.core.bricks.WorkbenchLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums.SketchStatus
import mathact.core.model.messages.{M, Msg}
import mathact.core.ActorBase
import mathact.core.gui.SelectSketchWindow

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

class MainController(doStop: Int⇒Unit, config: AppConfig) extends ActorBase{
  //Parameters
  val sketchStartTimeout = 5.seconds
  //Messages
  case object ShowUI
  case class RunSketch(className: String)
  case class SketchStarted(className: String)
  case class SketchStartTimeout(className: String)
  case object DoStop                     //Normal stop
  case object DoErrorStop                //Stop by error
  case class DoTerminate(exitCode: Int)  //Free resources and terminate
  //Holders
  case class CurrentSketch(sketch: SketchData, isWorking: Boolean, controller: Option[ActorRef]){
    def started(): CurrentSketch = CurrentSketch(sketch, isWorking = true, controller)
    def withController(controller: ActorRef): CurrentSketch = CurrentSketch(sketch, isWorking, Some(controller))}
  //UI definitions
  val uiSelectSketch = new SelectSketchWindow(log){
    def sketchSelected(sketchClassName: String): Unit = {self ! RunSketch(sketchClassName)}
    def windowClosed(): Unit = {self ! DoStop}}
  //Variables
  var sketches = List[SketchData]()
  var currentSketch: Option[CurrentSketch] = None




  //TODO Код ниже это конструирование субакторов для WorkbenchController
  //new WorkbenchController(...){
  //  val randomName = "_" + sketchData.className + "_" + UUID.randomUUID
  //  val sketchUi = context.actorOf(Props(
  //    new SketchUI(config.sketchUI, self)),
  //    "SketchUI" + randomName)
  //  val userLogging = context.actorOf(Props(
  //    new UserLogging(config.userLogging, self)),
  //    "UserLogging" + randomName)
  //  val visualization = context.actorOf(Props(
  //    new Visualization(config.visualization, self)),
  //    "Visualization" + randomName)
  //  val pumping = context.actorOf(Props(
  //    new Pumping(config.pumping, self, sketchName, userLogging, visualization)),
  //    "Pumping" + randomName)
  //}




  //Functions
  def setCurrentSketchState(newStat: SketchStatus): Unit = ???

//    currentSketch.foreach{ cs ⇒
//    sketches = sketches.map{
//      case s if s.className == cs.sketch.className ⇒ s.copy(status = newStat)
//      case s ⇒ s}}
  def cleanCurrentSketch(): Unit = {
    currentSketch.foreach(_.controller.foreach(_ ! M.ShutdownSketchController))
    currentSketch = None}
  //Messages handling
  def reaction = {
    //Handling of starting
    case M.MainControllerStart(sketchList) ⇒
      sketches = sketchList
      //Check if there is autoruned
//      sketchList.find(_.status == SketchStatus.Autorun) match{
//        case Some(sketch) ⇒
//          self ! RunSketch(sketch.className)
//        case None ⇒
//          self ! ShowUI}
    //Display UI
    case ShowUI ⇒
//      tryToRun{uiSelectSketch.show(sketches)} match{
//        case Success(_) ⇒
//          log.debug("[MainController.MainControllerStart] UI is created.")
//        case Failure(_) ⇒
//          self ! DoErrorStop}
    //Run selected sketch
    case RunSketch(className) ⇒
      (currentSketch, sketches.find(_.className == className)) match{
        case (None, Some(sketch)) ⇒
          currentSketch = Some(CurrentSketch(sketch, isWorking = false, None))
          //Starting creating timeout
          context.system.scheduler.scheduleOnce(sketchStartTimeout, self, SketchStartTimeout(className))
          //Hid UI
//          tryToRun{uiSelectSketch.hide()}
          //Create Workbench instance
          Future{sketch.clazz.newInstance()}
            .map{ _ ⇒ self ! SketchStarted(className)}
            .recover{
              case t: ExecutionException ⇒ self ! M.SketchError(className, t.getCause)
              case t: Throwable ⇒ self ! M.SketchError(className, t)}
        case (Some(curSketch), _) if curSketch.sketch.className != className ⇒
          log.warning(s"[MainController.RunSketch] Current sketch $curSketch not ended.")
        case (_, None) ⇒
          log.error(s"[MainController.RunSketch] Not found sketch for className: $className")
        case _ ⇒}
    //Creating of new SketchContext instance, return Either[Exception,SketchContext]
    case M.NewSketchContext(workbench: WorkbenchLike) ⇒

      //TODO Проветить запрос от текущего скетча и если нет ошыбка, если да пересылка контроллкру скетча GetSketchContext



//
//
//      (currentSketch, Option(workbench.getClass.getCanonicalName)) match {
//        case (Some(s), Some(cn)) if s.sketch.className == cn ⇒
//          //Create SketchContext
//          val controller: ActorRef = ???
//
//
////            context.actorOf(
////            Props(new WorkbenchController(s.sketch, self, config)),
////            "WorkbenchControllerActor_" + s.sketch.className)
//
//
//          context.watch(controller)
//          currentSketch = currentSketch.map(_.withController(controller))
//          //Init of Workbench Controller
//          controller ! M.WorkbenchControllerInit(sender)
//
//
//
//
//
//        case (_, cn) ⇒  sender ! Left(new Exception(
//          s"[MainController.NewSketchContext] Workbench class $cn not match a current sketch: $currentSketch"))}
    //SketchData started
    case SketchStarted(className) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{
        case s if s.controller.nonEmpty ⇒
          s.controller.foreach(_ ! M.StartSketchController)
          currentSketch = currentSketch.map(_.started())
        case s ⇒
          self ! M.SketchError(className, new Exception(
            s"[MainController.SketchStarted] Workbench controller not created, current sketch: $currentSketch"))}
    //Normal end of sketch
    case M.SketchDone(className) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{ _ ⇒
        log.info(s"[MainController.SketchDone] Current sketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Ended)
        cleanCurrentSketch()
        self ! ShowUI}
    //Failure end of sketch
    case M.SketchError(className, error) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{ _ ⇒
        log.error(
          s"[MainController.SketchError] Error: $error currentSketch: $currentSketch, " +
          s"StackTrace: \n ${error.getStackTrace.mkString("\n")}")
        setCurrentSketchState(SketchStatus.Failed)
        cleanCurrentSketch()
        self ! ShowUI}
    //SketchData start timeout
    case SketchStartTimeout(className) ⇒
      currentSketch.filter(cs ⇒ cs.sketch.className == className && (! cs.isWorking)).foreach{ s ⇒
        log.error(s"[MainController.SketchStartTimeout] Timeout: $sketchStartTimeout, currentSketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Failed)
        cleanCurrentSketch()
        self ! ShowUI}
    //Terminated of current sketch
    case Terminated(actor) ⇒
      currentSketch.filter(_.controller.contains(actor)).foreach{ _ ⇒
        log.error(s"[MainController.Terminated] Actor: $actor, currentSketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Failed)
        currentSketch = None
        self ! ShowUI}
    //Self normal stopping
    case DoStop ⇒
      cleanCurrentSketch()
      self ! DoTerminate(0)
    //Error normal stopping
    case DoErrorStop ⇒
      cleanCurrentSketch()
      self ! DoTerminate(-1)
    case DoTerminate(exitCode) ⇒
      //Hide UI
//      tryToRun{uiSelectSketch.hide()}
      //Call stop
      doStop(exitCode)
      self ! PoisonPill}}
